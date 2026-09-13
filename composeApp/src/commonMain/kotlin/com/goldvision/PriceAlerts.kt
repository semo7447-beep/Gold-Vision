package com.goldvision

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// تنبيه سعر يحدده المستخدم بنفسه: "نبّهني لما يوصل سعر عيار كذا لسعر كذا".
// الاتجاه (صعود/هبوط) يُستنتج تلقائياً وقت الإنشاء بمقارنة السعر الحالي
// بالسعر المستهدف — المستخدم يكتب رقماً واحداً بس بدون اختيار اتجاه يدوياً
@Serializable
internal data class PriceAlert(
    val id: String,
    val karat: String,
    val targetPrice: Double,
    val isUpward: Boolean
)

private const val priceAlertsStorageFile = "price_alerts.json"

internal fun loadPriceAlerts(): List<PriceAlert> {
    val text = AppStorage.readText(priceAlertsStorageFile) ?: return emptyList()
    return try {
        Json.decodeFromString<List<PriceAlert>>(text)
    } catch (e: Exception) {
        emptyList()
    }
}

internal fun persistPriceAlerts(alerts: List<PriceAlert>) {
    AppStorage.writeText(priceAlertsStorageFile, Json.encodeToString(alerts))
}

// يجدول أو يلغي فحصاً دورياً (كل 15 دقيقة، أقل مدة تسمح بها WorkManager)
// لأسعار الذهب مقابل التنبيهات المحفوظة — يعمل فقط طالما فيه تنبيه واحد
// نشط على الأقل، ويتوقف تلقائياً إذا حُذفت كل التنبيهات (لتوفير البطارية)
internal expect object PriceAlertScheduler {
    fun setActive(active: Boolean)
}

// نص إشعار تنبيه السعر (عنوان + محتوى)، أو null إن لم يتحقق شرط أي تنبيه
// بعد. يُستدعى من Worker الأندرويد بعد جلب السعر الحي — ويُعيد أيضاً قائمة
// التنبيهات المتبقية (بعد حذف أي تنبيه تحقق شرطه، لأنه يعمل مرة واحدة فقط)
internal fun checkPriceAlerts(alerts: List<PriceAlert>): Pair<List<Pair<String, String>>, List<PriceAlert>> {
    val triggered = mutableListOf<Pair<String, String>>()
    val remaining = mutableListOf<PriceAlert>()
    alerts.forEach { alert ->
        val currentPrice = GoldMarket.prices.firstOrNull { it.karat == alert.karat }?.price
        val reachedPrice = currentPrice?.takeIf { price ->
            if (alert.isUpward) price >= alert.targetPrice else price <= alert.targetPrice
        }
        if (reachedPrice != null) {
            val title = "تنبيه سعر الذهب"
            val body = "${karatLabel(alert.karat)} وصل إلى ${fmt(reachedPrice, 2, grouped = true)} ريال " +
                "(هدفك: ${fmt(alert.targetPrice, 2, grouped = true)} ريال)"
            triggered.add(title to body)
        } else {
            remaining.add(alert)
        }
    }
    return triggered to remaining
}
