package com.goldvision

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

internal actual object AppShare {
    @Composable
    actual fun rememberShareTrigger(): () -> Unit {
        val context = LocalContext.current
        return {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, appShareText)
            }
            context.startActivity(
                Intent.createChooser(sendIntent, null).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
        }
    }
}
