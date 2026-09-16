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

// مجموعة الألوان الفعلية لكل وضع — GoldDark/Green/Red/Yellow تبقى ثابتة
// بين الوضعين (ألوان دلالية عالمية: نجاح/خطر...)، أما gold (لون التمييز
// الأساسي بكل الشاشات: أسعار، حدود، تبويبات محددة...) وألوان الشريط
// العلوي فتتغيّر فعلياً بين داكن وفاتح — بناءً على طلب صريح: بالوضع
// الفاتح لا ذهبي إطلاقاً، الشريط العلوي كحلي غامق بمحتوى أبيض، وبقية
// التطبيق أبيض ودرجات رمادي فقط (الكحلي نفسه يحل محل الذهبي كلون تمييز
// موحّد بدل التشتت بين أكثر من لون تمييز)
internal data class AppColorScheme(
    val background: Color,
    val card: Color,
    val gold: Color,
    val text: Color,
    val textSecondary: Color,
    val border: Color,
    val headerBackground: Color,
    val headerContent: Color
)

private val DarkAppColors = AppColorScheme(
    background = Color(0xFF050505),
    card = Color(0xFF090909),
    gold = Color(0xFFFFC21A),
    text = Color(0xFFF4F4F4),
    textSecondary = Color(0xFFB8B8B8),
    border = Color(0xFF9B7300),
    // نفس خلفية الصفحة (بلا تغيير عن الشكل الأصلي غير المشتكى منه):
    // الشريط العلوي يندمج بصرياً مع بقية الصفحة الداكنة كما كان دائماً
    headerBackground = Color(0xFF050505),
    headerContent = Color(0xFFFFC21A)
)

// أبيض ورمادي فاتح بناءً على طلب صريح — بلا حدود ذهبية داكنة (تُستبدل
// بحد رمادي فاتح محايد يناسب الخلفية البيضاء بدل تباين قوي وغير مريح).
// gold أصبحت كحلياً غامقاً (لا ذهبي إطلاقاً بالوضع الفاتح)، يبقى مقروءاً
// كنص/حدّ فوق الأبيض بدل التلاشي الذي لاحظه المستخدم فعلياً
private val LightAppColors = AppColorScheme(
    background = Color(0xFFFFFFFF),
    card = Color(0xFFF1F1F3),
    gold = Color(0xFF16213E),
    text = Color(0xFF1A1A1A),
    textSecondary = Color(0xFF6E6E6E),
    border = Color(0xFFE2E2E5),
    // شريط علوي كحلي غامق مميّز عن خلفية الصفحة البيضاء، بمحتوى أبيض
    // ثابت (لا يتبع لون النص العام، الذي يصبح غامقاً بالوضع الفاتح
    // وسيختفي فوق خلفية كحلية غامقة)
    headerBackground = Color(0xFF16213E),
    headerContent = Color(0xFFFFFFFF)
)

internal val LocalAppColors = compositionLocalOf { DarkAppColors }

internal val currentAppColors: AppColorScheme
    get() = if (AppTheme.mode == AppThemeMode.LIGHT) LightAppColors else DarkAppColors
