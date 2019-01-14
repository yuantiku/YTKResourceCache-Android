package com.fenbi.android.ytkresourcecache

import java.io.InputStream

/**
 * @author zheng on 12/24/18
 */

interface CacheResourceReader {

    fun getStream(url: String): InputStream?
}

fun ((String) -> InputStream?).asCacheResourceReader(): CacheResourceReader {
    return object : CacheResourceReader {
        override fun getStream(url: String): InputStream? =
            this@asCacheResourceReader(url)
    }
}
