package com.fenbi.android.ytkresourcecache

/**
 * Created by yangjw on 2019/1/14.
 */
interface CacheStorage {
    val cacheReader: CacheResourceReader

    val cacheWriter: CacheResourceWriter
}