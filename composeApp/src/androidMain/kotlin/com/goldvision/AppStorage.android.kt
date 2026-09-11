package com.goldvision

import android.content.Context
import java.io.File

// يُهيَّأ مرة واحدة من MainActivity.onCreate قبل أول استخدام (قبل
// setContent)، بسياق التطبيق (applicationContext) حتى لا يُحتفَظ بمرجع
// لأي Activity قد تُدمَّر
actual object AppStorage {
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    actual fun readText(fileName: String): String? {
        val context = appContext ?: return null
        val file = File(context.filesDir, fileName)
        return if (file.exists()) file.readText() else null
    }

    actual fun writeText(fileName: String, content: String) {
        val context = appContext ?: return
        File(context.filesDir, fileName).writeText(content)
    }
}
