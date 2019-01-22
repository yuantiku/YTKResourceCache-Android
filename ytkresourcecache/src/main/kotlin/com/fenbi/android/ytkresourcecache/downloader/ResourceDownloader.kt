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
    private val client: OkHttpClient = defaultOkHttpClient
) {

    private var callMap = hashMapOf<String, Call>()

    fun download(url: String, callbackInit: (DownloadCallback.() -> Unit)? = null) {
        var call = callMap[url]
        if (call != null && !call.isCanceled) {
            return
        }
        val callback = DownloadCallback().apply { callbackInit?.invoke(this) }
        val outputStream = cacheWriter.getStream(url)
        if (outputStream == null) {
            callback.onFailed?.invoke(RuntimeException("verify file error."))
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
                callback.onFailed?.invoke(e)
                callMap.remove(url)
            }
        })
    }

    fun download(urlList: List<String>, callbackInit: (MultiDownloadCallback.() -> Unit)? = null) {
        val multiDownloadCallback = MultiDownloadCallback().apply { callbackInit?.invoke(this) }
        val multiProgress = MultiProgress(urlList, hashMapOf())
        urlList.distinct().forEach {
            download(it) {
                onSuccess = {
                    multiDownloadCallback.onUrlSuccess?.invoke(it)
                }

                onFailed = { e: Throwable ->
                    multiDownloadCallback.onUrlFailed?.invoke(it, e)
                }

                onCanceled = {
                    multiDownloadCallback.onUrlCanceled?.invoke(it)
                }

                onProgress = { loaded, total ->
                    multiProgress.progressMap[it] = Pair(loaded, total)
                    multiDownloadCallback.onProgress?.invoke(multiProgress)
                }
            }
        }
    }

    fun cancel(url: String? = null) {
        val runningCalls = client.dispatcher().runningCalls()
        if (url != null) {
            val call = callMap[url] ?: return
            call.cancel()
            if (!runningCalls.contains(call)) {
                callMap.remove(url)
            }
        } else {
            //cancel all download task
            callMap.forEach { it.value.cancel() }
            with(callMap.iterator()) {
                while (hasNext()) {
                    if (!runningCalls.contains(next().value)) {
                        remove()
                    }
                }
            }
        }
    }

    private fun processResponse(resourceInfo: ResourceInfo): Boolean {
        with(resourceInfo) {
            if (!response.isSuccessful) {
                callback.onFailed?.invoke(IllegalStateException("HTTP response code is ${response.code()}"))
                return false
            }
            val totalLength = parseResourceLength(response)
            val body = response.body()
            if (body == null) {
                callback.onFailed?.invoke(IllegalStateException("Response body is null."))
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
                        callback.onCanceled?.invoke()
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
                        callback.onProgress?.invoke(currentSize, totalLength)
                    }
                }
                outputStream.flush()
                if (outputStream is PauseableOutputStream) outputStream.onCacheSuccess()
                callback.onProgress?.invoke(totalLength, totalLength)
                callback.onSuccess?.invoke()
                return true
            } catch (e: IOException) {
                callback.onFailed?.invoke(e)
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

    internal data class ResourceInfo(
        val url: String,
        val response: Response,
        val outputStream: OutputStream,
        val callback: DownloadCallback
    )
}