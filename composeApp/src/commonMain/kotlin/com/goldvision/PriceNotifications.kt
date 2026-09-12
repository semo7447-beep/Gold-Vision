package com.goldvision

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// تفضيل تفعيل/تعطيل إشعار سعر الذهب اليومي (افتتاح/إغلاق عيار 24 و21)،
// محفوظ محلياً فقط على الجهاز. مُعطَّل افتراضياً حتى يفعّله المستخدم
// بنفسه من شاشة "الإشعارات" في "المزيد" — لا نطلب صلاحية الإشعارات من
// النظام قبل أن يطلبها هو بنفسه
@Serializable
internal data class NotificationSettings(val dailyPriceEnabled: Boolean = false)

private const val notificationSettingsStorageFile = "notification_settings.json"

internal fun loadNotificationSettings(): NotificationSettings {
    val text = AppStorage.readText(notificationSettingsStorageFile) ?: return NotificationSettings()
    return try {
        Json.decodeFromString<NotificationSettings>(text)
    } catch (e: Exception) {
        NotificationSettings()
    }
}

internal fun persistNotificationSettings(settings: NotificationSettings) {
    AppStorage.writeText(notificationSettingsStorageFile, Json.encodeToString(settings))
}

// يجدول أو يلغي إشعاراً يومياً واحداً بسعري الافتتاح والإغلاق (عيار 24
// و21)، بناءً على آخر شمعة يومية حقيقية متوفرة من GoldHistory. التطبيق
// الفعلي مختلف لكل منصة (WorkManager على أندرويد؛ لا تأثير على iOS بعد)
internal expect object PriceNotificationScheduler {
    fun setEnabled(enabled: Boolean)
}

// نص الإشعار (عنوان + محتوى) يعرض سعري الافتتاح والإغلاق الفعليين
// لعيار 24 وعيار 21 بالريال للجرام، من آخر شمعة يومية حقيقية متوفرة —
// أو null إن لم تتوفر بيانات كافية بعد (أول تشغيل قبل أي جلب ناجح)
internal fun buildDailyPriceNotificationText(bars: List<HistoryBar>): Pair<String, String>? {
    val latest = bars.maxByOrNull { it.date } ?: return null
    val open24 = usdPerOunceToSarPerGram(latest.open, "24K")
    val close24 = usdPerOunceToSarPerGram(latest.close, "24K")
    val open21 = usdPerOunceToSarPerGram(latest.open, "21K")
    val close21 = usdPerOunceToSarPerGram(latest.close, "21K")
    val title = "أسعار الذهب اليوم"
    val body = "عيار 24: افتتاح ${fmt(open24, 2, grouped = true)} - إغلاق ${fmt(close24, 2, grouped = true)} ريال\n" +
        "عيار 21: افتتاح ${fmt(open21, 2, grouped = true)} - إغلاق ${fmt(close21, 2, grouped = true)} ريال"
    return title to body
}
