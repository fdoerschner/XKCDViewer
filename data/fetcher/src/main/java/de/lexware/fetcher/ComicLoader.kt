package de.lexware.fetcher

interface ComicLoader {
    suspend fun loadComic(type: ComicType): ApiComic

    sealed interface ComicType {
        data object Random : ComicType

        data object Newest : ComicType

        data class WithId(val id: Int) : ComicType
    }
}