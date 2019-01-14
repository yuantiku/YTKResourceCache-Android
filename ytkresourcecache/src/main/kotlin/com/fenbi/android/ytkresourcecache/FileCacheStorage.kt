package com.fenbi.android.ytkresourcecache

import android.content.Context
import android.net.Uri
import java.io.File

/**
 * Created by yangjw on 2019/1/14.
 */
open class FileCacheStorage(
    protected val context: Context,
    protected val mappingRule: MappingRule = MappingRule.Default,
    val cacheDir: String = context.cacheDir.absolutePath
) : CacheStorage {

    override val cacheReader: CacheResourceReader =
        DefaultCacheResourceReader(context, cacheDirectory = cacheDir, mappingRule = mappingRule)

    override val cacheWriter: CacheResourceWriter = FileResourceWriter(cacheDir, mappingRule)

    fun getCacheFile(url: String?): File? {
        if (url == null) {
            return null
        }

        val dir = File(cacheDir)
        if(!dir.exists()){
            dir.mkdirs()
        }
        return File(cacheDir, mappingRule.mapUrlToPath(url))
    }

}