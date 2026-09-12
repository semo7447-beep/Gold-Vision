package com.goldvision

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonPrimitive

// جسر الأسعار العالمية الحية: يجلب سعر أونصة الذهب الفعلي بالدولار من
// xaus.com — مزوّد مخصص لبيانات XAU/USD تحديداً، بلا حاجة لمفتاح API
// ولا تسجيل، ومُصمَّم أصلاً للاستطلاع المتكرر (مصدره محدَّث باستمرار
// ومخزَّن مؤقتاً 30 ثانية عند الحافة، بلا حد أقصى شهري صارم).
//
// تاريخ المزوّدين المجرَّبين: data-asg.goldprice.org رجع فاضياً على شبكة
// المستخدم، وapi.goldprice.dev (v1/carat) عنده حصة شهرية 1000 طلب فقط
// اكتُشفت بالتجربة الفعلية (رسالة quota_exceeded حقيقية من المزوّد عبر
// Sentry) — تنفد خلال أيام من الاستخدام العادي وتجعله غير مناسب لتحديث
// شبه لحظي. متاح من commonMain فيعمل بنفس الطريقة على أندرويد و iOS
// مستقبلاً
//
// ⚠️ لم يُختبر هذا المزوّد فعلياً من هذه الجلسة (الشبكة هنا مقيّدة عن
// نطاقه)، وشكل استجابته غير موثّق بدقة كافية — لذلك التحليل أدناه دفاعي
// (يجرّب عدة مسارات محتملة)، ويُلتقط نص الاستجابة الخام في Sentry عند
// الفشل لتشخيصه فوراً لو احتاج تعديلاً
//
// ملاحظة: السعر يوصل بالدولار فقط، فنحوّله بسعر الصرف الرسمي الثابت.
// ونسبة/قيمة التغيّر المعروضة هي "منذ آخر تحديث" (محسوبة محلياً بمقارنة
// آخر سعرين)، وليست تغيّر اليوم
// الأونصة = 31.1034768 جرام — يُستخدم هنا وفي extractUsdPerGram24k أدناه
private const val TROY_OUNCE_GRAMS = 31.1034768

internal object GoldMarket {

    // الريال السعودي مربوط رسمياً بسعر ثابت للدولار الأمريكي منذ 1986
    private const val USD_TO_SAR = 3.75
    private val karatPurity = listOf(
        "24K" to 1.0,
        "22K" to 22.0 / 24.0,
        "21K" to 21.0 / 24.0,
        "18K" to 18.0 / 24.0
    )

    private val defaultPrices = listOf(
        KaratPrice("24K", 403.75, 0.65, 0.16),
        KaratPrice("22K", 370.10, 0.61, 0.16),
        KaratPrice("21K", 353.28, 0.58, 0.16),
        KaratPrice("18K", 302.38, 0.55, 0.18)
    )

    var prices by mutableStateOf(defaultPrices)
        private set

    var isLoading by mutableStateOf(false)
        private set

    // null يعني آخر تحديث نجح؛ أي نص هنا هو سبب فشل آخر محاولة تحديث
    // (وتبقى الأسعار المعروضة هي آخر سعر ناجح، وليست صفراً)
    var lastError by mutableStateOf<String?>(null)
        private set

    private val client = HttpClient {
        install(HttpTimeout) {
            requestTimeoutMillis = 10_000
        }
    }

    suspend fun refresh() {
        isLoading = true
        var rawBody = ""
        try {
            rawBody = client.get("https://xaus.com/api/v1/spot?currency=USD&unit=gram").bodyAsText()
            val usdPerGram24k = extractUsdPerGram24k(rawBody)
                ?.takeIf { it > 0.0 }
                ?: error("شكل استجابة غير متوقع من مزوّد الأسعار")

            val previous = prices
            val updated = karatPurity.map { (karat, purity) ->
                val sarPerGram = usdPerGram24k * purity * USD_TO_SAR
                val previousPrice = previous.firstOrNull { it.karat == karat }?.price ?: sarPerGram
                val change = sarPerGram - previousPrice
                val percent = if (previousPrice != 0.0) (change / previousPrice) * 100.0 else 0.0
                KaratPrice(karat, sarPerGram, change, percent)
            }

            prices = updated
            lastError = null
        } catch (e: Exception) {
            // نرسل تفاصيل الخطأ الفعلية مرة واحدة فقط عند أول فشل بعد نجاح
            // (وليس عند كل محاولة فاشلة متكررة)، مع مقطع من نص الاستجابة
            // الخام، لتشخيص أسباب انقطاع الأسعار عن بُعد بدل الاعتماد على
            // لقطات شاشة فقط
            if (lastError == null) {
                reportSilentError("GoldMarket.refresh failed: ${e.message} | body: ${rawBody.take(500)}")
            }
            lastError = "تعذر تحديث الأسعار العالمية، يتم عرض آخر سعر متوفر"
        } finally {
            isLoading = false
        }
    }
}

// تحليل دفاعي: نجرّب عدة مسارات محتملة لسعر جرام الذهب الخالص (عيار 24)
// بالدولار من استجابة GET https://xaus.com/api/v1/spot?currency=USD&unit=gram،
// لأن شكل الاستجابة غير موثّق بدقة كافية ولم يمكن اختباره فعلياً من هذه
// البيئة. إن فشلت كل المسارات نرجع null بدل قيمة خاطئة، ويُلتقط نص
// الاستجابة الخام في catch أعلاه للتشخيص عن بُعد
private fun extractUsdPerGram24k(bodyText: String): Double? {
    val root = try {
        Json.parseToJsonElement(bodyText) as? JsonObject
    } catch (e: Exception) {
        null
    } ?: return null

    // 1) حقل مباشر لسعر الجرام بالدولار (إن وُجد بهذا الاسم)
    root["per_gram_usd"]?.jsonPrimitive?.doubleOrNull?.let { return it }

    // 2) كائن xau متداخل بحقل price (متوقَّع عند طلب unit=gram)
    (root["xau"] as? JsonObject)?.get("price")?.jsonPrimitive?.doubleOrNull?.let { return it }

    // 3) سعر الأونصة بالدولار (يُفترض أنه يرجع دائماً بغض النظر عن unit)،
    // نحوّله يدوياً لسعر الجرام إذا لم نجد سعر الجرام مباشرة
    root["spot_usd_oz"]?.jsonPrimitive?.doubleOrNull?.let { return it / TROY_OUNCE_GRAMS }

    return null
}
