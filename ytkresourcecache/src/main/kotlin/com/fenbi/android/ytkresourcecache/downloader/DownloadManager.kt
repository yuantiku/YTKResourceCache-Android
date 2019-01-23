package com.fenbi.android.ytkresourcecache.downloader

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.fenbi.android.ytkresourcecache.FileCacheStorage
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import okhttp3.OkHttpClient
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong
import kotlin.concurrent.timer

/**
 * @author zheng on 11/26/18
 */

class DownloadManager(
    private val cacheStorage: FileCacheStorage,
    private val httpClient: OkHttpClient = defaultOkHttpClient,
    private val skipExisting: Boolean = true,
    private val onSuccess: (() -> Unit)? = null,
    private val onFailed: ((Throwable) -> Unit)? = null,
    private val onProgress: ((List<Progress>) -> Unit)? = null,
    private val onSpeedUpdate: ((String) -> Unit)? = null
) {

    private lateinit var resourceUrls: List<String>
    private lateinit var progressList: List<Progress>
    var isDownloading = false
    private var downloadedCount: Int = 0
    private val downloaders = mutableListOf<ResourceDownloader>()
    private val downloadSize = AtomicLong(0L)
    private var downloadTimeStart = 0L
    private var timer: Timer? = null
    private val threadPool by lazy { Executors.newFixedThreadPool(THREADS).asCoroutineDispatcher() }
    private lateinit var channel: ReceiveChannel<IndexedValue<String>>
    private val uiHandler = Handler(Looper.getMainLooper())

    fun startDownload(urls: List<String>) {
        resourceUrls = urls
        isDownloading = true
        progressList = urls.map { Progress(it) }
        downloadedCount = 0
        downloaders.clear()
        downloadSize.set(0L)
        downloadTimeStart = System.currentTimeMillis()
        channel = GlobalScope.produce {
            resourceUrls.withIndex().forEach {
                send(it)
            }
        }
        launchDownloaders()
        startTimer()
    }

    fun cancel() {
        if (isDownloading) {
            isDownloading = false
            channel.cancel()
            downloaders.forEach { it.pause() }
        }
        timer?.cancel()
        timer = null
    }

    private fun launchDownloaders() {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                runDownloads()
            } catch (e: Throwable) {
                Log.e(TAG, "launchDownloaders", e)
                if (e !is CancellationException) {
                    onFailed?.invoke(e)
                }
                return@launch
            } finally {
                this@DownloadManager.cancel()
            }
            onSuccess?.invoke()
        }
    }

    private suspend fun runDownloads() = coroutineScope {
        repeat(THREADS) {
            val downloader = ResourceDownloader(cacheStorage, httpClient)
            downloaders.add(downloader)
            launch(threadPool) {
                runDownload(downloader)
            }
        }
    }

    private suspend fun runDownload(downloader: ResourceDownloader) {
        var downloadIndex = 0
        downloader.onDownloadedBytes = { length ->
            downloadSize.addAndGet(length)
        }
        downloader.onProgress = { loaded, total ->
            uiHandler.post {
                with(progressList[downloadIndex]) {
                    this.loaded = loaded
                    this.total = total
                }
                updateTotalProgress()
            }
        }
        while (isDownloading && !channel.isClosedForReceive) {
            val (index, url) = try {
                channel.receive()
            } catch (e: Throwable) {
                break
            }
            downloadIndex = index
            Log.d(TAG, "download $url")
            val file = cacheStorage.getCacheFile(url)
            if (skipExisting && file?.exists() == true && file.length() > 0) {
                withContext(Dispatchers.Main){
                    progressList[downloadIndex].total = file.length()
                }
            } else {
                try {
                    downloader.download(url)
                } catch (e: Throwable) {
                    throw e
                }
            }
            withContext(Dispatchers.Main) {
                with(progressList[downloadIndex]) {
                    loaded = total
                }
                updateTotalProgress()
            }
        }
    }

    private fun startTimer() {
        timer = timer(period = 500) {
            uiHandler.post {
                val dur = System.currentTimeMillis() - downloadTimeStart
                val speed = downloadSize.getAndSet(0L) * 1000L / dur
                downloadTimeStart = System.currentTimeMillis()
                onSpeedUpdate?.invoke(speed.readable() + "B/s")
            }
        }
    }

    private fun updateTotalProgress() {
        onProgress?.invoke(progressList)
    }

    data class Progress(val url: String, var loaded: Long = 0L, var total: Long = 0L)

    companion object {
        private const val THREADS = 4

        const val TAG = "DownloadManager"

        private val defaultOkHttpClient: OkHttpClient by lazy {
            OkHttpClient.Builder()
                .build()
        }

        private fun Long.readable(): String {
            var bytes = this
            var u = 0
            while (bytes > 1024 * 1024) {
                u++
                bytes = bytes shr 10
            }
            if (bytes > 1024)
                u++
            return String.format("%.1f%c", bytes / 1024f, " kMGTPE"[u])
        }
    }
}
