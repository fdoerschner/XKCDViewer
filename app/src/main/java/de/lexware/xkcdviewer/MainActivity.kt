package de.lexware.xkcdviewer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import de.lexware.reader.ComicTextToSpeech
import de.lexware.viewer.ComicScreen
import de.lexware.xkcdviewer.ui.theme.XKCDViewerTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var comicTextToSpeech: ComicTextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        lifecycle.addObserver(comicTextToSpeech)
        setContent {
            XKCDViewerTheme {
                ComicScreen()
            }
        }
    }
}
