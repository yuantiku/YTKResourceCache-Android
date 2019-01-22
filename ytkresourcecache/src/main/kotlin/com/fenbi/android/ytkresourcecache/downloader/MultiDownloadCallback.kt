package com.fenbi.android.ytkresourcecache.downloader

/**
 * Created by yangjw on 2019/1/22.
 */
class MultiDownloadCallback {
    var onUrlSuccess: ((url: String) -> Unit)? = null

    var onUrlFailed: ((url: String, e: Throwable) -> Unit)? = null

    var onUrlCanceled: ((url: String) -> Unit)? = null

    var onProgress: ((progress: MultiProgress) -> Unit)? = null
}