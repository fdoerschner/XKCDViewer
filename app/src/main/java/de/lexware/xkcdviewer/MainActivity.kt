package de.lexware.xkcdviewer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import de.lexware.viewer.ComicScreen
import de.lexware.xkcdviewer.ui.theme.XKCDViewerTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            XKCDViewerTheme {
                ComicScreen()
            }
        }
    }
}
