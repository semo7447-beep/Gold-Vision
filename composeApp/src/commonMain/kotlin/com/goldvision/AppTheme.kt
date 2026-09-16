package com.goldvision

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// وضع العرض (داكن/فاتح) — بنفس أسلوب AppLanguage تماماً (Localization.kt):
// حالة عامة محفوظة محلياً، تُقرأ عند إقلاع التطبيق وتبقى بعد أي تغيير
internal enum class AppThemeMode { DARK, LIGHT }

@Serializable
internal data class ThemeSettings(val mode: AppThemeMode = AppThemeMode.DARK)

private const val themeSettingsStorageFile = "theme_settings.json"

internal fun loadThemeSettings(): ThemeSettings {
    val text = AppStorage.readText(themeSettingsStorageFile) ?: return ThemeSettings()
    return try {
        Json.decodeFromString<ThemeSettings>(text)
    } catch (e: Exception) {
        ThemeSettings()
    }
}

internal fun persistThemeSettings(settings: ThemeSettings) {
    AppStorage.writeText(themeSettingsStorageFile, Json.encodeToString(settings))
}

internal object AppTheme {
    var mode by mutableStateOf(loadThemeSettings().mode)
        private set

    fun set(newMode: AppThemeMode) {
        if (mode == newMode) return
        mode = newMode
        persistThemeSettings(ThemeSettings(newMode))
    }
}

// مجموعة الألوان الفعلية لكل وضع — Gold/GoldDark/Green/Red/Yellow تبقى
// ثابتة بين الوضعين (ألوان علامة تجارية ودلالية عالمية: نجاح/خطر...)،
// فقط الخلفية والبطاقات والنص والحدود تتغيّر فعلياً بين داكن وفاتح
internal data class AppColorScheme(
    val background: Color,
    val card: Color,
    val text: Color,
    val textSecondary: Color,
    val border: Color
)

private val DarkAppColors = AppColorScheme(
    background = Color(0xFF050505),
    card = Color(0xFF090909),
    text = Color(0xFFF4F4F4),
    textSecondary = Color(0xFFB8B8B8),
    border = Color(0xFF9B7300)
)

// أبيض ورمادي فاتح بناءً على طلب صريح — بلا حدود ذهبية داكنة (تُستبدل
// بحد رمادي فاتح محايد يناسب الخلفية البيضاء بدل تباين قوي وغير مريح)
private val LightAppColors = AppColorScheme(
    background = Color(0xFFFFFFFF),
    card = Color(0xFFF1F1F3),
    text = Color(0xFF1A1A1A),
    textSecondary = Color(0xFF6E6E6E),
    border = Color(0xFFE2E2E5)
)

internal val LocalAppColors = compositionLocalOf { DarkAppColors }

internal val currentAppColors: AppColorScheme
    get() = if (AppTheme.mode == AppThemeMode.LIGHT) LightAppColors else DarkAppColors
