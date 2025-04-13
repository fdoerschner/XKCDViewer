package de.lexware.viewer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import de.lexware.common.Paddings

@Composable
fun ComicScreen(modifier: Modifier = Modifier) {
    InternalComicScreen(modifier)
}

@Composable
internal fun InternalComicScreen(
    modifier: Modifier = Modifier,
    viewModel: ComicViewModel = hiltViewModel(),
) {
    val viewState by viewModel.viewState.collectAsState()
    val event by viewModel.uiEvents.collectAsState()

    EventSink(event)
    Scaffold(
        modifier = modifier.safeDrawingPadding(),
        topBar = {
            ComicTopAppBar(viewState.topBar)
        },
        bottomBar = {
            when (val content = viewState.content) {
                is ComicContent.Comic -> ComicControlUi(content.controls)
                is ComicContent.LoadingError -> ComicControlUi(content.controls)
                ComicContent.Loading -> Unit
            }
        },
    ) {
        when (val content = viewState.content) {
            is ComicContent.Comic ->
                Column(modifier = Modifier.padding(it)) {
                    ComicImage(content, Modifier)
                    Text(
                        text = content.releaseDate,
                        modifier = Modifier.padding(Paddings.small),
                    )
                }

            is ComicContent.LoadingError -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(Icons.Filled.Warning, contentDescription = null)
                        Text(
                            text = "Error while loading image. Please try again later.",
                            modifier = Modifier.padding(top = Paddings.small),
                        )
                    }
                }
            }

            ComicContent.Loading ->
                Box(
                    modifier = Modifier
                        .padding(it)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.initialLoad()
    }
}

@Composable
private fun ComicControlUi(comicControls: List<ComicControl>, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        comicControls.forEach { control ->
            IconButton(control.onClick) {
                Icon(control.icon.toPainter(), contentDescription = null)
            }
        }
    }
}

@Composable
private fun ComicImage(comic: ComicContent.Comic, modifier: Modifier = Modifier) {
    val longPress by rememberUpdatedState(comic.onLongPress)
    var scale by remember(comic) {
        mutableFloatStateOf(1.0f)
    }
    var offset by remember(comic) { mutableStateOf(Offset(0f, 0f)) }
    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        scale *= zoomChange
        offset += panChange
    }
    AsyncImage(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offset.x,
                translationY = offset.y,
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { longPress() },
                )
            }
            .transformable(transformState),
        placeholder = comic.placeHolderImage.toPainter(),
        model = comic.comicUrl,
        contentDescription = null,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ComicTopAppBar(topBar: TopBarConfig) {
    TopAppBar(
        title = {
            Text(
                text = topBar.title,
            )
        },
        actions = {
            topBar.contextIcons.forEach { contextButton ->
                IconButton(contextButton.onClick) {
                    Icon(contextButton.icon.toPainter(), contentDescription = null)
                }
            }
        },
    )
}

@Composable
private fun EventSink(event: UiEvent) {
    when (event) {
        is UiEvent.LongPressEvent -> LongPressToast(event)
        UiEvent.NoEvent -> Unit
    }
}

@Composable
private fun LongPressToast(
    longPressEvent: UiEvent.LongPressEvent,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = longPressEvent.onDismiss)
            .zIndex(2f),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Surface(
            modifier = Modifier
                .padding(bottom = Paddings.huge, start = Paddings.small, end = Paddings.small),
            shape = MaterialTheme.shapes.large,
            shadowElevation = Paddings.small,
        ) {
            Row(
                modifier = Modifier
                    .padding(Paddings.default)
                    .fillMaxWidth()
                    .padding(Paddings.small),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = longPressEvent.altText,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )
                IconButton(longPressEvent.onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = null)
                }
            }
        }
    }
}
