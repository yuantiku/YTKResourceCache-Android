package com.fenbi.android.ytkresourcecache

import java.io.OutputStream

/**
 * Created by yangjw on 2019/1/14.
 */
interface CacheResourceWriter {
    fun getStream(url: String): OutputStream?
}