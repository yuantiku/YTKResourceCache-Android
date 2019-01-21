package com.fenbi.android.ytkresourcecache

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Created by yangjw on 2019/1/21.
 */
@RunWith(AndroidJUnit4::class)
class TestCacheStorage {

    val url = "http://www.fenbi.com/test.html"

    val content = "hello world!"

    val cacheStorage by lazy {
        FileCacheStorage(InstrumentationRegistry.getContext())
    }

    @Test
    fun test(){
        val outputStream = cacheStorage.cacheWriter.getStream(url)
        outputStream!!.use {
            it.write(content.toByteArray())
        }
        val inputStream = cacheStorage.cacheReader.getStream(url)
        val line = BufferedReader(InputStreamReader(inputStream)).readLine()
        assertEquals(line, content)

        val file = cacheStorage.getCacheFile(url)
        assertNotNull(file)
        assertEquals(file?.length(), content.toByteArray().size.toLong())
    }
}