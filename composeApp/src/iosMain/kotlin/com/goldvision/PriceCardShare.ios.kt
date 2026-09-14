package com.goldvision

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap

internal actual object PriceCardShare {
    @Composable
    actual fun rememberCapture(): PriceCardCapture = PriceCardCapture(modifier = Modifier, capture = { null })

    @Composable
    actual fun rememberShareImage(): (ImageBitmap) -> Unit = {}

    @Composable
    actual fun rememberSaveImage(): (ImageBitmap) -> Boolean = { false }
}
