package com.fenbi.android.ytkresourcecache.downloader

/**
 * Created by yangjw on 2019/1/21.
 */
interface MutipleDownloadsCallback {

    fun onUrlSuccess(url: String)

    fun onUrlFailed(url: String,e: Throwable)

    fun onUrlCanceled(url: String)

    fun onProgress(progress: MutipleProgress)
}