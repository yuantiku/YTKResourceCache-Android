package com.fenbi.android.ytkresourcecache

import android.Manifest
import android.support.test.InstrumentationRegistry
import android.support.test.rule.GrantPermissionRule
import android.support.test.runner.AndroidJUnit4
import android.util.Log
import com.fenbi.android.ytkresourcecache.downloader.DownloadCallback
import com.fenbi.android.ytkresourcecache.downloader.PauseableOutputStream
import com.fenbi.android.ytkresourcecache.downloader.ResourceDownloader
import okhttp3.Dispatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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


    @Test
    fun testDownloadSingleFile() {
        val cacheStorage = object : FileCacheStorage(InstrumentationRegistry.getContext()) {
            override val cacheWriter = object : CacheResourceWriter {
                override fun getStream(url: String?): OutputStream? {
                    if (url == null) return null
                    val path = File(cacheDir, mappingRule.mapUrlToPath(url)).absolutePath
                    return PauseableOutputStream(path)
                }
            }
        }
        val resourceDownloader = ResourceDownloader(cacheStorage.cacheWriter) {
            connectTimeout(10, TimeUnit.SECONDS)
            dispatcher(Dispatcher(Executors.newFixedThreadPool(4)))
        }
        val url = "http://www.ovh.net/files/1Mio.dat"
        val future = CompletableFuture<Long>()
        resourceDownloader.download(url, object : DownloadCallback {
            override fun onSuccess() {
                val file = cacheStorage.getCacheFile(url)
                assertNotNull(file)
                future.complete(file?.length())
            }

            override fun onFailed(e: Throwable) {
                throw e
            }

            override fun onCanceled() {
                throw RuntimeException()
            }

            override fun onProgress(loaded: Long, total: Long) {
                Log.d("testDownloadSingleFile", "onProgress loaded:$loaded  total:$total")
            }

        })
        assertEquals(future.get(120, TimeUnit.SECONDS), 1024 * 1024)
    }
}