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
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

// جسر الأسعار العالمية الحية: يجلب سعر الذهب الفعلي بالجرام لكل عيار من
// goldprice.dev (مزوّد عام موثّق، بلا حاجة لمفتاح API ولا تسجيل — على عكس
// المزوّدين السابقين: data-asg.goldprice.org ثبت أنه يرجع فاضي على شبكة
// المستخدم، وGoldAPI.io يحتاج تسجيل حساب). متاح من commonMain فيعمل بنفس
// الطريقة على أندرويد و iOS مستقبلاً.
//
// ملاحظة: السعر يوصل بالدولار فقط (الريال غير مدعوم في قائمة عملات هذا
// المزوّد)، فنحوّله بسعر الصرف الرسمي الثابت. ونسبة/قيمة التغيّر المعروضة
// هي "منذ آخر تحديث" (محسوبة محلياً بمقارنة آخر سعرين)، وليست تغيّر اليوم،
// لأن هذا المزوّد لا يرجّع تغيّراً يومياً في هذا المسار.
internal object GoldMarket {

    // الريال السعودي مربوط رسمياً بسعر ثابت للدولار الأمريكي منذ 1986
    private const val USD_TO_SAR = 3.75

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
            rawBody = client.get("https://api.goldprice.dev/v1/carat?currency=USD").bodyAsText()
            val fields = extractCaratFields(rawBody)
                ?: error("شكل استجابة غير متوقع من مزوّد الأسعار")

            val previous = prices
            val updated = listOf(
                "24K" to fields.priceGram24k,
                "22K" to fields.priceGram22k,
                "21K" to fields.priceGram21k,
                "18K" to fields.priceGram18k
            ).map { (karat, usdPerGramText) ->
                // قيمة غير رقمية أو صفرية/سالبة تعني استجابة غير سليمة
                // (شكل مختلف، صيانة، تحديد معدّل...) — نرفضها بدل قبولها
                // كسعر حقيقي، حتى لا تُصفَّر الأسعار المعروضة صامتة
                val usdPerGram = usdPerGramText.toDoubleOrNull()
                    ?.takeIf { it > 0.0 }
                    ?: error("سعر غير صالح لعيار $karat: \"$usdPerGramText\"")
                val sarPerGram = usdPerGram * USD_TO_SAR
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

// شكل استجابة GET https://api.goldprice.dev/v1/carat?currency=USD — مزوّد
// عام بلا حاجة لمفتاح API. الأسعار ترجع كنصوص عشرية (decimal strings)
// وليست أرقاماً مباشرة
private data class CaratFields(
    val priceGram24k: String,
    val priceGram22k: String,
    val priceGram21k: String,
    val priceGram18k: String
)

// تحليل دفاعي: نبحث عن الحقول الأربعة في جذر الاستجابة مباشرة، وإن لم
// نجدها هناك نبحث داخل أغلفة شائعة (data/result/prices) بدل الفشل فوراً
// — هذا يتحمّل تغيّر شكل الاستجابة (تغليفها بكائن إضافي مثلاً) دون
// الحاجة لتحديث الكود في كل مرة
private fun extractCaratFields(bodyText: String): CaratFields? {
    val root = try {
        Json.parseToJsonElement(bodyText) as? JsonObject
    } catch (e: Exception) {
        null
    } ?: return null

    val candidateObjects = listOfNotNull(
        root,
        root["data"] as? JsonObject,
        root["result"] as? JsonObject,
        root["prices"] as? JsonObject
    )

    for (obj in candidateObjects) {
        val g24 = obj["price_gram_24k"]?.jsonPrimitive?.contentOrNull
        val g22 = obj["price_gram_22k"]?.jsonPrimitive?.contentOrNull
        val g21 = obj["price_gram_21k"]?.jsonPrimitive?.contentOrNull
        val g18 = obj["price_gram_18k"]?.jsonPrimitive?.contentOrNull
        if (g24 != null && g22 != null && g21 != null && g18 != null) {
            return CaratFields(g24, g22, g21, g18)
        }
    }
    return null
}
