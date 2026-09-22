package com.goldvision

import kotlinx.datetime.daysUntil
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// تفضيل تفعيل/تعطيل إشعار سعر الذهب اليومي (افتتاح/إغلاق عيار 24 و21)،
// محفوظ محلياً فقط على الجهاز. مُعطَّل افتراضياً حتى يفعّله المستخدم
// بنفسه من شاشة "الإشعارات" في "المزيد" — لا نطلب صلاحية الإشعارات من
// النظام قبل أن يطلبها هو بنفسه
@Serializable
internal data class NotificationSettings(
    val dailyPriceEnabled: Boolean = false,
    val fedMeetingAlertsEnabled: Boolean = false,
    val newsAlertsEnabled: Boolean = false
)

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

// يجدول أو يلغي إشعاراً كل ساعة بسعري الافتتاح والإغلاق (عيار 24
// و21)، بناءً على آخر شمعة يومية حقيقية متوفرة من GoldHistory. التطبيق
// الفعلي مختلف لكل منصة (WorkManager على أندرويد؛ لا تأثير على iOS بعد)
internal expect object PriceNotificationScheduler {
    fun setEnabled(enabled: Boolean)
}

// يجدول أو يلغي فحصاً يومياً لأقرب اجتماع فيدرالي: إن كان بعد أسبوع أو
// غداً أو اليوم، يُصدر إشعار تذكير بالتاريخ والوقت المتوقع للإعلان —
// وليس كل يوم بينهما. التطبيق الفعلي مختلف لكل منصة (WorkManager على
// أندرويد؛ لا تأثير على iOS بعد)
internal expect object FedMeetingNotificationScheduler {
    fun setEnabled(enabled: Boolean)
}

// يجدول أو يلغي فحصاً دورياً كل 30 دقيقة لأحدث خبر عن الذهب (GoldNews)،
// يُصدر إشعاراً فقط عند وجود خبر جديد فعلاً (غير الخبر الذي أُشعر به
// آخر مرة) بدل تكرار نفس الخبر كل دورة. التطبيق الفعلي مختلف لكل منصة
// (WorkManager على أندرويد؛ لا تأثير على iOS بعد)
internal expect object GoldNewsNotificationScheduler {
    fun setEnabled(enabled: Boolean)
}

// نص إشعار تذكير بأقرب موعد اقتصادي مهم (عنوان + محتوى)، أو null إن لم
// يكن أي موعد قادم بعد أسبوع بالضبط أو غداً أو اليوم (لا داعي لإشعار في
// أي يوم آخر بينهما). يفحص اجتماع الفيدرالي أولاً (منطقه الخاص القائم
// أصلاً)، ثم بقية المؤشرات الاقتصادية (NFP/CPI/Core PCE/GDP) بنفس
// القاعدة — أول موعد مطابق فقط يُشعِر به، تفادياً لتكديس أكثر من
// إشعار بنفس اليوم لو تصادف موعدان معاً
private fun reminderWhenText(daysLeft: Int): String = when (daysLeft) {
    0 -> "اليوم"
    1 -> "غداً"
    else -> "بعد أسبوع"
}

internal fun buildFedMeetingNotificationText(): Pair<String, String>? {
    nextFedMeetingDate()?.let { date ->
        val daysLeft = todayLocalDate().daysUntil(date)
        if (daysLeft == 0 || daysLeft == 1 || daysLeft == 7) {
            val title = "اجتماع الفيدرالي ${reminderWhenText(daysLeft)}"
            val body = "قرار الفائدة الأمريكية يُعلن عادة الساعة 9:00 مساءً بتوقيت مكة المكرمة"
            return title to body
        }
    }

    listOf(EconomicEventTab.NFP, EconomicEventTab.CPI, EconomicEventTab.CORE_PCE, EconomicEventTab.GDP).forEach { tab ->
        val row = upcomingEconomicEvents(tab).firstOrNull()
        if (row != null && (row.daysLeft == 0 || row.daysLeft == 1 || row.daysLeft == 7)) {
            val title = "موعد ${tab.label} ${reminderWhenText(row.daysLeft)}"
            val body = "الإصدار الرسمي: ${row.date} الساعة ${row.time} بتوقيت مكة المكرمة"
            return title to body
        }
    }

    return null
}

private const val lastNotifiedNewsStorageFile = "last_notified_news.txt"

// نص إشعار أحدث خبر مؤثر على الذهب (عنوان + محتوى)، أو null إن لم يتغيّر
// أحدث خبر عن آخر خبر أُشعر به فعلاً (بمقارنة الرابط، أو العنوان إن لم
// يتوفر رابط)، أو لا توجد أخبار بعد. يحفظ معرّف الخبر الجديد فور بنائه
// حتى لا يتكرر نفس الإشعار كل دورة فحص (كل 30 دقيقة)
internal fun buildGoldNewsNotificationText(articles: List<GoldNewsArticle>): Pair<String, String>? {
    val latest = articles.firstOrNull() ?: return null
    val key = latest.link.ifBlank { latest.title }
    if (key == AppStorage.readText(lastNotifiedNewsStorageFile)) return null
    AppStorage.writeText(lastNotifiedNewsStorageFile, key)
    return "خبر مؤثر على الذهب" to latest.title
}

// نص الإشعار (عنوان + محتوى) يعرض السعر الحالي (الحي) للجرام لعيار 24
// وعيار 21 بالريال، من آخر سعر ناجح متوفر بـ GoldMarket — أو null إن لم
// يتوفر سعر لأحد العيارين (أول تشغيل قبل أي جلب ناجح)
internal fun buildDailyPriceNotificationText(prices: List<KaratPrice>): Pair<String, String>? {
    val price24 = prices.firstOrNull { it.karat == "24K" }?.price ?: return null
    val price21 = prices.firstOrNull { it.karat == "21K" }?.price ?: return null
    val title = "أسعار الذهب اليوم"
    val body = "عيار 24: ${fmt(price24, 2, grouped = true)} ريال للجرام\n" +
        "عيار 21: ${fmt(price21, 2, grouped = true)} ريال للجرام"
    return title to body
}
