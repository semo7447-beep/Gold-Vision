package com.goldvision

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import kotlinx.datetime.Clock
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonPrimitive

// جسر الأسعار العالمية الحية: يجلب سعر أونصة الذهب الفعلي بالدولار من
// GoldAPI.io (مزوّد مخصص لبيانات المعادن، مصدره بورصة لندن LBMA)،
// بمفتاح API شخصي (goldApiKey، يُقرأ من local.properties محلياً — غير
// مرفوع على GitHub). الخطة المجانية محدودة بـ 100 طلب/شهر فقط، لذلك
// refresh() تفرض حداً أدنى بين طلبات التحديث التلقائية (انظر
// MIN_AUTO_REFRESH_INTERVAL_MILLIS أدناه) — الحد يُحفَظ محلياً فيبقى
// فعّالاً حتى لو أُعيد تشغيل التطبيق، ويحمي الحصة من كل مصادر التحديث
// التلقائي مجتمعة (الشاشة الرئيسية + الويدجتين + التنبيهات) بغض النظر
// عن فترة كل مصدر بمفرده. الضغط اليدوي على زر التحديث يتجاوز هذا الحد
// دائماً (نية صريحة من المستخدم).
//
// ملاحظة: السعر يوصل بالدولار فقط، فنحوّله بسعر الصرف الرسمي الثابت.
// ونسبة/قيمة التغيّر المعروضة هي "منذ آخر تحديث" (محسوبة محلياً بمقارنة
// آخر سعرين)، وليست تغيّر اليوم
// الأونصة = 31.1034768 جرام
private const val TROY_OUNCE_GRAMS = 31.1034768

private const val MIN_AUTO_REFRESH_INTERVAL_MILLIS = 8L * 60 * 60 * 1000 // 8 ساعات
private const val lastFetchStorageFile = "gold_market_last_fetch.txt"
private const val lastPricesStorageFile = "gold_market_last_prices.json"

// يُقرأ مرة واحدة عند إقلاع التطبيق: آخر أسعار حُفظت فعلياً بعد نجاح
// حقيقي، بدل القيم الافتراضية الثابتة — بدونه، أي إعادة تشغيل للتطبيق
// (حتى لو كان آخر تحديث ناجح قبل ثوانٍ) بلا إنترنت تُرجع الأسعار لقيم
// وهمية ثابتة كأن التطبيق لم يُشغَّل من قبل إطلاقاً
private fun loadCachedPrices(): List<KaratPrice>? {
    val text = AppStorage.readText(lastPricesStorageFile) ?: return null
    return try {
        Json.decodeFromString<List<KaratPrice>>(text).takeIf { it.isNotEmpty() }
    } catch (e: Exception) {
        null
    }
}

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

    var prices by mutableStateOf(loadCachedPrices() ?: defaultPrices)
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

    // force = true (الضغط اليدوي على زر التحديث) يتجاوز الحد الأدنى بين
    // الطلبات التلقائية؛ الاستدعاءات التلقائية (عند فتح الشاشة، عمال
    // الويدجت، التنبيهات) تمر بلا force فتُفرض عليها الحماية من استهلاك
    // الحصة الشهرية
    suspend fun refresh(force: Boolean = false) {
        val now = Clock.System.now().toEpochMilliseconds()
        if (!force) {
            val lastAttempt = AppStorage.readText(lastFetchStorageFile)?.toLongOrNull()
            if (lastAttempt != null && now - lastAttempt < MIN_AUTO_REFRESH_INTERVAL_MILLIS) {
                return
            }
        }
        AppStorage.writeText(lastFetchStorageFile, now.toString())

        isLoading = true
        var rawBody = ""
        try {
            rawBody = client.get("https://www.goldapi.io/api/price/XAU/USD") {
                header("x-access-token", goldApiKey)
            }.bodyAsText()
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
            AppStorage.writeText(lastPricesStorageFile, Json.encodeToString(updated))
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

// شكل استجابة GoldAPI.io موثّق وثابت (خلاف المزوّدين السابقين): يُحلَّل
// من الحقول الحقيقية لواجهتهم (price_per_unit.gram و price)، مع بقاء
// احتمالات احتياطية بسيطة تحسباً لتبديل مزوّد لاحقاً
private fun extractUsdPerGram24k(bodyText: String): Double? {
    val root = try {
        Json.parseToJsonElement(bodyText) as? JsonObject
    } catch (e: Exception) {
        null
    } ?: return null

    // 1) شكل GoldAPI.io: سعر الجرام بالدولار مباشرة (عيار 24 الخالص)
    (root["price_per_unit"] as? JsonObject)?.get("gram")?.jsonPrimitive?.doubleOrNull?.let { return it }

    // 2) سعر الأونصة بالدولار (حقل "price" في GoldAPI.io)، نحوّله يدوياً
    root["price"]?.jsonPrimitive?.doubleOrNull?.let { return it / TROY_OUNCE_GRAMS }

    // 3) احتمالات احتياطية (مزوّدين آخرين محتملين مستقبلاً)
    root["per_gram_usd"]?.jsonPrimitive?.doubleOrNull?.let { return it }
    (root["xau"] as? JsonObject)?.get("price")?.jsonPrimitive?.doubleOrNull?.let { return it }
    root["spot_usd_oz"]?.jsonPrimitive?.doubleOrNull?.let { return it / TROY_OUNCE_GRAMS }

    return null
}
