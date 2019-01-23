package com.fenbi.android.ytkresourcecache

import java.io.*

/**
 * Created by yangjw on 2019/1/14.
 */
class FileResourceWriter(
    private val cacheDir: String, private val mappingRule: MappingRule
) : CacheResourceWriter {

    override fun getStream(url: String?): OutputStream? {
        if (url == null) return null
        val path = File(cacheDir, mappingRule.mapUrlToPath(url)).absolutePath
        return ResourceOutputStream(path)
    }
}