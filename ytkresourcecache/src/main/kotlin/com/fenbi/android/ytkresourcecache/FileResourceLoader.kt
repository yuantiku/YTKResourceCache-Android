package com.fenbi.android.ytkresourcecache

import java.io.File
import java.io.FileInputStream
import java.io.InputStream

/**
 * @author zheng on 12/24/18
 */

class FileResourceLoader(
    private val directory: File,
    private val mappingRule: MappingRule
) : CacheResourceLoader {

    override fun getCachedResourceStream(url: String?): InputStream? {
        if (url == null) {
            return null
        }
        val path = mappingRule.mapUrlToPath(url)
        return try {
            FileInputStream(File(directory, path))
        } catch (e: Throwable) {
            null
        }
    }
}
