package de.lexware.viewer

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lexware.common.UiIcon
import de.lexware.fetcher.ApiComic
import de.lexware.fetcher.ApiSingletonValues
import de.lexware.fetcher.ComicLoader
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
internal class ComicViewModel @Inject constructor(
    private val loader: ComicLoader,
    private val singletonValues: ApiSingletonValues,
    private val comicScreenRequester: ComicScreenRequester,
) : ViewModel() {
    private val internalComicFlow = MutableStateFlow<Result<ApiComic>?>(null)
    private val internalEventFlow = MutableStateFlow<UiEvent>(UiEvent.NoEvent)
    private val internalTTSProgress = MutableStateFlow(ComicScreenRequester.TTSProgress.Stopped)

    private val dateFormat = SimpleDateFormat.getDateInstance()
    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        internalComicFlow.value = Result.failure(throwable)
    }
    private val comicFlow = internalComicFlow.map { it?.getOrNull() }.filterNotNull()
    private val topBarConfig = combine(
        comicFlow,
        internalTTSProgress,
    ) { comic, ttsProgress ->
        TopBarConfig(
            title = comic.let { "${it.title} #${it.id}" },
            contextIcons = listOfNotNull(
                comic.transcript
                    ?.takeIf {
                        it.isNotBlank() && ttsProgress == ComicScreenRequester.TTSProgress.Stopped
                    }?.let { transcript ->
                        ContextMenuButton(
                            icon = UiIcon.ComposeIcon(Icons.Outlined.PlayArrow),
                            onClick = {
                                comicScreenRequester.onTextToSpeech(transcript, ::updateTtsProgress)
                            },
                        )
                    },
                ContextMenuButton(
                    icon = UiIcon.DrawableIcon(id = R.drawable.pause),
                    onClick = {
                        comicScreenRequester.stopTextToSpeech()
                        updateTtsProgress(ComicScreenRequester.TTSProgress.Stopped)
                    },
                ).takeIf { ttsProgress == ComicScreenRequester.TTSProgress.Running }
            ),
        )
    }

    val uiEvents: StateFlow<UiEvent> = internalEventFlow
    val viewState: StateFlow<ComicViewState> = combine(
        topBarConfig,
        internalComicFlow.filterNotNull(),
    ) { topBar, result ->
        val comic = result.getOrNull()
        val content = if (result.isFailure || comic == null) {
            ComicContent.LoadingError(createComicControls(null))
        } else {
            ComicContent.Comic(
                comicUrl = comic.url,
                releaseDate = dateFormat.format(comic.releaseDate),
                controls = createComicControls(comic),
                onLongPress = {
                    internalEventFlow.value = UiEvent.LongPressEvent(
                        onDismiss = {
                            internalEventFlow.value = UiEvent.NoEvent
                        },
                        altText = comic.altText,
                    )
                    startAndResetEvent()
                },
            )
        }

        ComicViewState(
            topBar = topBar,
            content = content,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        ComicViewState(TopBarConfig(""), ComicContent.Loading),
    )

    fun initialLoad() {
        loadNewest()
    }

    private fun startAndResetEvent() {
        viewModelScope.launch {
            delay(eventResetDuration)
            internalEventFlow.value = UiEvent.NoEvent
        }
    }

    private fun createComicControls(comic: ApiComic?) = listOfNotNull(
        ComicControl(
            icon = UiIcon.DrawableIcon(id = R.drawable.skip_previous),
            onClick = { loadWithId(1) },
        ),
        comic?.takeIf { comic.id > 1 }?.let {
            ComicControl(
                icon = UiIcon.ComposeIcon(Icons.AutoMirrored.Default.ArrowBack),
                onClick = { loadWithId(comic.id - 1) },
            )
        },
        ComicControl(
            icon = UiIcon.DrawableIcon(id = R.drawable.question_mark),
            onClick = ::loadRandom,
        ),
        comic?.takeIf { comic.id < (singletonValues.newestComicId ?: 0) }?.let {
            ComicControl(
                icon = UiIcon.ComposeIcon(Icons.AutoMirrored.Default.ArrowForward),
                onClick = { loadWithId(comic.id + 1) },
            )
        },
        ComicControl(
            icon = UiIcon.DrawableIcon(id = R.drawable.skip_next),
            onClick = ::loadNewest,
        ),
    )

    private fun updateTtsProgress(ttsProgress: ComicScreenRequester.TTSProgress) {
        internalTTSProgress.value = ttsProgress
    }

    private fun loadNewest() {
        viewModelScope.launch(coroutineExceptionHandler) {
            internalComicFlow.value = Result.success(loader.loadComic(ComicLoader.ComicType.Newest))
        }
    }

    private fun loadRandom() {
        viewModelScope.launch(coroutineExceptionHandler) {
            internalComicFlow.value = Result.success(loader.loadComic(ComicLoader.ComicType.Random))
        }
    }

    private fun loadWithId(id: Int) {
        viewModelScope.launch(coroutineExceptionHandler) {
            internalComicFlow.value =
                Result.success(loader.loadComic(ComicLoader.ComicType.WithId(id)))
        }
    }

    companion object {
        private val eventResetDuration = 3.seconds
    }
}