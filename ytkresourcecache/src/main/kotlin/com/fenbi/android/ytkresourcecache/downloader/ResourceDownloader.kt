package com.fenbi.android.ytkresourcecache.downloader

import com.fenbi.android.ytkresourcecache.CacheResourceWriter
import okhttp3.*
import java.io.IOException
import java.io.OutputStream
import java.lang.IllegalStateException
import java.lang.RuntimeException

/**
 * Created by yangjw on 2019/1/21.
 */
class ResourceDownloader(
    private val cacheWriter: CacheResourceWriter,
    clientInit: OkHttpClient.Builder.() -> Unit
) {

    private val callMap = hashMapOf<String, Call>()
    private val client: OkHttpClient = OkHttpClient.Builder()
        .apply {
            clientInit()
        }.build()

    fun download(url: String, callback: DownloadCallback? = null) {
        var call = callMap[url]
        if (call != null && call.isExecuted) {
            return
        }
        val outputStream = cacheWriter.getStream(url)
        if (outputStream == null) {
            callback?.onFailed(RuntimeException("verify file error."))
            return
        }
        val builder = Request.Builder().url(url)
        if (outputStream is PauseableOutputStream) {
            builder.header("Range", "bytes=${outputStream.length()}-")
        }
        call = client.newCall(builder.build())
        callMap[url] = call
        call.enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val resourceInfo = ResourceInfo(url, response, outputStream, callback)
                processResponse(resourceInfo)
                callMap.remove(url)
            }

            override fun onFailure(call: Call, e: IOException) {
                callback?.onFailed(e)
                callMap.remove(url)
            }
        })
    }

    private fun cancel(url: String) {
        callMap[url]?.cancel()
        if (callMap[url]?.isExecuted != true) {
            callMap.remove(url)
        }
    }

    private fun processResponse(resourceInfo: ResourceInfo): Boolean {
        with(resourceInfo) {
            if (!response.isSuccessful) {
                callback?.onFailed(IllegalStateException("HTTP response code is ${response.code()}"))
                return false
            }
            val totalLength = parseResourceLength(response)
            val body = response.body()
            if (body == null) {
                callback?.onFailed(IllegalStateException("Response body is null."))
                return false
            }
            val inputStream = body.byteStream()
            var timestamp = System.currentTimeMillis()
            var currentSize = if (outputStream is PauseableOutputStream) outputStream.length() else 0
            val buffer = ByteArray(4096)
            var len: Int
            try {
                while (true) {
                    if (callMap[url]?.isCanceled == true) {
                        callback?.onCanceled()
                        return false
                    }
                    len = inputStream.read(buffer)
                    if (len == -1) {
                        break
                    }
                    outputStream.write(buffer, 0, len)
                    currentSize += len.toLong()
                    if (System.currentTimeMillis() - timestamp > 300L) {
                        timestamp = System.currentTimeMillis()
                        callback?.onProgress(currentSize, totalLength)
                    }
                }
                outputStream.flush()
                if (outputStream is PauseableOutputStream) outputStream.onCacheSuccess()
                callback?.onSuccess()
                return true
            } catch (e: IOException) {
                if (outputStream is PauseableOutputStream) outputStream.onCacheFailed()
                callback?.onFailed(e)
            } finally {
                inputStream.close()
                outputStream.close()
                response.close()
            }
        }
        return false
    }

    private fun parseResourceLength(response: Response?): Long {
        var length = -1L
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

    inner class ResourceInfo(
        val url: String,
        val response: Response,
        val outputStream: OutputStream,
        val callback: DownloadCallback?
    )
}