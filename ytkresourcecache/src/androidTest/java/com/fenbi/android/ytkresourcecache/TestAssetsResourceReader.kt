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
class TestAssetsResourceReader {

    @Test
    fun test() {
        val assetsReader = AssetsResourceReader(InstrumentationRegistry.getContext(), "cache",
            object : MappingRule {
                override fun mapUrlToPath(url: String): String {
                    return url
                }
            })
        val inputStream = assetsReader.getStream("helloworld.txt")
        assertNotNull(inputStream)
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val line = bufferedReader.readLine()
        assertEquals(line, "hello world!")
    }
}