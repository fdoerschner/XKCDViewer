package de.lexware.fetcher

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
@Suppress("UndocumentedPublicFunction", "UndocumentedPublicClass")
object Module {
    @Provides
    internal fun provideRetrofit(): Retrofit {
        val contentType = "application/json".toMediaType()
        val json = Json {
            ignoreUnknownKeys = true
        }
        return Retrofit.Builder()
            .baseUrl("https://xkcd.com/")
            .addConverterFactory(
                json.asConverterFactory(contentType),
            )
            .build()
    }

    @Provides
    internal fun provideComicService(retrofit: Retrofit) =
        retrofit.create(XKCDApiService::class.java)

    @Provides
    internal fun provideComicLoader(
        service: XKCDApiService,
        singletonValues: ApiSingletonValues,
    ): ComicLoader = ComicLoaderImpl(service, singletonValues)

    @Provides
    @Singleton
    fun provideComicSingletons() = ApiSingletonValues(null)
}