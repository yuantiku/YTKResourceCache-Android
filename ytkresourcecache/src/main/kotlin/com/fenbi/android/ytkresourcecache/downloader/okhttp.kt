package com.fenbi.android.ytkresourcecache.downloader

import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import java.util.concurrent.Executors

/**
 * Created by yangjw on 2019/1/22.
 */

val defaultOkHttpClient: OkHttpClient by lazy {
    OkHttpClient.Builder()
        .dispatcher(Dispatcher(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())))
        .build()
}