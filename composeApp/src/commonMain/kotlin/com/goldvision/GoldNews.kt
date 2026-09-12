package com.goldvision

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toInstant

internal data class GoldNewsArticle(val title: String, val source: String, val publishedAt: String)

// أخبار حقيقية عن الذهب من Google News RSS (بحث بالعربية عن "أسعار الذهب
// الفيدرالي") — مصدر مجاني بالكامل، بلا مفتاح API ولا تسجيل ولا حد أقصى
// صارم للطلبات، ومتاح من عقود طويلة (خدمة بحث RSS رسمية من جوجل)
//
// ⚠️ لم يُختبر هذا المزوّد فعلياً من هذه الجلسة (الشبكة هنا مقيّدة عن
// نطاق news.google.com)، لذلك التحليل أدناه دفاعي (regex بسيط بدل مكتبة
// XML كاملة غير متاحة أصلاً بشكل موحّد بين أندرويد و iOS)، ويُلتقط نص
// الاستجابة الخام في Sentry عند الفشل لتشخيصه فوراً لو احتاج تعديلاً
internal object GoldNews {
    private const val FEED_URL =
        "https://news.google.com/rss/search?q=%D8%A7%D9%84%D8%B0%D9%87%D8%A8%20%D8%A7%D9%84%D9%81%D9%8A%D8%AF%D8%B1%D8%A7%D9%84%D9%8A%20when:3d&hl=ar&gl=SA&ceid=SA:ar"

    var articles by mutableStateOf<List<GoldNewsArticle>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    // null يعني آخر تحديث نجح؛ أي نص هنا هو سبب فشل آخر محاولة تحديث
    // (وتبقى الأخبار المعروضة هي آخر نتيجة ناجحة، وليست فارغة)
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
            rawBody = client.get(FEED_URL).bodyAsText()
            val parsed = parseRssItems(rawBody)
            if (parsed.isEmpty()) error("لم يُعثر على أي عنصر أخبار في الاستجابة")
            articles = parsed
            lastError = null
        } catch (e: Exception) {
            if (lastError == null) {
                reportSilentError("GoldNews.refresh failed: ${e.message} | body: ${rawBody.take(500)}")
            }
            lastError = "تعذر تحديث الأخبار، يتم عرض آخر الأخبار المتوفرة"
        } finally {
            isLoading = false
        }
    }
}

private val itemRegex = Regex("<item>(.*?)</item>", RegexOption.DOT_MATCHES_ALL)
private val titleRegex = Regex("<title>(?:<!\\[CDATA\\[)?(.*?)(?:]]>)?</title>", RegexOption.DOT_MATCHES_ALL)
private val sourceRegex = Regex("<source[^>]*>(?:<!\\[CDATA\\[)?(.*?)(?:]]>)?</source>", RegexOption.DOT_MATCHES_ALL)
private val pubDateRegex = Regex("<pubDate>(.*?)</pubDate>", RegexOption.DOT_MATCHES_ALL)

// تحليل RSS دفاعي بالـ regex بدل مكتبة XML: يكتفي باستخراج العنوان
// والمصدر وتاريخ النشر من كل <item>، ويتجاهل أي عنصر ناقص العنوان بدل
// تعطّل التحديث بالكامل بسبب عنصر واحد غير متوقّع الشكل
private fun parseRssItems(xml: String): List<GoldNewsArticle> {
    return itemRegex.findAll(xml).mapNotNull { match ->
        val block = match.groupValues[1]
        val title = titleRegex.find(block)?.groupValues?.get(1)?.let(::decodeXmlEntities)?.trim()
            ?.takeIf { it.isNotEmpty() } ?: return@mapNotNull null
        val source = sourceRegex.find(block)?.groupValues?.get(1)?.let(::decodeXmlEntities)?.trim() ?: ""
        val pubDate = pubDateRegex.find(block)?.groupValues?.get(1)?.trim() ?: ""
        GoldNewsArticle(title = title, source = source, publishedAt = relativeTimeFromRfc822(pubDate))
    }.toList()
}

private fun decodeXmlEntities(text: String): String = text
    .replace("&amp;", "&")
    .replace("&lt;", "<")
    .replace("&gt;", ">")
    .replace("&quot;", "\"")
    .replace("&#39;", "'")

private val rfc822MonthAbbreviations = mapOf(
    "Jan" to 1, "Feb" to 2, "Mar" to 3, "Apr" to 4, "May" to 5, "Jun" to 6,
    "Jul" to 7, "Aug" to 8, "Sep" to 9, "Oct" to 10, "Nov" to 11, "Dec" to 12
)

// يحوّل تاريخاً بصيغة RFC 822 المعتادة في RSS (مثال: "Thu, 12 Sep 2026
// 14:23:00 GMT") إلى نص عربي نسبي ("منذ 36 دقيقة")، بنفس أسلوب باقي
// التطبيق. يرجع نصاً فارغاً إذا فشل التحليل بدل تعطّل الخبر كاملاً
private fun relativeTimeFromRfc822(rfc822: String): String {
    return try {
        val parts = rfc822.substringAfter(", ").trim().split(" ")
        val day = parts[0].toInt()
        val month = rfc822MonthAbbreviations[parts[1]] ?: return ""
        val year = parts[2].toInt()
        val timeParts = parts[3].split(":").map { it.toInt() }
        val published = LocalDateTime(year, month, day, timeParts[0], timeParts[1], timeParts[2])
            .toInstant(TimeZone.UTC)
        val minutesAgo = (Clock.System.now() - published).inWholeMinutes.coerceAtLeast(0)
        when {
            minutesAgo < 1 -> "الآن"
            minutesAgo < 60 -> "منذ $minutesAgo دقيقة"
            minutesAgo < 60 * 24 -> "منذ ${minutesAgo / 60} ساعة"
            else -> "منذ ${minutesAgo / (60 * 24)} يوم"
        }
    } catch (e: Exception) {
        ""
    }
}
