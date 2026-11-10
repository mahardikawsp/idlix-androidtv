package com.idlix.mobile

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.idlix.data.api.ApiClient
import com.idlix.data.local.PlayerAppearanceStore
import kotlinx.coroutines.Dispatchers

class IdlixMobileApp : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()
        appContext = this
        ApiClient.init(this, "IdlixMobile-Client/1.0 (Android)")
        PlayerAppearanceStore.init(this)
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .okHttpClient(ApiClient.okHttpClient)
            .allowRgb565(true)
            .respectCacheHeaders(false)
            .decoderDispatcher(Dispatchers.IO)
            .fetcherDispatcher(Dispatchers.IO)
            .transformationDispatcher(Dispatchers.IO)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(150L * 1024 * 1024)
                    .build()
            }
            .crossfade(true)
            .build()
    }

    companion object {
        lateinit var appContext: Application
            private set
    }
}
