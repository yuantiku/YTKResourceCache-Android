package com.fenbi.android.ytkresourcecache

import android.content.Context
import java.io.File
import java.io.InputStream

/**
 * @author zheng on 12/24/18
 */

class DefaultCacheResourceReader(
    private val context: Context,
    assetsDirectory: String? = null,
    cacheDirectory: String? = null,
    mappingRule: MappingRule? = null
) : CacheResourceReader {

    private val cacheDir = if (cacheDirectory != null) {
        File(cacheDirectory)
    } else {
        File(context.filesDir, "cache")
    }

    private val innerMappingRule by lazy { mappingRule ?: MappingRule.Default }

    private val protocols = listOf("http://", "https://")

    private val String.isSupported: Boolean
        get() = protocols.any { this.startsWith(it) }

    private val innerLoaders by lazy {
        listOf(
            AssetsResourceReader(context, assetsDirectory ?: "cache", innerMappingRule),
            FileResourceReader(cacheDir, innerMappingRule))
    }

    override fun getStream(url: String): InputStream? {
        if (!url.isSupported) {
            return null
        }
        innerLoaders.forEach { loader ->
            loader.getStream(url)?.let {
                return it
            }
        }
        return null
    }
}
