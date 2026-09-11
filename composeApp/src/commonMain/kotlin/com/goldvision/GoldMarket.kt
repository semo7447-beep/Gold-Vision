package com.goldvision

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

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
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 10_000
        }
    }

    suspend fun refresh() {
        isLoading = true
        try {
            val response: CaratResponse =
                client.get("https://api.goldprice.dev/v1/carat?currency=USD").body()

            val previous = prices
            val updated = listOf(
                "24K" to response.priceGram24k,
                "22K" to response.priceGram22k,
                "21K" to response.priceGram21k,
                "18K" to response.priceGram18k
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
            lastError = "تعذر تحديث الأسعار العالمية، يتم عرض آخر سعر متوفر"
        } finally {
            isLoading = false
        }
    }
}

// شكل استجابة GET https://api.goldprice.dev/v1/carat?currency=USD — مزوّد
// عام بلا حاجة لمفتاح API. الأسعار ترجع كنصوص عشرية (decimal strings)
// وليست أرقاماً مباشرة، لذلك الحقول هنا String وتُحوَّل يدوياً لاحقاً.
// عمداً بلا قيمة افتراضية: لو تغيّر شكل الاستجابة واختفى أحد الحقول
// نريد فشل التحليل (Exception) صراحة بدل الحصول على "0" بصمت — الفشل
// الصريح يُمسَك في catch أعلاه ويُبقي آخر سعر ناجح بدل تصفيره
@Serializable
private data class CaratResponse(
    @SerialName("price_gram_24k") val priceGram24k: String,
    @SerialName("price_gram_22k") val priceGram22k: String,
    @SerialName("price_gram_21k") val priceGram21k: String,
    @SerialName("price_gram_18k") val priceGram18k: String
)
