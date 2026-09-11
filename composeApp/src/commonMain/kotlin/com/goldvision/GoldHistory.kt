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

// جسر بيانات الأسعار التاريخية الحقيقية: نفس مزوّد الأسعار الحية
// (api.goldprice.dev) عنده مسار /v1/bars يرجّع شموع يومية حقيقية
// (open/high/low/close) لسعر الأونصة بالدولار — لكن خطته المجانية بلا
// تسجيل تسمح فقط بآخر 30 يوماً (حتى مع فتح حساب مجاني، هذا سقف الخطة
// وليس حد تسجيل). الفترات الأطول (3 شهور فأكثر) تحتاج اشتراك مدفوع
// عند نفس المزوّد، فنستخدم لها تقديراً "ذكياً" مبنياً على زخم آخر
// 30 يوماً الحقيقية بدل رسم عشوائي غير مرتبط بالواقع (انظر
// estimatedPeriodStats في App.kt)
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
        try {
            val from = today.minus(REAL_HISTORY_DAYS - 1, DateTimeUnit.DAY)
            val url = "https://api.goldprice.dev/v1/bars" +
                    "?symbol=XAU-USD-SPOT&interval=1d&from=$from&to=$today&limit=100"
            val bodyText = client.get(url).bodyAsText()
            val bars = parseBars(bodyText)
            if (bars.isEmpty()) {
                lastError = "تعذر تحليل بيانات الأسعار التاريخية الحقيقية"
            } else {
                dailyBarsUsdPerOunce = bars
                lastError = null
            }
        } catch (e: Exception) {
            lastError = "تعذر تحميل بيانات الأسعار التاريخية الحقيقية"
        } finally {
            isLoading = false
        }
    }

    // تحليل دفاعي: شكل استجابة /v1/bars غير موثّق بدقة (لم تُتَح تجربته
    // مباشرة من هذه البيئة بسبب حجب الشبكة)، فبدل كائن Kotlin صارم يفشل
    // بالكامل عند أول اختلاف تسمية، نبحث يدوياً عن أول مصفوفة JSON في
    // الاستجابة ثم نقرأ كل شمعة بمرونة (نص التاريخ يُقبل بعدة تسميات
    // شائعة)، فإن فشل كل شيء نرجع قائمة فارغة ويظهر تحذير بدل بيانات
    // مزيّفة أو تعطّل التطبيق
    private fun parseBars(bodyText: String): List<HistoryBar> {
        val root = Json.parseToJsonElement(bodyText) as? JsonObject ?: return emptyList()

        val arrayCandidateKeys = listOf("bars", "data", "results", "items", "prices", "candles")
        val barsArray: JsonArray = arrayCandidateKeys
            .firstNotNullOfOrNull { key -> root[key] as? JsonArray }
            ?: root.values.filterIsInstance<JsonArray>().firstOrNull()
            ?: return emptyList()

        val dateKeys = listOf("t", "date", "time", "timestamp", "d")

        return barsArray.mapNotNull { element ->
            val bar = element as? JsonObject ?: return@mapNotNull null
            // قيمة صفرية/سالبة تعني شمعة غير سليمة (شكل استجابة مختلف عن
            // المتوقع) — نستبعدها بدل قبولها كسعر حقيقي بصفر
            val open = bar["open"]?.jsonPrimitive?.doubleOrNull?.takeIf { it > 0.0 } ?: return@mapNotNull null
            val close = bar["close"]?.jsonPrimitive?.doubleOrNull?.takeIf { it > 0.0 } ?: return@mapNotNull null
            val high = bar["high"]?.jsonPrimitive?.doubleOrNull?.takeIf { it > 0.0 } ?: maxOf(open, close)
            val low = bar["low"]?.jsonPrimitive?.doubleOrNull?.takeIf { it > 0.0 } ?: minOf(open, close)
            val dateText = dateKeys.firstNotNullOfOrNull { key -> bar[key]?.jsonPrimitive?.contentOrNull }
                ?: return@mapNotNull null
            val date = parseLooseIsoDate(dateText) ?: return@mapNotNull null
            HistoryBar(date = date, open = open, high = high, low = low, close = close)
        }.sortedBy { it.date }
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
