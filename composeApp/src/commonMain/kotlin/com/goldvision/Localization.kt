package com.goldvision

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// نظام ترجمة بسيط: بدل قاموس مفاتيح منفصل (صعب المطابقة مع آلاف النصوص
// المكتوبة مباشرة داخل الواجهات)، كل نص عربي يُغلَّف مكانه بدالة t()
// التي تُرجع النسخة الإنجليزية بدلاً منه إذا كانت اللغة الحالية إنجليزية
internal enum class AppLang { AR, EN }

@Serializable
internal data class LanguageSettings(val lang: AppLang = AppLang.AR)

private const val languageSettingsStorageFile = "language_settings.json"

internal fun loadLanguageSettings(): LanguageSettings {
    val text = AppStorage.readText(languageSettingsStorageFile) ?: return LanguageSettings()
    return try {
        Json.decodeFromString<LanguageSettings>(text)
    } catch (e: Exception) {
        LanguageSettings()
    }
}

internal fun persistLanguageSettings(settings: LanguageSettings) {
    AppStorage.writeText(languageSettingsStorageFile, Json.encodeToString(settings))
}

// حالة اللغة الحالية، تُقرأ عند إقلاع التطبيق وتبقى محفوظة محلياً بعد أي تغيير
internal object AppLanguage {
    var current by mutableStateOf(loadLanguageSettings().lang)
        private set

    fun set(lang: AppLang) {
        if (current == lang) return
        current = lang
        persistLanguageSettings(LanguageSettings(lang))
    }
}

// دالة الترجمة: تُستدعى مكان أي نص عربي مباشر — t("نص عربي", "English text")
internal fun t(ar: String, en: String): String = if (AppLanguage.current == AppLang.EN) en else ar
