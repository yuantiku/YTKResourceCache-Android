package com.fenbi.android.ytkresourcecache

import android.content.Context
import java.io.File

/**
 * Created by yangjw on 2019/1/14.
 */
open class FileCacheStorage(
    private val context: Context,
    val mappingRule: MappingRule = MappingRule.Default,
    val cacheDir: String = context.cacheDir.absolutePath
) : CacheStorage {

    override val cacheReader: CacheResourceReader =
        DefaultCacheResourceReader(context, cacheDirectory = cacheDir, mappingRule = mappingRule)

    override val cacheWriter: CacheResourceWriter = FileResourceWriter(cacheDir, mappingRule)

    fun clearCache() {
        File(cacheDir).deleteRecursively()
    }

}