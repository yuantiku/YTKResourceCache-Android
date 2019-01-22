package com.fenbi.android.ytkresourcecache

import android.Manifest
import android.support.test.InstrumentationRegistry
import android.support.test.rule.GrantPermissionRule
import android.support.test.runner.AndroidJUnit4
import android.util.Log
import com.fenbi.android.ytkresourcecache.downloader.PauseableOutputStream
import com.fenbi.android.ytkresourcecache.downloader.ResourceDownloader
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.OutputStream
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Created by yangjw on 2019/1/21.
 */
@RunWith(AndroidJUnit4::class)
class TestResourceDownloader {

    @get:Rule
    var permissionRule = GrantPermissionRule.grant(
        Manifest.permission.INTERNET
        , Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    val cacheStorage = object : FileCacheStorage(InstrumentationRegistry.getContext()) {
        override val cacheWriter = object : CacheResourceWriter {
            override fun getStream(url: String?): OutputStream? {
                if (url == null) return null
                val path = File(cacheDir, mappingRule.mapUrlToPath(url)).absolutePath
                return PauseableOutputStream(path)
            }
        }
    }

    @Test
    fun testDownloadSingleFile() {
        val resourceDownloader = ResourceDownloader(cacheStorage.cacheWriter)
        val url = "http://t2.hddhhn.com/uploads/tu/201812/621/640.webp%20(43).jpg"
        val future = CompletableFuture<Long>()
        resourceDownloader.download(url) {
            onSuccess = {
                val file = cacheStorage.getCacheFile(url)
                assertNotNull(file)
                future.complete(file?.length())
            }

            onFailed = { e: Throwable ->
                throw e
            }


            onCanceled = {

            }

            onProgress = { loaded, total ->
                Log.d("testDownloadSingleFile", "onProgress loaded:$loaded  total:$total")
            }
        }
        assertEquals(future.get(120, TimeUnit.SECONDS), 413005)
    }

    @Test
    fun testDownloadMultiFiles() {
        val resourceDownloader = ResourceDownloader(cacheStorage.cacheWriter)
        val urlList = listOf(
            "http://t2.hddhhn.com/uploads/tu/201812/621/640.webp%20(43).jpg",
            "http://t2.hddhhn.com/uploads/tu/201812/621/640.webp%20(41).jpg",
            "https://t2.hddhhn.com/uploads/tu/201812/621/640.webp%20(34).jpg"
        )
        val future = CompletableFuture<Long>()
        resourceDownloader.download(urlList) {
            onUrlSuccess = {
                Log.d("testDownloadMultiFiles", "onUrlSuccess url=$it")
            }

            onUrlFailed = { url: String, e: Throwable ->
                throw e
            }


            onUrlCanceled = {

            }

            onProgress = {
                Log.d("testDownloadMultiFiles", "onProgress: $it")
                val finishSize = it.progressMap.values
                    .filter { it.first == it.second && it.second > 0 }
                    .size
                if (finishSize == urlList.size) {
                    val sum = it.progressMap.values.map { it.second }.sum()
                    future.complete(sum)
                }
            }
        }
        assertEquals(future.get(300, TimeUnit.SECONDS), 413005 + 297010 + 240075)
    }
}