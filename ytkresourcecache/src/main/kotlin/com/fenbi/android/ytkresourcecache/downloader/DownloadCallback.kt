package com.fenbi.android.ytkresourcecache.downloader

/**
 * Created by yangjw on 2019/1/22.
 */
class DownloadCallback {
    var onSuccess: (() -> Unit)? = null

    var onFailed: ((e: Throwable) -> Unit)? = null

    var onCanceled: (() -> Unit)? = null

    var onProgress: ((loaded: Long, total: Long) -> Unit)? = null
}
