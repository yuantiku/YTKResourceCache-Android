package com.fenbi.android.ytkresourcecache.downloader

/**
 * Created by yangjw on 2019/1/21.
 */
interface DownloadCallback {
    fun onSuccess()

    fun onFailed(e: Throwable)

    fun onCanceled()

    fun onProgress(loaded: Long, total: Long)
}