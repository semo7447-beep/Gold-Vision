package com.goldvision

internal actual object AppShare {
    @androidx.compose.runtime.Composable
    actual fun rememberShareTrigger(): () -> Unit = {}
}
