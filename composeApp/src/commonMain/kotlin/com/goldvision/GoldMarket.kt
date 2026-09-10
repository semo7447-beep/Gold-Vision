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
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// جسر الأسعار العالمية الحية: يجلب سعر الأونصة الفعلي للذهب والفضة (XAU/USD،
// XAG/USD) ويحوّله لسعر الجرام بالريال السعودي لكل عيار، بدل الأرقام الثابتة
// السابقة. متاح من commonMain فيعمل بنفس الطريقة على أندرويد و iOS مستقبلاً.
internal object GoldMarket {

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
        isLoading = true
        try {
            val response: GoldPriceResponse =
                client.get("https://data-asg.goldprice.org/dbXRates/USD").body()
            val item = response.items.firstOrNull()
                ?: throw IllegalStateException("empty response")

            val goldPerGram24kSar = (item.xauPrice / GRAMS_PER_TROY_OUNCE) * USD_TO_SAR
            val goldChangePerGram24kSar = (item.chgXau / GRAMS_PER_TROY_OUNCE) * USD_TO_SAR

            prices = listOf(24, 22, 21, 18).map { karat ->
                val purityRatio = karat / 24.0
                KaratPrice(
                    karat = "${karat}K",
                    price = goldPerGram24kSar * purityRatio,
                    change = goldChangePerGram24kSar * purityRatio,
                    percent = item.pcXau
                )
            }

            silverPricePerGram = (item.xagPrice / GRAMS_PER_TROY_OUNCE) * USD_TO_SAR
            silverPercentChange = item.pcXag

            lastError = null
        } catch (e: Exception) {
            lastError = "تعذر تحديث الأسعار العالمية، يتم عرض آخر سعر متوفر"
        } finally {
            isLoading = false
        }
    }
}

// شكل استجابة data-asg.goldprice.org/dbXRates/USD (نفس المزوّد المستخدم
// في أداة الأسعار المباشرة الشهيرة goldprice.org، بلا حاجة لمفتاح API)
@Serializable
private data class GoldPriceResponse(
    val items: List<GoldPriceItem> = emptyList()
)

@Serializable
private data class GoldPriceItem(
    val xauPrice: Double = 0.0,
    val xagPrice: Double = 0.0,
    val chgXau: Double = 0.0,
    val chgXag: Double = 0.0,
    val pcXau: Double = 0.0,
    val pcXag: Double = 0.0
)
