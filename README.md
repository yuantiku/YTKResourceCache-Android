# YTKResourceCache-Android

A cache loader that works with
[YTKWebView](https://github.com/yuantiku/YTKWebView-Android). You can also use
it without YTKWebView to load the cache resources separately.

## Usage

The `CacheResourceLoader` interface provides cache loading functionality for
YTKWebView, it's declared as follows,

```kotlin
interface CacheResourceLoader {
  fun getCachedResourceStream(url: String?): InputStream?
}
```

You can create your own `CacheResourceLoader`, or use our default implementation
instead. To create a default `CacheResourceLoader`,

```kotlin
val cacheResourceLoader = DefaultCacheResourceLoader.Builder(context)
  .assetsDirectory("cache")
  .cacheDirectory(getCacheDirectory())
  .mappingRule(CustomMappingRule())
  .build()
```

The `DefaultCacheResourceLoader` first looks up in the assets directory, then in
the local cache directory, by a specific mapping rule that maps remote urls to
local file paths.

To specify the CacheResourceLoader in YTKWebView

```kotlin
YTKWebView(context)
  .setCacheLoader(cacheResourceLoader)
  .attach(webView)
```
