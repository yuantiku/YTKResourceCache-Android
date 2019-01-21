# YTKResourceCache-Android

A cache library that works with [YTKWebView](https://github.com/yuantiku/YTKWebView-Android). You can also use it without YTKWebView to load the cache resources separately.

## Usage

### Resource cache ability

```kotlin
val cacheStorage = FileCacheStorage(
    context,
    mappingRule = DefaultMappingRule(),
    cacheDir = context.getCacheDir().getAbsolutePath(),
)

YTKWebView(context)
    .setCacheReader(cachesStorage.cacheReader)
    .attach(webView)
```

You can also create your own cache storage like `MemoryCacheStorage` by implementing `CacheStorage` interface and then create your own `CacheResourceReader` and `CacheResourceWriter`.

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

`FileCacheStorage`  use `DefaultCacheResourceReader`  for reading cache and `FileResourceWriter`  for writing cache. The `DefaultCacheResourceReader` first looks up in the assets directory, then in the local cache directory, by a specific mapping rule that maps remote urls to local file paths.  The `FileResourceWriter` use disk file to store cache so make sure your application has `WRITE_EXTERNAL_STORAGE` permission.



### Resource download ability

YTKResourceCache offers you the ability to download resource via internet, you can download file by url like this:

```kotlin
val url = "http://..."     // A url points to some resource
val resourceDownloader = ResourceDownloader(
            cacheWriter = cacheStorage.cacheWriter,
            threadPool = Executors.newFixedThreadPool(4),
            connectTimeout = 10*1000)
resourceDownloader.download(url, object : DownloadCallback{
    override fun onSuccess() {
        
    }
    
    override fun onFailed() {
        
    }
    
    override fun onProgress(loaded: Long, total: Long) {
        
    }
})
```

For mutiple resources at once:

```kotlin
resourceDownloader.download(urlList: List<String>, object : MutipleDownloadsCallback{
    override fun onUrlSuccess(url: String) {
        
    }
    
    override fun onUrlFailed(url: String) {
        
    }
    
    override fun onProgress(progress: MutipleProgress) {   
        
    }
})
```

Once you use `ResourceDownloader` to download resources from internet, the resource is cached by `CacheWriter`. You can later use `CacheReader`  to quickly get a copy of the resource from cache.
