package de.lexware.fetcher

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InternalApiComic(
    @SerialName("month") val month: String,
    @SerialName("num") val num: Int,
    @SerialName("link") val link: String,
    @SerialName("year") val year: String,
    @SerialName("news") val news: String,
    @SerialName("transcript") val transcript: String,
    @SerialName("alt") val alt: String,
    @SerialName("img") val img: String,
    @SerialName("title") val title: String,
    @SerialName("safe_title") val safeTitle: String,
    @SerialName("day") val day: String,
)
