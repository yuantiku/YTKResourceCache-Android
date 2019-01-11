# YTKResourceCache-Android

A cache library that works with [YTKWebView](https://github.com/yuantiku/YTKWebView-Android). You can also use it without YTKWebView to load the cache resources separately.

## Usage

```kotlin
val cacheStorage = CacheStorage(
    context,
    mappingRule = DefaultMappingRule(),
    diskCacheDir = context.getCacheDir().getAbsolutePath(),
    diskCacheSize = 200*1024*1024,
    memCacheSize = 30*1024*1024,
    cacheStrategy = CacheStrategy.LRU
)

YTKWebView(context)
    .setCacheReader(cachesStorage.cacheReader)
    .attach(webView)
```

You can also create your own cache reader and writer by implementing `CacheResourceReader`  or `CacheResourceWriter` interface. Then make a subclass of CacheStorage and override cacheReader or cacheWriter.

```kotlin
interface CacheResourceReader {
    fun getCachedResourceStream(url: String): InputStream?
}

interface CacheResourceWriter{
    fun getWriteCacheStream(url: String): OutputStream
}
```

The `DefaultCacheResourceReader` first looks up in the assets directory, then in the local cache directory, by a specific mapping rule that maps remote urls to local file paths.  

The `DefaultCacheResourceWriter`  will keep a copy of data in both memory and disk file, it use LRU(least recently used) as default strategy to evict cache when cache size is full. You can change the cache strategy by explicitly assigning the cacheStrategy parameter in CacheWriter's constructor.
