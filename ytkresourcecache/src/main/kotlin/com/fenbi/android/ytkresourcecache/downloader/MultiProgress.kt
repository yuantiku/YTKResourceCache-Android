package com.fenbi.android.ytkresourcecache.downloader

/**
 * Created by yangjw on 2019/1/22.
 */
data class MultiProgress(val urlList: List<String>, val progressMap: HashMap<String, Pair<Long, Long>>)