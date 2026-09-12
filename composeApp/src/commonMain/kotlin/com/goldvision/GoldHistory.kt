package com.goldvision

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonPrimitive

// جسر بيانات الأسعار التاريخية الحقيقية: xaus.com/api/v1/history يرجّع
// حتى 5 سنوات من الشموع اليومية (XAU/USD) دون أي حاجة لمفتاح أو معاملات
// — نفس مزوّد الأسعار الحية في GoldMarket.kt (استُبدل به api.goldprice.dev
// بعد اكتشاف حصته الشهرية المحدودة 1000 طلب فقط، والتي نفدت فعلياً
// بالتجربة). نأخذ من الاستجابة الكاملة آخر 30 يوماً فقط (كافية لفترات
// "أمس/أسبوع/شهر" في شاشة التحليل الفني)
internal object GoldHistory {

    private const val REAL_HISTORY_DAYS = 30L

    // شموع يومية حقيقية لآخر 30 يوماً، بسعر الأونصة بالدولار (XAU/USD)،
    // فارغة قبل أول تحديث ناجح أو إذا فشل التحليل/الطلب
    var dailyBarsUsdPerOunce by mutableStateOf<List<HistoryBar>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    // null يعني آخر تحديث نجح؛ أي نص هنا هو سبب فشل آخر محاولة (وتبقى
    // آخر بيانات ناجحة معروضة، وليست فارغة فجأة)
    var lastError by mutableStateOf<String?>(null)
        private set

    private val client = HttpClient {
        install(HttpTimeout) {
            requestTimeoutMillis = 10_000
        }
    }

    suspend fun refresh(today: LocalDate) {
        isLoading = true
        var rawBody = ""
        try {
            rawBody = client.get("https://xaus.com/api/v1/history").bodyAsText()
            val cutoff = today.minus(REAL_HISTORY_DAYS - 1, DateTimeUnit.DAY)
            val bars = parseBars(rawBody).filter { it.date >= cutoff && it.date <= today }
            if (bars.isEmpty()) {
                reportSilentError("GoldHistory.refresh failed: no bars parsed | body: ${rawBody.take(500)}")
                lastError = "تعذر تحليل بيانات الأسعار التاريخية الحقيقية"
            } else {
                dailyBarsUsdPerOunce = bars
                lastError = null
            }
        } catch (e: Exception) {
            reportSilentError("GoldHistory.refresh failed: ${e.message} | body: ${rawBody.take(500)}")
            lastError = "تعذر تحميل بيانات الأسعار التاريخية الحقيقية"
        } finally {
            isLoading = false
        }
    }

    // تحليل دفاعي: شكل استجابة /v1/history غير موثّق بدقة كافية (لم تُتَح
    // تجربته مباشرة من هذه البيئة بسبب حجب الشبكة)، فبدل كائن Kotlin
    // صارم يفشل بالكامل عند أول اختلاف تسمية، نبحث يدوياً عن أول مصفوفة
    // JSON في الاستجابة (بما فيها "points" الموثَّقة) ثم نقرأ كل شمعة
    // بمرونة (حروف مختصرة شائعة: d/c/h/l إضافة للأسماء الكاملة). حقل
    // الافتتاح غالباً غير متوفر بهذا المزوّد (شموع close/high/low فقط)،
    // فنشتقه من إغلاق اليوم السابق بعد الترتيب الزمني بدل استبعاد الشمعة
    private fun parseBars(bodyText: String): List<HistoryBar> {
        val root = Json.parseToJsonElement(bodyText) as? JsonObject ?: return emptyList()

        val arrayCandidateKeys = listOf("points", "bars", "data", "results", "items", "prices", "candles")
        val barsArray: JsonArray = arrayCandidateKeys
            .firstNotNullOfOrNull { key -> root[key] as? JsonArray }
            ?: root.values.filterIsInstance<JsonArray>().firstOrNull()
            ?: return emptyList()

        val dateKeys = listOf("d", "t", "date", "time", "timestamp")
        val closeKeys = listOf("c", "close")
        val highKeys = listOf("h", "high")
        val lowKeys = listOf("l", "low")
        val openKeys = listOf("o", "open")

        data class RawPoint(val date: LocalDate, val open: Double?, val high: Double, val low: Double, val close: Double)

        val points = barsArray.mapNotNull { element ->
            val point = element as? JsonObject ?: return@mapNotNull null
            // قيمة صفرية/سالبة تعني نقطة غير سليمة (شكل استجابة مختلف
            // عن المتوقع) — نستبعدها بدل قبولها كسعر حقيقي بصفر
            val close = closeKeys.firstNotNullOfOrNull { key -> point[key]?.jsonPrimitive?.doubleOrNull }
                ?.takeIf { it > 0.0 } ?: return@mapNotNull null
            val high = highKeys.firstNotNullOfOrNull { key -> point[key]?.jsonPrimitive?.doubleOrNull }
                ?.takeIf { it > 0.0 } ?: close
            val low = lowKeys.firstNotNullOfOrNull { key -> point[key]?.jsonPrimitive?.doubleOrNull }
                ?.takeIf { it > 0.0 } ?: close
            val open = openKeys.firstNotNullOfOrNull { key -> point[key]?.jsonPrimitive?.doubleOrNull }
                ?.takeIf { it > 0.0 }
            val dateText = dateKeys.firstNotNullOfOrNull { key -> point[key]?.jsonPrimitive?.contentOrNull }
                ?: return@mapNotNull null
            val date = parseLooseIsoDate(dateText) ?: return@mapNotNull null
            RawPoint(date, open, high, low, close)
        }.sortedBy { it.date }

        var previousClose: Double? = null
        return points.map { p ->
            val open = p.open ?: previousClose ?: p.close
            previousClose = p.close
            HistoryBar(date = p.date, open = open, high = p.high, low = p.low, close = p.close)
        }
    }

    // يقبل "2026-08-12" أو "2026-08-12T00:00:00Z" أو ما شابه، ويأخذ أول
    // 10 محارف فقط (yyyy-MM-dd)
    private fun parseLooseIsoDate(text: String): LocalDate? {
        val datePart = text.take(10)
        return try {
            LocalDate.parse(datePart)
        } catch (e: IllegalArgumentException) {
            null
        }
    }
}

internal data class HistoryBar(
    val date: LocalDate,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double
)
