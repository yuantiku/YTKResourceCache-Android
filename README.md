# YTKResourceCache-Android

A cache library that works with [YTKWebView](https://github.com/yuantiku/YTKWebView-Android). You can also use it without YTKWebView to load the cache resources separately.

## Usage

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
    fun getStream(url: String): InputStream?
}

interface CacheResourceWriter{
    fun getStream(url: String): OutputStream?
}
```

The `DefaultCacheResourceReader` first looks up in the assets directory, then in the local cache directory, by a specific mapping rule that maps remote urls to local file paths.  
