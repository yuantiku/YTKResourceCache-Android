# YTKResourceCache-Android

A cache loader that works with [YTKWebView](https://github.com/yuantiku/YTKWebView-Android). You can also use it without YTKWebView to load the cache resources separately.

## Usage

```kotlin
val cacheLoader = DefaultCacheResourceLoader(
    context,
    assetsDirectory = "cache",
    cacheDirectory = getCacheDirectory(),
    mappingRule = CustomMappingRule()
)

val cacheWriter = DefaultCacheResourceWriter(){
    context,
    memCacheSize = 30*1024*1024,
    diskCacheSize = 200*1024*1024,
    diskCacheDir = context.getCacheDir().getAbsolutePath(),
    mappingRule = CustomMappingRule(),
    cacheStrategy = CacheStrategy.LRU
}

val cacheStorage = CacheStorage(
    cacheResourceLoder = cacheLoader,
    cacheResourceWriter = cacheWriter
)

YTKWebView(context)
    .setCache(cacheStorage)
    .attach(webView)
```

You can also create your own cache loader and writer by implementing `CacheResourceLoader`  or `CacheResourceWriter` interface.

```kotlin
interface CacheResourceLoader {
    fun getCachedResourceStream(url: String): InputStream?
}

interface CacheResourceWriter{
    fun writeResourceToCache(url: String, stream: InputStream)
}
```

The `DefaultCacheResourceLoader` first looks up in the assets directory, then in the local cache directory, by a specific mapping rule that maps remote urls to local file paths.  

The `DefaultCacheResourceWriter`  will keep a copy of data in both memory and disk file, it use LRU(least recently used) as default strategy to evict cache when cache size is full. You can change the cache strategy by explicitly assigning the cacheStrategy parameter in CacheWriter's constructor.
