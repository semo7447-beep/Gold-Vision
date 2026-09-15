package com.goldvision

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// دالة بحتة (غير Composable) حتى تُستدعى أيضاً من MainActivity مباشرة
// عند فتحها عبر اختصار "مشاركة التطبيق" بأيقونة التطبيق
internal fun shareAppText(context: Context) {
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

internal actual object AppShare {
    @Composable
    actual fun rememberShareTrigger(): () -> Unit {
        val context = LocalContext.current
        return { shareAppText(context) }
    }
}
