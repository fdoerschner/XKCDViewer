package de.lexware.fetcher

import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.random.Random

internal class ComicLoaderImpl(
    private val service: XKCDApiService,
    private val singletonValues: ApiSingletonValues,
) : ComicLoader {
    override suspend fun loadComic(comicType: ComicLoader.ComicType): ApiComic {
        return when (comicType) {
            ComicLoader.ComicType.Newest -> loadNewest()
            ComicLoader.ComicType.Random -> loadRandom()
            is ComicLoader.ComicType.WithId -> loadForId(comicType.id)
        }
    }

    private suspend fun loadNewest(): ApiComic {
        return service.loadNewestComic().also {
            singletonValues.newestComicId = it.num
        }.toApiComic()
    }

    private suspend fun loadRandom(): ApiComic {
        // as of writing this code, 3075 is the newest and maximum number.
        // this should not be hardcoded but should be received differently
        val maxNum = singletonValues.newestComicId ?: 3075
        val number = Random.nextInt(maxNum)

        return service.loadComic(number).toApiComic()
    }

    private suspend fun loadForId(id: Int): ApiComic = service.loadComic(id).toApiComic()

    private fun InternalApiComic.toApiComic() = ApiComic(
        url = img,
        id = num,
        altText = alt,
        title = title,
        releaseDate = Date(
            Calendar.getInstance(Locale.getDefault()).apply {
                set(Calendar.YEAR, year.toInt())
                set(Calendar.MONTH, month.toInt())
                set(Calendar.DAY_OF_MONTH, day.toInt())
            }.timeInMillis,
        ),
    )
}