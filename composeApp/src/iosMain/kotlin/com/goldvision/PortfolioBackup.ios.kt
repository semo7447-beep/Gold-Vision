package com.goldvision

import androidx.compose.runtime.Composable

// النسخ الاحتياطي (تصدير/استيراد) غير مفعّل بعد على iOS — بلا تأثير حالياً
internal actual object PortfolioBackupExport {
    actual fun exportBackup(jsonContent: String) {
    }
}

internal actual object PortfolioBackupImport {
    @Composable
    actual fun rememberLauncher(onResult: (jsonContent: String?, error: String?) -> Unit): () -> Unit {
        return { onResult(null, null) }
    }
}
