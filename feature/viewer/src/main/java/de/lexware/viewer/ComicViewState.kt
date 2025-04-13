package de.lexware.viewer

import de.lexware.common.UiIcon

internal data class ComicViewState(
    val topBar: TopBarConfig,
    val content: ComicContent,
)

internal sealed interface ComicContent {
    data object Loading : ComicContent

    data class Comic(
        val comicUrl: String,
        val releaseDate: String,
        val placeHolderImage: UiIcon = UiIcon.DrawableIcon(id = R.drawable.gallery),
        val controls: List<ComicControl>,
        val onLongPress: () -> Unit,
    ) : ComicContent

    data class LoadingError(
        val controls: List<ComicControl>,
    ) : ComicContent
}

internal data class ComicControl(
    val icon: UiIcon,
    val onClick: () -> Unit,
)

internal data class TopBarConfig(
    val title: String,
    val contextIcons: List<ContextMenuButton> = listOf(),
    val onBack: (() -> Unit)? = null,
)

internal data class ContextMenuButton(
    val icon: UiIcon,
    val onClick: () -> Unit,
)

internal sealed interface UiEvent {
    data object NoEvent : UiEvent

    data class LongPressEvent(val onDismiss: () -> Unit, val altText: String) : UiEvent
}
