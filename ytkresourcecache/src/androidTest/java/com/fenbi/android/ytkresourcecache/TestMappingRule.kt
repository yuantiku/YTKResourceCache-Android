package com.fenbi.android.ytkresourcecache

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Created by yangjw on 2019/1/21.
 */
@RunWith(AndroidJUnit4::class)
class TestFileNameUtils {

    @Test
    fun test(){
        val url = "http://www.fenbi.com/resource/1.1/test.html"
        val path = FileNameUtils.getFilePath(url)
        val extension = FileNameUtils.getExtension(url)

        assertEquals(path, "www.fenbi.com/resource/1.1/test.html")
        assertEquals(extension, "html")

    }
}