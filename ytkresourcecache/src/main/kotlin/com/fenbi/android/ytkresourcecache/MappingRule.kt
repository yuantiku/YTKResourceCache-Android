package com.fenbi.android.ytkresourcecache

/**
 * @author zheng on 1/7/19
 */

interface MappingRule {

    fun mapUrlToPath(url: String): String

    companion object {

        internal val Default = object : MappingRule {
            override fun mapUrlToPath(url: String): String {
                return FileNameUtils.getFilePath(url)
            }
        }
    }
}
