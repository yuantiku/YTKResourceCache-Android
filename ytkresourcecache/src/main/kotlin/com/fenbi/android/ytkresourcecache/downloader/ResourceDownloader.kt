/*
 * Copyright 2017 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.fenbi.android.ytkresourcecache.downloader

import android.util.Log
import com.fenbi.android.ytkresourcecache.FileCacheStorage
import com.fenbi.android.ytkresourcecache.asResourceOutputStream
import kotlinx.coroutines.CancellationException
import okhttp3.*
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.lang.IllegalStateException


open class ResourceDownloader(private val cacheStorage: FileCacheStorage, private val okHttpClient: OkHttpClient) {

    protected var url: String? = null
    private var initialSize: Long = 0
    private var interrupted: Boolean = false
    private var totalFileSize = INVALID_FILE_SIZE
    private var outputStream: OutputStream? = null
    private val downloadDir by lazy {
        val file = File(cacheStorage.cacheDir)
        if (!file.exists()){
            file.mkdirs()
        }
        return@lazy file
    }

    var onDownloadedBytes: ((Long) -> Unit)? = null
    var onProgress: ((loaded: Long, total: Long) -> Unit)? = null
    var onSuccess: ((Long) -> Unit)? = null
    var onFailed: ((url: String?, errorType: ErrorType) -> Unit)? = null

    fun getFileSize(url: String, callback: FileSizeCallback) {
        if (totalFileSize != INVALID_FILE_SIZE) {
            callback.onFileSizeGot(totalFileSize)
        }
        val request = Request.Builder()
            .url(url)
            .header("Range", "bytes=0-5")
            .build()
        val call = okHttpClient.newCall(request)
        call.enqueue(object : Callback {

            override fun onFailure(call: Call?, e: IOException?) {
                callback.onFileSizeGot(INVALID_FILE_SIZE)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call?, response: Response?) {
                totalFileSize = parseInstanceLength(response)
                callback.onFileSizeGot(totalFileSize)
            }
        })
    }

    fun download(url: String) {
        val call = buildDownloadCall(url) ?: return
        try {
            val response = call.execute()
            if (!processResponse(response)) {
                throw CancellationException()
            }
        } catch (e: Throwable) {
            throw e
        }
    }

    private fun buildDownloadCall(url: String): Call? {
        this.url = url
        interrupted = false
        outputStream = cacheStorage.cacheWriter.getStream(url)
        initialSize = outputStream?.asResourceOutputStream()?.length() ?: 0L
        val request = Request.Builder()
            .url(url)
            .header("Range", "bytes=$initialSize-")
            .build()
        return okHttpClient.newCall(request)
    }

    private fun processResponse(response: Response): Boolean {
        response.use {
            if (interrupted) {
                onFailed?.invoke(url, ErrorType.TaskCancelled)
                return false
            }
            if (outputStream == null) {
                onFailed?.invoke(url, ErrorType.FileVerifyError)
                throw IllegalStateException("outputStream is null")
            }
            if (!response.isSuccessful) {
                // delete temp file to restart from beginning at the next time.
                outputStream?.asResourceOutputStream()?.onCacheFailed()
                onFailed?.invoke(url, ErrorType.NetworkError)
                throw IllegalStateException("response is not success, response: $response")
            }
            if (!checkSpace()) {
                onFailed?.invoke(url, ErrorType.FullDiskError)
                throw IllegalStateException("no enough disk space, available space:${downloadDir.usableSpace}")
            }
            val totalLength = parseInstanceLength(response)
            val body = response.body() ?: throw IllegalStateException("response body is null, response: $response")
            val inputStream = body.byteStream()
            try {
                var timestamp = System.currentTimeMillis()
                var savedSize = initialSize
                val buffer = ByteArray(4096)
                var len: Int
                while (true) {
                    len = inputStream.read(buffer)
                    if (len == -1) {
                        break
                    }
                    onDownloadedBytes?.invoke(len.toLong())
                    outputStream?.write(buffer, 0, len)
                    savedSize += len.toLong()
                    if (System.currentTimeMillis() - timestamp > 300L) {
                        timestamp = System.currentTimeMillis()
                        onProgress?.invoke(savedSize, totalLength)
                    }
                    if (interrupted) {
                        onFailed?.invoke(url, ErrorType.TaskCancelled)
                        return false
                    }
                }
                if (savedSize != totalLength) {
                    Log.e(TAG, "file size not match, header $totalLength, download $savedSize")
                    outputStream?.asResourceOutputStream()?.onCacheFailed()
                    onFailed?.invoke(url, ErrorType.FileVerifyError)
                    throw IllegalStateException("file size not match, header $totalLength, download $savedSize, response:$response")
                }
                outputStream?.flush()
                outputStream?.asResourceOutputStream()?.onCacheSuccess()
                onProgress?.invoke(savedSize, totalLength)
                onSuccess?.invoke(totalLength)
                return true
            } catch (e: Throwable) {
                outputStream?.asResourceOutputStream()?.onCacheFailed()
                onFailed?.invoke(url, ErrorType.NetworkError)
                throw e
            } finally {
                inputStream.close()
                outputStream?.close()
            }
        }
    }

    fun pause() {
        interrupted = true
    }

    private fun checkSpace(): Boolean {
        return downloadDir.usableSpace > 20 * 1024 * 1024
    }

    interface FileSizeCallback {

        fun onFileSizeGot(fileSize: Long)
    }

    enum class ErrorType {
        NetworkError,
        TaskCancelled,
        FullDiskError,
        FileVerifyError
    }

    companion object {

        private const val INVALID_FILE_SIZE = -1L

        const val TAG = "ResourceDownloader"

        private fun parseInstanceLength(response: Response?): Long {
            var length = INVALID_FILE_SIZE
            if (response != null) {
                val range = response.header("Content-Range") ?: return -1L
                try {
                    val section = range.split("/").dropLastWhile { it.isEmpty() }.toTypedArray()
                    length = section[1].toLong()
                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                }

            }
            return length
        }

    }
}
