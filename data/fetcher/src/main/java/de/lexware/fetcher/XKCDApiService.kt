package de.lexware.fetcher

import retrofit2.http.GET
import retrofit2.http.Path

internal interface XKCDApiService {
    @GET("/info.0.json")
    suspend fun loadNewestComic(): InternalApiComic

    @GET("/{num}/info.0.json")
    suspend fun loadComic(
        @Path("num") id: Int,
    ): InternalApiComic
}