package com.fenbi.android.ytkresourcecache

import java.io.File
import java.io.FileInputStream
import java.io.InputStream

/**
 * @author zheng on 12/24/18
 */

class FileResourceReader(
    private val directory: File,
    private val mappingRule: MappingRule
) : CacheResourceReader {

    override fun getStream(url: String): InputStream? {
        val path = mappingRule.mapUrlToPath(url)
        return try {
            FileInputStream(File(directory, path))
        } catch (e: Throwable) {
            null
        }
    }
}
