package com.goldvision

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// تفضيل قفل التطبيق ببصمة/وجه عند كل فتح — محفوظ محلياً فقط، معطَّل
// افتراضياً حتى يفعّله المستخدم بنفسه من الملف الشخصي
@Serializable
internal data class AppLockSettings(val enabled: Boolean = false)

private const val appLockSettingsStorageFile = "app_lock_settings.json"

internal fun loadAppLockSettings(): AppLockSettings {
    val text = AppStorage.readText(appLockSettingsStorageFile) ?: return AppLockSettings()
    return try {
        Json.decodeFromString<AppLockSettings>(text)
    } catch (e: Exception) {
        AppLockSettings()
    }
}

internal fun persistAppLockSettings(settings: AppLockSettings) {
    AppStorage.writeText(appLockSettingsStorageFile, Json.encodeToString(settings))
}
