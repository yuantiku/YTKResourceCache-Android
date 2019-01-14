package com.fenbi.android.ytkresourcecache

import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.OutputStream

/**
 * Created by yangjw on 2019/1/14.
 */
class FileResourceWriter(
    private val cacheDir: String, private val mappingRule: MappingRule
) : CacheResourceWriter {

    override fun getStream(url: String): OutputStream? {
        val cacheFilePath = mappingRule.mapUrlToPath(url)
        val cacheFile = File(cacheDir, cacheFilePath)
        if (!cacheFile.exists()) {
            cacheFile.parentFile?.mkdirs()
            cacheFile.createNewFile()
        }
        return try {
            FileOutputStream(cacheFile)
        } catch (e: FileNotFoundException) {
            null
        }
    }
}