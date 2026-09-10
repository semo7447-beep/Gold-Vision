package com.goldvision

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// جسر الأسعار العالمية الحية: يجلب سعر الذهب والفضة الفعلي (XAU/USD، XAG/USD)
// من GoldAPI.io ويحوّله لسعر الجرام بالريال السعودي لكل عيار، بدل الأرقام
// الثابتة السابقة. متاح من commonMain فيعمل بنفس الطريقة على أندرويد و iOS.
//
// لازم تحصل على مفتاح API مجاني (دقيقتين، بلا بطاقة ائتمان) من:
//   https://www.goldapi.io/dashboard/signup
// وتحط المفتاح مكان GOLD_API_KEY تحت. بدون مفتاح صحيح ستظل الأسعار المعروضة
// هي القيم الافتراضية defaultPrices فقط (مش هتتحدث، لكن التطبيق يعمل عادي).
//
// السبب في التحول عن مزوّد سابق بلا مفتاح (data-asg.goldprice.org): ثبت في
// الاستخدام الفعلي أنه غير موثوق (يرجع فاضي على بعض الشبكات). GoldAPI.io
// مزوّد رسمي موثّق مقابل ذلك.
internal object GoldMarket {

    private const val GOLD_API_KEY = "YOUR_GOLDAPI_IO_KEY_HERE"

    // 1 أونصة تروي (الوحدة العالمية لتسعير المعادن الثمينة) = 31.1034768 جرام
    private const val GRAMS_PER_TROY_OUNCE = 31.1034768

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

    var silverPricePerGram by mutableStateOf(4.35)
        private set

    var silverPercentChange by mutableStateOf(0.22)
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
        if (GOLD_API_KEY == "YOUR_GOLDAPI_IO_KEY_HERE") {
            lastError = "لم يتم ضبط مفتاح GoldAPI.io بعد — راجع تعليق GOLD_API_KEY في GoldMarket.kt"
            return
        }

        isLoading = true
        try {
            val gold: GoldApiResponse = client.get("https://www.goldapi.io/api/XAU/USD") {
                header("x-access-token", GOLD_API_KEY)
            }.body()

            val changePerGramSar = (gold.ch / GRAMS_PER_TROY_OUNCE) * USD_TO_SAR

            prices = listOf(
                KaratPrice("24K", gold.priceGram24k * USD_TO_SAR, changePerGramSar, gold.chp),
                KaratPrice("22K", gold.priceGram22k * USD_TO_SAR, changePerGramSar * (22.0 / 24.0), gold.chp),
                KaratPrice("21K", gold.priceGram21k * USD_TO_SAR, changePerGramSar * (21.0 / 24.0), gold.chp),
                KaratPrice("18K", gold.priceGram18k * USD_TO_SAR, changePerGramSar * (18.0 / 24.0), gold.chp)
            )

            val silver: GoldApiResponse = client.get("https://www.goldapi.io/api/XAG/USD") {
                header("x-access-token", GOLD_API_KEY)
            }.body()

            silverPricePerGram = (silver.price / GRAMS_PER_TROY_OUNCE) * USD_TO_SAR
            silverPercentChange = silver.chp

            lastError = null
        } catch (e: Exception) {
            lastError = "تعذر تحديث الأسعار العالمية، يتم عرض آخر سعر متوفر"
        } finally {
            isLoading = false
        }
    }
}

// شكل استجابة GoldAPI.io (https://www.goldapi.io/api/XAU/USD وXAG/USD) —
// price بالدولار للأونصة، وprice_gram_* جاهزة بالدولار للجرام لكل عيار ذهب
// (غير متاحة لطلب الفضة، فنحسبها يدوياً من price بنفس طريقة الذهب)
@Serializable
private data class GoldApiResponse(
    val price: Double = 0.0,
    val ch: Double = 0.0,
    val chp: Double = 0.0,
    @SerialName("price_gram_24k") val priceGram24k: Double = 0.0,
    @SerialName("price_gram_22k") val priceGram22k: Double = 0.0,
    @SerialName("price_gram_21k") val priceGram21k: Double = 0.0,
    @SerialName("price_gram_18k") val priceGram18k: Double = 0.0
)
