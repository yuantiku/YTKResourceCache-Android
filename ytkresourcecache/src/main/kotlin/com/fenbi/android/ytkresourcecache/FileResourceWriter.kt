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
        val cacheFilePath = mappingRule.mapUrlToPath(url)
        val cacheFile = File(cacheDir, cacheFilePath)
        try {
            if (cacheFile.exists()) {
                cacheFile.delete()
            }
            cacheFile.parentFile?.mkdirs()
            cacheFile.createNewFile()
            return FileOutputStream(cacheFile)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }
}