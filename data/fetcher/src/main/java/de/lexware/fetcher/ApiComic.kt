package de.lexware.fetcher

import java.util.Date

data class ApiComic(
    val url: String,
    val id: Int,
    val altText: String,
    val title: String,
    val releaseDate: Date,
    val transcript: String? = null
)
