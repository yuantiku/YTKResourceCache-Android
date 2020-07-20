package com.fenbi.android.ytkresourcecache

import android.Manifest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Created by yangjw on 2019/1/21.
 */
@RunWith(AndroidJUnit4::class)
class TestCacheStorage {

    @get:Rule
    var permissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    val url = "http://www.fenbi.com/test.html"

    val content = "hello world!"

    val cacheStorage by lazy {
        FileCacheStorage(InstrumentationRegistry.getInstrumentation().context)
    }

    @Test
    fun test(){
        val outputStream = cacheStorage.cacheWriter.getStream(url)
        outputStream!!.use {
            it.write(content.toByteArray())
            outputStream.asResourceOutputStream()?.onCacheSuccess()
        }
        val inputStream = cacheStorage.cacheReader.getStream(url)
        val line = BufferedReader(InputStreamReader(inputStream)).readLine()
        assertEquals(line, content)

        val file = cacheStorage.getCacheFile(url)
        assertNotNull(file)
        assertEquals(file?.length(), content.toByteArray().size.toLong())
    }
}