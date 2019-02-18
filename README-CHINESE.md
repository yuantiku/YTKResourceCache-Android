# YTKResourceCache-Android

一个配合[YTKWebView](https://github.com/yuantiku/YTKWebView-Android)使用的缓存框架，你也可以单独使用本框架来进行资源的缓存与加载。

## 使用

### 资源缓存

```kotlin
val cacheStorage = FileCacheStorage(
    context,
    mappingRule = MappingRule.Default,
    cacheDir = context.getCacheDir().getAbsolutePath(),
)

YTKWebView(context)
    .setCacheReader(cachesStorage.cacheReader)
    .attach(webView)
```

你也可以通过实现`CacheStorage`接口并创建`CacheResourceReader`与`CacheResourceWriter`来实现你自己的CacheStorage比如`MemoryCacheStorage`。

```kotlin
interface CacheStorage{
    val cacheReader: CacheResourceReader

    val cacheWriter: CacheResourceWriter
}

interface CacheResourceReader {
    fun getStream(url: String?): InputStream?
}

interface CacheResourceWriter{
    fun getStream(url: String?): OutputStream?
}
```

`FileCacheStorage`默认使用`DefaultCacheResourceReader`来读取缓存并使用`FileResourceWriter`来写缓存。`DefaultResourceReader`首先在应用的assets目录下查找缓存，如果没找到则继续在本地存储上查找。缓存文件的匹配规则由CacheStorage的MappingRule指定，默认的是将资源URL替换为本地存储对应结构的目录下。`FileResourceWriter`使用磁盘空间来存储缓存所以请确保你的APP拥有`WRITE_EXTERNAL_STORAGE`权限。

### 资源下载

YTKResoueceCache提供了通过网络下载资源的功能，你可以像这样使用url来下载资源:

```kotlin
val urlList = listOf("http://...", "http://...")
val downloadTask = DownloadTask(urlList, cacheStorage,
    onSuccess = {

    },
    onFailed = { it:Throwable -> 

    },
    onProgress = { progressList: List<Progress> ->

    }
)
downloadTask.start()
```

取消资源下载：

```kotlin
downloadTask.cancel()
```

一旦你通过使用`DownloadTask`来从网络上下载资源，这些资源将被`CacheStorage`缓存。稍后你可以通过使用`CacheResourceReader`来从缓存中迅速获得一份资源的拷贝。
