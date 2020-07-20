package com.fenbi.android.ytkresourcecache

import android.Manifest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import android.util.Log
import com.fenbi.android.ytkresourcecache.downloader.DownloadTask
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Created by yangjw on 2019/1/21.
 */
@RunWith(AndroidJUnit4::class)
class TestDownloader {

    @get:Rule
    var permissionRule = GrantPermissionRule.grant(
        Manifest.permission.INTERNET
        , Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    val cacheStorage by lazy { FileCacheStorage(InstrumentationRegistry.getInstrumentation().context) }

    @Test
    fun testDownloadManager() {
        val urlList = listOf(
            "http://img3.doubanio.com/view/photo/r/public/p480747492.jpg",
            "http://img3.doubanio.com/view/photo/r/public/p2561716440.jpg",
            "http://img3.doubanio.com/view/photo/r/public/p2500944103.jpg"
        )
        val countDownLatch = CountDownLatch(1)
        var size: Long = 0
        val downlaodManager = DownloadTask(urlList, cacheStorage, skipExisting = false,
            onFailed = {
                throw it
            }
            ,
            onSuccess = {
                Log.w("testDownloadManager", "onSuccess")
                val sum = urlList.map { cacheStorage.getCacheFile(it)?.length() ?: 0L }.sum()
                size = sum
                countDownLatch.countDown()
            }
            ,
            onProgress = { progressList ->
                Log.w("testDownloadManager", "progressList = $progressList")
            }
        )
        downlaodManager.start()
        countDownLatch.await(60, TimeUnit.SECONDS)
        assertEquals(size, 1098564 + 158893 + 382663)
    }
}