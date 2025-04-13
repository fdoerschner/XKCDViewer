package de.lexware.common

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource

sealed class UiIcon {
    @Composable
    fun toPainter() = when (this) {
        is ComposeIcon -> rememberVectorPainter(this.vector)
        is DrawableIcon -> painterResource(this.id)
    }

    data class DrawableIcon(@DrawableRes val id: Int) : UiIcon()

    data class ComposeIcon(val vector: ImageVector) : UiIcon()
}