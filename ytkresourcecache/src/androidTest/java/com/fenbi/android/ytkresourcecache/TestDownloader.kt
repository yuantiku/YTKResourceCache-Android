package com.fenbi.android.ytkresourcecache

import android.Manifest
import android.support.test.InstrumentationRegistry
import android.support.test.rule.GrantPermissionRule
import android.support.test.runner.AndroidJUnit4
import android.util.Log
import com.fenbi.android.ytkresourcecache.downloader.DownloadTask
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CompletableFuture
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

    val cacheStorage by lazy { FileCacheStorage(InstrumentationRegistry.getContext()) }

    @Test
    fun testDownloadManager() {
        val urlList = listOf(
            "http://t2.hddhhn.com/uploads/tu/201812/621/640.webp%20(43).jpg",
            "http://t2.hddhhn.com/uploads/tu/201812/621/640.webp%20(41).jpg",
            "https://t2.hddhhn.com/uploads/tu/201812/621/640.webp%20(34).jpg"
        )
        val future = CompletableFuture<Long>()
        val downlaodManager = DownloadTask(urlList, cacheStorage, skipExisting = false,
            onFailed = {
                throw it
            }
            ,
            onSuccess = {
                Log.w("testDownloadManager", "onSuccess")
                val sum = urlList.map { cacheStorage.getCacheFile(it)?.length() ?: 0L }.sum()
                future.complete(sum)
            }
            ,
            onProgress = { progressList ->
                Log.w("testDownloadManager", "progressList = $progressList")
            }
        )
        downlaodManager.start()
        assertEquals(future.get(60, TimeUnit.SECONDS), 413005 + 297010 + 240075)
    }
}