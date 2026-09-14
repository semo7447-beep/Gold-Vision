package com.goldvision

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Balance
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.goldvision.resources.Res
import com.goldvision.resources.logo_gold_vision
import org.jetbrains.compose.resources.painterResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.seconds

private val Black = Color(0xFF050505)
private val CardBlack = Color(0xFF090909)
private val Gold = Color(0xFFFFC21A)
private val GoldDark = Color(0xFF7A5A00)
private val White = Color(0xFFF4F4F4)
private val Gray = Color(0xFFB8B8B8)
private val Green = Color(0xFF35D12F)
private val Red = Color(0xFFFF3B30)
private val Yellow = Color(0xFFFFC21A)
private val Border = Color(0xFF9B7300)

// يُستخدم من App.kt (واجهات الشاشات) ومن GoldMarket.kt (جلب الأسعار الحية)
internal data class KaratPrice(
    val karat: String,
    val price: Double,
    val change: Double,
    val percent: Double
)

// ملف شخصي محلي بسيط (اسم + رمز تعبيري) — بلا تسجيل دخول ولا حساب فعلي،
// يُحفظ على الجهاز فقط عبر AppStorage، بلا أي خادم أو مزامنة
@Serializable
private data class UserProfile(
    val name: String = "",
    val avatar: String = "👤"
)

private val profileAvatarOptions = listOf("👤", "😊", "🧑", "👨", "👩", "🧔", "👳", "🕵️")

// بيانات صفقة محفوظة من شاشة "المحل أعطاك سعراً؟" (اسم المحل + السعر + مستوى التقييم)
private data class SavedDeal(
    val shopName: String,
    val totalPrice: Double,
    val tierLabel: String,
    val tierColor: Color
)

// ==================== بيانات شاشة الزكاة ====================
private data class ZakatItem(
    val name: String,
    val emoji: String,
    val karat: String,
    val weightGrams: Double,
    val purchaseDate: String
)

// قطعة ذهب أضافها المستخدم بنفسه عبر شاشة "إضافة قطعة" — تظهر في المحفظة
// وفي الزكاة معاً (نفس الصنف بنفس البيانات)، بسعر يُحسب حياً من GoldMarket
@Serializable
internal data class GoldItem(
    val name: String,
    val emoji: String,
    val karat: String,
    val weightGrams: Double,
    val purchasePriceWithTax: Double,
    val manufacturingPerGram: Double,
    val purchaseDate: String,
    val notes: String,
    // قطعة "مباعة" (محفوظة من الحاسبة في وضع بيع) لم تعد مِلكاً للمستخدم:
    // تُستبعد من إجمالي الزكاة رغم بقائها في سجل المحفظة. القيمة
    // الافتراضية false تحافظ على توافق القطع المحفوظة قبل إضافة هذا الحقل
    val isSold: Boolean = false
)

// رموز تشكيلية لتمثيل شكل القطعة بدل صورة فعلية (خاتم/سوار/سلسلة/سبيكة...)
private val pieceEmojiOptions = listOf(
    "💍" to "خاتم",
    "⭕" to "سوار",
    "📿" to "سلسلة",
    "🟨" to "سبيكة",
    "👂" to "حلق",
    "🪙" to "عملة"
)

// نصوص pieceEmojiOptions تبقى عربية كمعرّف عرض ثابت؛ هذه الدالة تُترجم
// النص المعروض فقط (الإيموجي نفسه هو الذي يُحفظ فعلياً في GoldItem)
private fun pieceShapeLabel(label: String): String = when (label) {
    "خاتم" -> t("خاتم", "Ring")
    "سوار" -> t("سوار", "Bracelet")
    "سلسلة" -> t("سلسلة", "Necklace")
    "سبيكة" -> t("سبيكة", "Bar")
    "حلق" -> t("حلق", "Earring")
    "عملة" -> t("عملة", "Coin")
    else -> label
}

private fun GoldItem.toZakatItem(): ZakatItem = ZakatItem(
    name = name,
    emoji = emoji,
    karat = karat,
    weightGrams = weightGrams,
    purchaseDate = purchaseDate
)

private fun LocalDate.toDisplayText(): String {
    val day = dayOfMonth.toString().padStart(2, '0')
    val month = monthNumber.toString().padStart(2, '0')
    return "$day / $month / $year"
}

private fun todayDateText(): String = todayLocalDate().toDisplayText()

// DatePicker (Material3) يرجّع ميلي ثانية UTC لمنتصف ليل اليوم المختار،
// فنحوّلها بتوقيت UTC نفسه تفادياً لخطأ يوم واحد بسبب فرق التوقيت المحلي
private fun dateTextFromEpochMillis(epochMillis: Long): String =
    Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(TimeZone.UTC).date.toDisplayText()

private fun parseDisplayDate(text: String): LocalDate? {
    val parts = text.split("/").map { it.trim() }
    if (parts.size != 3) return null
    val day = parts[0].toIntOrNull() ?: return null
    val month = parts[1].toIntOrNull() ?: return null
    val year = parts[2].toIntOrNull() ?: return null
    return try {
        LocalDate(year, month, day)
    } catch (e: IllegalArgumentException) {
        null
    }
}

// الحول الهجري (القمري) الكامل ≈ 354 يوماً، يُستخدم كتقريب عملي لمرور
// الحول بدل تقويم هجري كامل (غير متوفر في kotlinx-datetime)
private const val HAWL_DAYS = 354

private fun daysSincePurchase(purchaseDateText: String): Int {
    val purchaseDate = parseDisplayDate(purchaseDateText) ?: return 0
    return purchaseDate.daysUntil(todayLocalDate()).coerceAtLeast(0)
}

private data class ZakatStatus(val label: String, val color: Color, val caption: String)

// حالة كل قطعة تُبنى من شرطين معاً: مرور الحول منذ تاريخ الشراء، وبلوغ
// إجمالي محفظة الذهب النصاب الشرعي (exceedsNisab يُحسب على مستوى الشاشة
// كاملة وليس لكل قطعة على حدة، لأن النصاب شرط إجمالي لكل ما يملكه الشخص)
private fun zakatStatusFor(purchaseDateText: String, exceedsNisab: Boolean): ZakatStatus {
    val daysElapsed = daysSincePurchase(purchaseDateText)
    val hawlCompleted = daysElapsed >= HAWL_DAYS
    return when {
        hawlCompleted && exceedsNisab ->
            ZakatStatus(
                t("وجب عليه الزكاة", "Zakat Due"),
                Green,
                t("منذ ${(daysElapsed / 30).coerceAtLeast(1)} شهراً تقريباً", "About ${(daysElapsed / 30).coerceAtLeast(1)} months ago")
            )
        hawlCompleted ->
            ZakatStatus(t("وقت الزكاة", "Zakat Time"), Green, t("حال عليه الحول", "Hawl completed"))
        else ->
            ZakatStatus(
                t("متبقي ${HAWL_DAYS - daysElapsed} يوماً", "${HAWL_DAYS - daysElapsed} days left"),
                Gray,
                t("لم يكتمل الحول بعد", "Hawl not yet complete")
            )
    }
}

// ==================== مواعيد الفيدرالي ====================
private data class FedMeetingRaw(val year: Int, val month: Int, val day: Int, val time: String)

// القرار يُعلن عادة الساعة 9:00 مساءً بتوقيت مكة المكرمة (2:00 ظهراً بتوقيت
// واشنطن) — يجب تحديث هذه القائمة يدوياً كل عام عند إعلان التقويم الرسمي
// الجديد على federalreserve.gov، لا يوجد مصدر بيانات مجاني حي لهذه المواعيد
private val fedMeetingsRaw = listOf(
    FedMeetingRaw(2026, 1, 28, "09:00 م"),
    FedMeetingRaw(2026, 3, 18, "09:00 م"),
    FedMeetingRaw(2026, 4, 29, "09:00 م"),
    FedMeetingRaw(2026, 6, 17, "09:00 م"),
    FedMeetingRaw(2026, 7, 29, "09:00 م"),
    FedMeetingRaw(2026, 9, 16, "09:00 م"),
    FedMeetingRaw(2026, 10, 28, "09:00 م"),
    FedMeetingRaw(2026, 12, 9, "09:00 م"),
    FedMeetingRaw(2027, 1, 27, "09:00 م"),
    FedMeetingRaw(2027, 3, 17, "09:00 م")
)

// مبني على kotlinx-datetime بدل java.util.Calendar/SimpleDateFormat
// لأن هذه الأخيرة متاحة على أندرويد فقط ولن تُصرّف على iOS مستقبلاً
private val arabicDayNames = mapOf(
    DayOfWeek.SATURDAY to "السبت",
    DayOfWeek.SUNDAY to "الأحد",
    DayOfWeek.MONDAY to "الاثنين",
    DayOfWeek.TUESDAY to "الثلاثاء",
    DayOfWeek.WEDNESDAY to "الأربعاء",
    DayOfWeek.THURSDAY to "الخميس",
    DayOfWeek.FRIDAY to "الجمعة"
)

private val englishDayNames = mapOf(
    DayOfWeek.SATURDAY to "Saturday",
    DayOfWeek.SUNDAY to "Sunday",
    DayOfWeek.MONDAY to "Monday",
    DayOfWeek.TUESDAY to "Tuesday",
    DayOfWeek.WEDNESDAY to "Wednesday",
    DayOfWeek.THURSDAY to "Thursday",
    DayOfWeek.FRIDAY to "Friday"
)

private fun dayNameFor(dayOfWeek: DayOfWeek): String =
    if (AppLanguage.current == AppLang.EN) englishDayNames[dayOfWeek] ?: "" else arabicDayNames[dayOfWeek] ?: ""

// fedMeetingsRaw.time مخزَّن بصيغة "09:00 م" ثابتة؛ هذه الدالة فقط تستبدل
// حرف الصباح/المساء العربي بالمكافئ الإنجليزي عند عرضه، دون تغيير التخزين
private fun timeDisplayLabel(time: String): String =
    if (AppLanguage.current == AppLang.EN) time.replace("ص", "AM").replace("م", "PM") else time

internal fun todayLocalDate(): LocalDate =
    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

private data class FedMeetingRow(val day: String, val date: String, val time: String, val daysLeft: Int)

private fun upcomingFedMeetings(): List<FedMeetingRow> {
    val today = todayLocalDate()
    return fedMeetingsRaw
        .map { it to LocalDate(it.year, it.month, it.day) }
        .filter { (_, date) -> date >= today }
        .sortedBy { (_, date) -> date }
        .map { (raw, date) ->
            FedMeetingRow(
                day = dayNameFor(date.dayOfWeek),
                date = "${raw.year}/${raw.month}/${raw.day}",
                time = raw.time,
                daysLeft = today.daysUntil(date)
            )
        }
}

private fun currentDateTimeText(): String {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val hour24 = now.hour
    val period = if (hour24 < 12) t("ص", "AM") else t("م", "PM")
    val hour12 = when {
        hour24 == 0 -> 12
        hour24 > 12 -> hour24 - 12
        else -> hour24
    }
    val minute = now.minute.toString().padStart(2, '0')
    return t(
        "آخر تحديث: ${now.year}/${now.monthNumber}/${now.dayOfMonth} الساعة $hour12:$minute $period",
        "Last updated: ${now.year}/${now.monthNumber}/${now.dayOfMonth} at $hour12:$minute $period"
    )
}

// نقطة الدخول المشتركة بين أندرويد و iOS — كل منصة تستدعيها من نقطة دخولها الخاصة
// (MainActivity على أندرويد، MainViewController على iOS)
@Composable
fun App() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Black
        ) {
            val layoutDirection = if (AppLanguage.current == AppLang.EN) LayoutDirection.Ltr else LayoutDirection.Rtl
            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                LaunchedEffect(Unit) { seedDefaultPriceAlertsIfNeeded() }
                var showOnboarding by remember { mutableStateOf(!hasSeenOnboarding()) }
                if (showOnboarding) {
                    OnboardingScreen(
                        onFinish = {
                            markOnboardingSeen()
                            showOnboarding = false
                        }
                    )
                } else {
                    val lockEnabled = remember { loadAppLockSettings().enabled }
                    BiometricAuthGate(enabled = lockEnabled) {
                        GoldVisionApp()
                    }
                }
            }
        }
    }
}

// ==================== شاشة تعريفية عند أول فتح للتطبيق ====================
private const val onboardingSeenStorageFile = "onboarding_seen.txt"

private fun hasSeenOnboarding(): Boolean = AppStorage.readText(onboardingSeenStorageFile) == "true"

private fun markOnboardingSeen() {
    AppStorage.writeText(onboardingSeenStorageFile, "true")
}

private data class OnboardingPage(val title: String, val description: String, val icon: ImageVector)

// دالة بدل val ثابتة حتى تبقى النصوص متجاوبة مع اللغة الحالية عند كل استدعاء
private fun onboardingPages(): List<OnboardingPage> = listOf(
    OnboardingPage(
        t("تتبع أسعار الذهب لحظياً", "Track Gold Prices Live"),
        t(
            "أسعار حقيقية تتحدث تلقائياً لكل العيارات (24، 22، 21، 18)، مع رسم بياني تاريخي لعدة فترات.",
            "Real prices that update automatically for every karat (24, 22, 21, 18), with a historical chart for several periods."
        ),
        Icons.AutoMirrored.Outlined.ShowChart
    ),
    OnboardingPage(
        t("محفظتك وزكاتك في مكان واحد", "Your Portfolio and Zakat in One Place"),
        t(
            "أضف قطعك الذهبية، وتابع قيمتها الحية، واحسب زكاتك تلقائياً وفق النصاب الشرعي.",
            "Add your gold items, track their live value, and calculate your Zakat automatically according to the Shariah Nisab."
        ),
        Icons.Outlined.AccountBalanceWallet
    ),
    OnboardingPage(
        t("قيّم عروض المحلات", "Evaluate Shop Offers"),
        t(
            "قبل ما تشتري أو تبيع، قارن سعر المحل بالسعر العادل فوراً واعرف هل الصفقة ممتازة.",
            "Before you buy or sell, instantly compare the shop's price to the fair price and know if the deal is great."
        ),
        Icons.Outlined.Store
    ),
    OnboardingPage(
        t("تنبيهات ذكية", "Smart Alerts"),
        t(
            "نبّهك عند وصول السعر لهدفك، وعند اقتراب اجتماعات الفيدرالي المؤثرة على السوق. " +
                "وحتى تجرّب الميزة فوراً، نضيف لك تلقائياً تنبيهين جاهزين على عيار 24 (ارتفاع 5 ريال وانخفاض 5 ريال) تقدر تعدّلهم أو تحذفهم متى ما أردت.",
            "We notify you when the price reaches your target, and when Fed meetings that affect the market approach. " +
                "So you can try the feature right away, we automatically add two ready alerts on 24K gold (+5 SAR and -5 SAR) that you can edit or delete anytime."
        ),
        Icons.Outlined.Notifications
    )
)

// معاينة مصغّرة لبطاقات التنبيهات الحقيقية (نفس تصميم شاشة الإشعارات
// الفعلية) — تُعرض فقط بصفحة "تنبيهات ذكية" التعريفية، حتى يشوف المستخدم
// شكل التنبيهين الافتراضيين بصرياً بدل مجرد وصف نصي
@Composable
private fun OnboardingAlertsPreview() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, Border, RoundedCornerShape(12.dp))
            .background(CardBlack)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf(
            Triple(true, t("24 عيار", "24K"), t("عند الارتفاع 5 ريال", "On +5 SAR rise")),
            Triple(false, t("24 عيار", "24K"), t("عند الانخفاض 5 ريال", "On -5 SAR drop"))
        ).forEach { (isUpward, karat, subtitle) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ShowChart,
                        contentDescription = null,
                        tint = if (isUpward) Green else Red,
                        modifier = Modifier.size(14.dp)
                    )
                    Column {
                        Text(karat, color = White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(subtitle, color = Gray, fontSize = 9.sp)
                    }
                }
                Switch(
                    checked = true,
                    onCheckedChange = {},
                    enabled = false,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Black,
                        checkedTrackColor = Gold,
                        disabledCheckedThumbColor = Black,
                        disabledCheckedTrackColor = Gold
                    ),
                    modifier = Modifier.scale(0.6f)
                )
            }
        }
    }
}

@Composable
private fun OnboardingScreen(onFinish: () -> Unit) {
    val pages = onboardingPages()
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == pages.lastIndex

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
            GoldLogo(size = 52.dp)
        }

        Spacer(Modifier.weight(1f))

        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth()) { page ->
            val item = pages[page]
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(GoldDark.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = item.icon, contentDescription = null, tint = Gold, modifier = Modifier.size(38.dp))
                }
                Spacer(Modifier.height(24.dp))
                Text(
                    item.title,
                    color = White,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    item.description,
                    color = Gray,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
                if (page == pages.lastIndex) {
                    Spacer(Modifier.height(16.dp))
                    OnboardingAlertsPreview()
                }
            }
        }

        Spacer(Modifier.height(28.dp))
        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
            pages.indices.forEach { index ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (index == pagerState.currentPage) 9.dp else 7.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(if (index == pagerState.currentPage) Gold else Border)
                )
            }
        }

        Spacer(Modifier.weight(1f))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Gold)
                .clickable {
                    if (isLastPage) {
                        onFinish()
                    } else {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text(if (isLastPage) t("ابدأ", "Start") else t("التالي", "Next"), color = Black, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(14.dp))
        Text(
            if (isLastPage) " " else t("تخطي", "Skip"),
            color = Gray,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !isLastPage) { onFinish() }
        )
    }
}

// ==================== الشاشة الرئيسية للتطبيق (تحتوي على نظام التنقل) ====================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GoldVisionApp() {
    var selectedKarat by remember { mutableStateOf("21K") }
    var selectedPeriod by remember { mutableStateOf("أسبوع") }
    var buyMode by remember { mutableStateOf(true) }
    var weight by remember { mutableDoubleStateOf(0.0) }
    var manufacturing by remember { mutableDoubleStateOf(35.0) }
    var selectedCountryTax by remember { mutableStateOf(countryTaxOptions.first()) }
    var taxPercent by remember { mutableDoubleStateOf(countryTaxOptions.first().vatPercent) }
    var selectedBottom by remember { mutableIntStateOf(0) }
    var showChartFull by remember { mutableStateOf(false) }
    var showDealEvaluator by remember { mutableStateOf(false) }
    var showAddGoldItem by remember { mutableStateOf(false) }
    var editingGoldItemIndex by remember { mutableStateOf<Int?>(null) }
    val savedDeals = remember { mutableStateListOf<SavedDeal>() }
    val savedGoldItems = remember { mutableStateListOf<GoldItem>().apply { addAll(loadSavedGoldItems()) } }
    var prefillGoldItem by remember { mutableStateOf<GoldItem?>(null) }
    var userProfile by remember { mutableStateOf(loadUserProfile()) }
    var showProfileScreen by remember { mutableStateOf(false) }
    var showPrivacyPolicy by remember { mutableStateOf(false) }
    var notificationSettings by remember { mutableStateOf(loadNotificationSettings()) }
    var showNotificationSettings by remember { mutableStateOf(false) }
    var signedInEmail by remember { mutableStateOf(AuthService.currentUserEmail) }
    var showAuthScreen by remember { mutableStateOf(false) }
    var showFedSchedule by remember { mutableStateOf(false) }

    var liveTimeText by remember { mutableStateOf(currentDateTimeText()) }
    LaunchedEffect(Unit) {
        while (true) {
            liveTimeText = currentDateTimeText()
            delay(30.seconds)
        }
    }

    // يجلب أسعار الذهب العالمية الحقيقية عند فتح التطبيق ثم يحدّثها
    // تلقائياً كل 30 ثانية (GoldMarket.kt أصبح يستخدم xaus.com، مزوّد
    // مصمَّم للاستطلاع المتكرر بلا حصة شهرية صارمة — على عكس
    // api.goldprice.dev السابق اللي كان سبب التوقف الحقيقي: حصة شهرية
    // 1000 طلب فقط نفدت خلال يوم الاختبار). 30 ثانية تطابق مدة التخزين
    // المؤقت المعلَنة عند المزوّد نفسه، فتحديث أسرع لن يعطي فائدة فعلية
    LaunchedEffect(Unit) {
        var backoffSeconds = 30L
        while (true) {
            GoldMarket.refresh()
            if (GoldMarket.lastError == null) {
                backoffSeconds = 30L
            } else {
                backoffSeconds = (backoffSeconds * 2).coerceAtMost(600L)
            }
            delay(backoffSeconds.seconds)
        }
    }

    // يجلب شموع الأسعار اليومية الحقيقية لآخر 30 يوماً (GoldHistory.kt)
    // — شموع يومية لا تتغيّر إلا مرة كل يوم تقريباً، فكل ساعتين كافٍ
    // جداً ويوفّر أغلب الحصة الشهرية المشتركة مع أسعار /v1/carat أعلاه
    LaunchedEffect(Unit) {
        while (true) {
            GoldHistory.refresh(todayLocalDate())
            delay(7200.seconds)
        }
    }

    // إن كان المستخدم مسجّل دخول أصلاً من جلسة سابقة، يزامن محفظته مع
    // السحابة مرة واحدة عند بدء التطبيق (وليس كل فتح شاشة)
    LaunchedEffect(Unit) {
        if (AuthService.currentUserEmail != null) {
            syncPortfolioWithCloud(savedGoldItems)
        }
    }

    // يجلب أخبار الذهب الحقيقية (GoldNews.kt) عند فتح التطبيق، ثم كل
    // 30 دقيقة — أخبار مالية لا تحتاج تحديثاً شبه لحظي كالأسعار
    LaunchedEffect(Unit) {
        while (true) {
            GoldNews.refresh()
            delay(1800.seconds)
        }
    }
    val marketScope = rememberCoroutineScope()

    val fedRows = remember { upcomingFedMeetings().take(4) }

    val selectedPrice = GoldMarket.prices.first { it.karat == selectedKarat }.price
    val beforeVat = selectedPrice * weight
    // عند "بيع" الذهب للمحل: يُدفع لك سعر الذهب الخام فقط دون مصنعية ولا
    // ضريبة (هذا هو المعتاد فعلياً — المحل لا يدفع مقابل مصنعية قطعة
    // مستعملة يشتريها منك، ولا ضريبة على هذا النوع من الشراء)، بعكس
    // "شراء" قطعة من المحل حيث تُضاف المصنعية والضريبة كاملة
    val manufacturingTotal = if (buyMode) manufacturing * weight else 0.0
    val isCalculatorTaxExempt = !buyMode || selectedKarat == "24K"
    val vat = if (isCalculatorTaxExempt) 0.0 else (beforeVat + manufacturingTotal) * (taxPercent / 100.0)
    val total = beforeVat + manufacturingTotal + vat

    // يربط زر/إيماءة الرجوع في النظام بنفس تنقّل زر الرجوع داخل التطبيق:
    // يقفل أي شاشة مفتوحة فوق التبويبات، أو يرجع لتبويب "الرئيسية" —
    // بنفس ترتيب أولوية العرض أدناه بالضبط. لو ما فيه شيء مفتوح، الزر/
    // الإيماءة تترك للنظام (يخرج من التطبيق كالمعتاد)
    val hasOverlayScreen = showChartFull || showDealEvaluator || showAddGoldItem ||
        showProfileScreen || showAuthScreen || showPrivacyPolicy || showNotificationSettings ||
        showFedSchedule || selectedBottom != 0
    BackHandler(enabled = hasOverlayScreen) {
        when {
            showChartFull -> showChartFull = false
            showDealEvaluator -> showDealEvaluator = false
            showAddGoldItem -> {
                showAddGoldItem = false
                editingGoldItemIndex = null
                prefillGoldItem = null
            }
            showProfileScreen -> showProfileScreen = false
            showAuthScreen -> {
                showAuthScreen = false
                showProfileScreen = true
            }
            showPrivacyPolicy -> showPrivacyPolicy = false
            showNotificationSettings -> showNotificationSettings = false
            showFedSchedule -> showFedSchedule = false
            selectedBottom != 0 -> selectedBottom = 0
        }
    }

    // سحب للأسفل للتحديث، متاح في كل الصفحات لأنه يلفّ منطقة المحتوى
    // المشتركة كلها — يحدّث نفس الأسعار الحية والتاريخية المستخدَمة في
    // كامل التطبيق بغض النظر عن الصفحة المفتوحة حالياً
    var isRefreshing by remember { mutableStateOf(false) }

    // يغلق أي شاشة (طبقة) مفتوحة فوق التبويبات — لازم يُستدعى قبل فتح
    // شاشة جديدة من الجرس/الحساب في الشريط العلوي، وإلا تبقى الشاشة
    // القديمة (مثل "إضافة قطعة" أو "تسجيل الدخول") ظاهرة فوق الجديدة لأن
    // شرط if/else الأول اللي لسه true هو اللي يُعرض (نفس المنطق المستخدم
    // أصلاً في BottomNav.onSelected أدناه)
    fun closeOverlayScreens() {
        showChartFull = false
        showDealEvaluator = false
        showAddGoldItem = false
        editingGoldItemIndex = null
        showProfileScreen = false
        showAuthScreen = false
        showPrivacyPolicy = false
        showNotificationSettings = false
        showFedSchedule = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Header(
            showNotificationBadge = !notificationSettings.dailyPriceEnabled,
            onNotificationsClick = {
                closeOverlayScreens()
                showNotificationSettings = true
            },
            onAccountClick = {
                closeOverlayScreens()
                if (signedInEmail != null) showProfileScreen = true else showAuthScreen = true
            }
        )

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                marketScope.launch {
                    GoldMarket.refresh()
                    GoldHistory.refresh(todayLocalDate())
                    GoldNews.refresh()
                    isRefreshing = false
                }
            },
            modifier = Modifier.weight(1f)
        ) {
            if (showChartFull) {
                PriceChartFullScreen(
                    selectedPeriod = selectedPeriod,
                    onPeriodSelected = { selectedPeriod = it },
                    selectedKarat = selectedKarat,
                    onBack = { showChartFull = false }
                )
            } else if (showDealEvaluator) {
                DealEvaluatorScreen(
                    initialKarat = selectedKarat,
                    initialWeight = weight,
                    manufacturing = manufacturing,
                    onBack = { showDealEvaluator = false },
                    onSaveDeal = { deal -> savedDeals.add(0, deal) }
                )
            } else if (showAddGoldItem) {
                val editingIndex = editingGoldItemIndex
                AddGoldItemScreen(
                    editingItem = editingIndex?.let { savedGoldItems.getOrNull(it) },
                    prefillItem = prefillGoldItem,
                    onBack = {
                        showAddGoldItem = false
                        editingGoldItemIndex = null
                        prefillGoldItem = null
                    },
                    onSave = { item ->
                        if (editingIndex != null && editingIndex in savedGoldItems.indices) {
                            savedGoldItems[editingIndex] = item
                        } else {
                            savedGoldItems.add(0, item)
                        }
                        persistGoldItems(savedGoldItems)
                        uploadPortfolioIfSignedIn(savedGoldItems, marketScope)
                        // الحفظ القادم من الحاسبة (prefillGoldItem) ينقل تلقائياً
                        // إلى شاشة المحفظة، حتى يرى المستخدم القطعة فور حفظها
                        if (prefillGoldItem != null) {
                            selectedBottom = 3
                        }
                        showAddGoldItem = false
                        editingGoldItemIndex = null
                        prefillGoldItem = null
                    },
                    onDelete = if (editingIndex != null) {
                        {
                            if (editingIndex in savedGoldItems.indices) {
                                savedGoldItems.removeAt(editingIndex)
                            }
                            persistGoldItems(savedGoldItems)
                        uploadPortfolioIfSignedIn(savedGoldItems, marketScope)
                            showAddGoldItem = false
                            editingGoldItemIndex = null
                            prefillGoldItem = null
                        }
                    } else null
                )
            } else if (showProfileScreen) {
                ProfileScreen(
                    profile = userProfile,
                    signedInEmail = signedInEmail,
                    onNavigateAuth = {
                        showProfileScreen = false
                        showAuthScreen = true
                    },
                    onSignOut = {
                        AuthService.signOut()
                        signedInEmail = AuthService.currentUserEmail
                    },
                    onBack = { showProfileScreen = false },
                    onSave = { profile ->
                        userProfile = profile
                        persistUserProfile(profile)
                        showProfileScreen = false
                    }
                )
            } else if (showAuthScreen) {
                AuthScreen(
                    onBack = {
                        showAuthScreen = false
                        showProfileScreen = true
                    },
                    onAuthSuccess = {
                        signedInEmail = AuthService.currentUserEmail
                        showAuthScreen = false
                        showProfileScreen = true
                        marketScope.launch { syncPortfolioWithCloud(savedGoldItems) }
                    }
                )
            } else if (showPrivacyPolicy) {
                PrivacyPolicyScreen(onBack = { showPrivacyPolicy = false })
            } else if (showNotificationSettings) {
                NotificationSettingsScreen(
                    settings = notificationSettings,
                    onBack = { showNotificationSettings = false },
                    onToggleDailyPrice = { enabled ->
                        notificationSettings = notificationSettings.copy(dailyPriceEnabled = enabled)
                        persistNotificationSettings(notificationSettings)
                        PriceNotificationScheduler.setEnabled(enabled)
                    },
                    onToggleFedMeetingAlerts = { enabled ->
                        notificationSettings = notificationSettings.copy(fedMeetingAlertsEnabled = enabled)
                        persistNotificationSettings(notificationSettings)
                        FedMeetingNotificationScheduler.setEnabled(enabled)
                    }
                )
            } else if (showFedSchedule) {
                FedMeetingsScreen(onBack = { showFedSchedule = false })
            } else {
                when (selectedBottom) {
                    0 -> HomeScreen(
                        liveTimeText = liveTimeText,
                        selectedKarat = selectedKarat,
                        onKaratSelected = { selectedKarat = it },
                        selectedPeriod = selectedPeriod,
                        onPeriodSelected = { selectedPeriod = it },
                        buyMode = buyMode,
                        onBuyModeChanged = { buyMode = it },
                        weight = weight,
                        onWeightChanged = { weight = it },
                        manufacturing = manufacturing,
                        onManufacturingChanged = { manufacturing = it },
                        beforeVat = beforeVat,
                        vat = vat,
                        total = total,
                        fedRows = fedRows,
                        savedGoldItems = savedGoldItems,
                        onNavigateCalculator = { selectedBottom = 1 },
                        onNavigateChart = { showChartFull = true },
                        onNavigateNews = { selectedBottom = 2 },
                        onNavigatePortfolio = { selectedBottom = 3 },
                        onNavigateFedSchedule = { showFedSchedule = true }
                    )
                    1 -> CalculatorFullScreen(
                        selectedKarat = selectedKarat,
                        buyMode = buyMode,
                        weight = weight,
                        manufacturing = manufacturing,
                        beforeVat = beforeVat,
                        vat = vat,
                        total = total,
                        selectedCountryTax = selectedCountryTax,
                        onCountrySelected = { selectedCountryTax = it },
                        taxPercent = taxPercent,
                        onTaxPercentChanged = { taxPercent = it },
                        isTaxExempt = isCalculatorTaxExempt,
                        onBuyModeChanged = { buyMode = it },
                        onKaratChanged = { selectedKarat = it },
                        onWeightChanged = { weight = it },
                        onManufacturingChanged = { manufacturing = it },
                        onNavigateDealEvaluator = { showDealEvaluator = true },
                        savedDeals = savedDeals,
                        onSaveToPortfolio = { item ->
                            prefillGoldItem = item
                            showAddGoldItem = true
                        },
                        onBack = { selectedBottom = 0 }
                    )
                    2 -> NewsScreen(onBack = { selectedBottom = 0 })
                    3 -> PortfolioScreen(
                        savedItems = savedGoldItems,
                        onNavigateAddItem = { showAddGoldItem = true },
                        onEditItem = { index ->
                            editingGoldItemIndex = index
                            showAddGoldItem = true
                        },
                        onBack = { selectedBottom = 0 }
                    )
                    4 -> ZakatScreen(
                        savedItems = savedGoldItems,
                        onNavigateAddItem = { showAddGoldItem = true },
                        onBack = { selectedBottom = 0 }
                    )
                    5 -> MoreScreen(
                        profile = userProfile,
                        signedInEmail = signedInEmail,
                        onNavigateProfile = { showProfileScreen = true },
                        onNavigatePrivacyPolicy = { showPrivacyPolicy = true },
                        onNavigateNotifications = { showNotificationSettings = true },
                        onBack = { selectedBottom = 0 }
                    )
                }
            }
        }

        BottomNav(
            selected = selectedBottom,
            onSelected = { index ->
                closeOverlayScreens()
                selectedBottom = index
            }
        )
    }
}

// ==================== محتوى الصفحة الرئيسية ====================
@Composable
private fun HomeScreen(
    liveTimeText: String,
    selectedKarat: String,
    onKaratSelected: (String) -> Unit,
    selectedPeriod: String,
    onPeriodSelected: (String) -> Unit,
    buyMode: Boolean,
    onBuyModeChanged: (Boolean) -> Unit,
    weight: Double,
    onWeightChanged: (Double) -> Unit,
    manufacturing: Double,
    onManufacturingChanged: (Double) -> Unit,
    beforeVat: Double,
    vat: Double,
    total: Double,
    fedRows: List<FedMeetingRow>,
    savedGoldItems: List<GoldItem>,
    onNavigateCalculator: () -> Unit,
    onNavigateChart: () -> Unit,
    onNavigateNews: () -> Unit,
    onNavigatePortfolio: () -> Unit,
    onNavigateFedSchedule: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp)
    ) {
        LiveStatus(updateText = liveTimeText)

        PriceCards(selectedKarat, onKaratSelected = onKaratSelected)

        Spacer(Modifier.height(5.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(315.dp)
                    .clickable { onNavigateCalculator() }
            ) {
                GoldCalculator(
                    selectedKarat = selectedKarat,
                    buyMode = buyMode,
                    weight = weight,
                    manufacturing = manufacturing,
                    beforeVat = beforeVat,
                    vat = vat,
                    total = total,
                    onBuyModeChanged = onBuyModeChanged,
                    onKaratChanged = onKaratSelected,
                    onWeightChanged = onWeightChanged,
                    onManufacturingChanged = onManufacturingChanged,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(315.dp)
            ) {
                PriceChart(
                    selectedPeriod = selectedPeriod,
                    onPeriodSelected = onPeriodSelected,
                    selectedKarat = selectedKarat,
                    onChartClick = onNavigateChart
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(225.dp)
                    .clickable { onNavigateNews() }
            ) {
                ImportantNews()
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(225.dp)
                    .clickable { onNavigateFedSchedule() }
            ) {
                FedSchedule(rows = fedRows)
            }
        }

        Spacer(Modifier.height(4.dp))

        Box(modifier = Modifier.clickable { onNavigatePortfolio() }) {
            val homeTotals = portfolioTotals(savedGoldItems)
            PortfolioSummary(
                totalValue = homeTotals.totalValue,
                itemCount = homeTotals.itemCount,
                totalWeight = homeTotals.totalWeight,
                totalCost = homeTotals.totalCost
            )
        }

        Spacer(Modifier.height(6.dp))
    }
}

// ==================== شاشة حاسبة الذهب الكاملة ====================
internal fun karatLabel(karat: String): String {
    val number = karat.removeSuffix("K")
    return t("$number عيار", "${number}K")
}

@Composable
private fun CalculatorFullScreen(
    selectedKarat: String,
    buyMode: Boolean,
    weight: Double,
    manufacturing: Double,
    beforeVat: Double,
    vat: Double,
    total: Double,
    selectedCountryTax: CountryTaxOption,
    onCountrySelected: (CountryTaxOption) -> Unit,
    taxPercent: Double,
    onTaxPercentChanged: (Double) -> Unit,
    isTaxExempt: Boolean,
    onBuyModeChanged: (Boolean) -> Unit,
    onKaratChanged: (String) -> Unit,
    onWeightChanged: (Double) -> Unit,
    onManufacturingChanged: (Double) -> Unit,
    onNavigateDealEvaluator: () -> Unit,
    savedDeals: List<SavedDeal>,
    onSaveToPortfolio: (GoldItem) -> Unit,
    onBack: () -> Unit
) {
    val selectedPrice = GoldMarket.prices.first { it.karat == selectedKarat }
    val marketScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Icon(
                imageVector = Icons.Outlined.Refresh,
                contentDescription = t("تحديث", "Refresh"),
                tint = Gold,
                modifier = Modifier
                    .size(22.dp)
                    .align(Alignment.CenterStart)
                    .clickable { marketScope.launch { GoldMarket.refresh() } }
            )
            Text(
                t("الحاسبة", "Calculator"),
                color = White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = t("رجوع", "Back"),
                tint = Gold,
                modifier = Modifier
                    .size(22.dp)
                    .align(Alignment.CenterEnd)
                    .clickable { onBack() }
            )
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, Border, RoundedCornerShape(10.dp))
        ) {
            CalculatorMode(
                text = t("بيع", "Sell"),
                selected = !buyMode,
                modifier = Modifier.weight(1f)
            ) { onBuyModeChanged(false) }

            CalculatorMode(
                text = t("شراء", "Buy"),
                selected = buyMode,
                modifier = Modifier.weight(1f)
            ) { onBuyModeChanged(true) }
        }

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("24K", "21K", "22K", "18K").forEach { k ->
                ChoiceButton(
                    text = karatLabel(k),
                    selected = k == selectedKarat,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                ) { onKaratChanged(k) }
            }
        }

        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "▲ ${fmt(selectedPrice.percent, 2)}%",
                    color = Green,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(t("تحديث منذ دقائق", "Updated minutes ago"), color = Gray, fontSize = 9.sp)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(t("سعر جرام الذهب", "Gold price per gram"), color = Gray, fontSize = 10.sp)
                Text(
                    fmt(selectedPrice.price, 2),
                    color = Gold,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Icon(
                imageVector = Icons.Outlined.Share,
                contentDescription = t("تصدير PDF", "Export PDF"),
                tint = Gold,
                modifier = Modifier
                    .size(18.dp)
                    .clickable {
                        val manufacturingTotal = if (buyMode) manufacturing * weight else 0.0
                        PdfExport.exportReport(
                            title = t("تقرير الصفقة — Gold Vision", "Deal Report — Gold Vision"),
                            generatedAt = t("تاريخ التصدير: ${todayDateText()}", "Export date: ${todayDateText()}"),
                            summary = listOf(
                                PdfReportRow(t("نوع العملية", "Transaction type"), if (buyMode) t("شراء", "Buy") else t("بيع", "Sell")),
                                PdfReportRow(t("العيار", "Karat"), karatLabel(selectedKarat)),
                                PdfReportRow(t("الوزن", "Weight"), "${fmt(weight, 2)} ${t("جرام", "g")}"),
                                PdfReportRow(t("الإجمالي (شامل الضريبة)", "Total (incl. tax)"), "${fmt(total, 2, grouped = true)} ${t("ريال", "SAR")}")
                            ),
                            rows = buildList {
                                add(PdfReportRow(t("سعر الذهب", "Gold price"), "${fmt(beforeVat, 2, grouped = true)} ${t("ريال", "SAR")}"))
                                if (buyMode) {
                                    add(PdfReportRow(t("المصنعية (للجرام)", "Workmanship (per gram)"), "${fmt(manufacturing, 2)} ${t("ريال", "SAR")}"))
                                    add(PdfReportRow(t("إجمالي المصنعية", "Total workmanship"), "${fmt(manufacturingTotal, 2, grouped = true)} ${t("ريال", "SAR")}"))
                                } else {
                                    add(PdfReportRow(t("المصنعية", "Workmanship"), "0.00 ${t("ريال", "SAR")}"))
                                }
                                add(
                                    PdfReportRow(
                                        if (isTaxExempt) t("ضريبة القيمة المضافة (معفى)", "VAT (exempt)") else t("ضريبة القيمة المضافة (${fmt(taxPercent, 0)}%)", "VAT (${fmt(taxPercent, 0)}%)"),
                                        "${fmt(vat, 2, grouped = true)} ${t("ريال", "SAR")}"
                                    )
                                )
                            }
                        )
                    }
            )
        }

        Spacer(Modifier.height(14.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, Border, RoundedCornerShape(10.dp))
                .padding(10.dp)
        ) {
            Text(
                t("الوزن (جرام)", "Weight (grams)"),
                color = Gray,
                fontSize = 10.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SmallActionButton("−") {
                    onWeightChanged((weight - 1).coerceAtLeast(0.1))
                }
                NumericInputField(
                    value = weight,
                    onValueChanged = onWeightChanged,
                    fontSize = 18.sp,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
                SmallActionButton("+") {
                    onWeightChanged(weight + 1)
                }
            }
        }

        if (!buyMode) {
            Spacer(Modifier.height(6.dp))
            Text(
                t("سعر البيع للمحل: قيمة الذهب فقط - بدون مصنعية أو ضريبة", "Sell price to shop: gold value only - no workmanship or tax"),
                color = Gray,
                fontSize = 10.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        } else if (manufacturing <= 0.0) {
            Spacer(Modifier.height(6.dp))
            Text(
                t("ذهب خالص - بدون مصنعية أو ضريبة", "Pure gold - no workmanship or tax"),
                color = Gray,
                fontSize = 10.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(14.dp))

        CountryTaxSelector(
            selectedCountry = selectedCountryTax,
            onCountrySelected = onCountrySelected,
            taxPercent = taxPercent,
            onTaxPercentChanged = onTaxPercentChanged,
            isTaxExempt = isTaxExempt
        )

        Spacer(Modifier.height(14.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, Border, RoundedCornerShape(12.dp))
                .background(CardBlack)
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Green)
                    )
                    Text(t("مباشر", "Live"), color = White, fontSize = 11.sp)
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF123321))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(t("صفقة ممتازة", "Great deal"), color = Green, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(10.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(t("الإجمالي (شامل الضريبة)", "Total (incl. tax)"), color = Gray, fontSize = 11.sp)
                Text(
                    "${fmt(total, 2, grouped = true)} ${t("ريال", "SAR")}",
                    color = Gold,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, Border, RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        if (isTaxExempt) t("معفى من الضريبة", "Tax exempt") else "${countryDisplayName(selectedCountryTax)} · ${fmt(taxPercent, 0)}%",
                        color = Gray,
                        fontSize = 9.sp
                    )
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = Gray,
                        modifier = Modifier.size(11.dp)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Border)
            )
            Spacer(Modifier.height(10.dp))

            CalculatorRow(t("سعر الذهب", "Gold price"), "${fmt(beforeVat, 2, grouped = true)} ${t("ريال", "SAR")}")
            if (buyMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(t("المصنعية (للجرام) ✎", "Workmanship (per gram) ✎"), color = White, fontSize = 10.sp)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        NumericInputField(
                            value = manufacturing,
                            onValueChanged = { onManufacturingChanged(it.coerceAtMost(500.0)) },
                            fontSize = 10.sp,
                            minValue = 0.0,
                            modifier = Modifier
                                .width(50.dp)
                                .height(18.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .border(1.dp, Border, RoundedCornerShape(5.dp))
                        )
                        Text(t("ريال", "SAR"), color = Gray, fontSize = 9.sp)
                    }
                }
                CalculatorRow(t("إجمالي المصنعية", "Total workmanship"), "${fmt(manufacturing * weight, 2, grouped = true)} ${t("ريال", "SAR")}")
            } else {
                CalculatorRow(t("المصنعية", "Workmanship"), "0.00 ${t("ريال", "SAR")}")
            }
            CalculatorRow(
                if (isTaxExempt) t("ضريبة القيمة المضافة (معفى)", "VAT (exempt)") else t("ضريبة القيمة المضافة (${fmt(taxPercent, 0)}%)", "VAT (${fmt(taxPercent, 0)}%)"),
                "${fmt(vat, 2, grouped = true)} ${t("ريال", "SAR")}"
            )
        }

        Spacer(Modifier.height(14.dp))

        SaveToPortfolioBox {
            onSaveToPortfolio(
                GoldItem(
                    name = "",
                    emoji = pieceEmojiOptions.first().first,
                    karat = selectedKarat,
                    weightGrams = weight,
                    purchasePriceWithTax = total,
                    manufacturingPerGram = if (buyMode) manufacturing else 0.0,
                    purchaseDate = todayDateText(),
                    notes = "",
                    isSold = !buyMode
                )
            )
        }

        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, Gold, RoundedCornerShape(10.dp))
                .clickable { onNavigateDealEvaluator() },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    if (buyMode) t("المحل أعطاك سعراً؟", "Shop gave you a price?") else t("المحل أعطاك سعراً للشراء؟", "Shop gave you a buy price?"),
                    color = Gold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = Gold,
                    modifier = Modifier.size(15.dp)
                )
            }
        }

        if (savedDeals.isNotEmpty()) {
            Spacer(Modifier.height(14.dp))
            Text(t("الأسعار المحفوظة", "Saved Prices"), color = Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, Border, RoundedCornerShape(10.dp))
                    .background(CardBlack)
            ) {
                savedDeals.forEachIndexed { index, deal ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Store,
                                contentDescription = null,
                                tint = deal.tierColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Column {
                                Text(deal.shopName, color = White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(deal.tierLabel, color = deal.tierColor, fontSize = 9.sp)
                            }
                        }
                        Text(
                            "${fmt(deal.totalPrice, 2, grouped = true)} ${t("ريال", "SAR")}",
                            color = Gold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (index != savedDeals.lastIndex) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp)
                                .height(1.dp)
                                .background(Border)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

// ==================== شاشة "المحل أعطاك سعراً؟" (تقييم عرض المحل) ====================
@Composable
private fun DealEvaluatorScreen(
    initialKarat: String,
    initialWeight: Double,
    manufacturing: Double,
    onBack: () -> Unit,
    onSaveDeal: (SavedDeal) -> Unit
) {
    var karat by remember { mutableStateOf(initialKarat) }
    var weight by remember { mutableDoubleStateOf(initialWeight) }
    var shopPrice by remember { mutableDoubleStateOf(3000.0) }
    var includingTax by remember { mutableStateOf(true) }
    var showMore by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var dealCountryTax by remember { mutableStateOf(countryTaxOptions.first()) }
    var dealTaxPercent by remember { mutableDoubleStateOf(countryTaxOptions.first().vatPercent) }
    var showKaratPicker by remember { mutableStateOf(false) }
    val isDealTaxExempt = karat == "24K"

    val karatPrice = GoldMarket.prices.first { it.karat == karat }.price
    val fairBeforeVat = karatPrice * weight
    val fairManufacturing = manufacturing * weight
    val fairSubtotal = fairBeforeVat + fairManufacturing
    val fairVat = if (isDealTaxExempt) 0.0 else fairSubtotal * (dealTaxPercent / 100.0)
    val fairTotal = fairSubtotal + fairVat

    val shopPriceWithTax = if (isDealTaxExempt || includingTax) shopPrice else shopPrice * (1 + dealTaxPercent / 100.0)
    val savings = fairTotal - shopPriceWithTax
    val ratio = if (fairTotal > 0) (shopPriceWithTax / fairTotal).toFloat() else 1f

    val (tierLabel, tierColor) = when {
        ratio <= 1.0f -> t("صفقة ممتازة", "Great deal") to Green
        ratio <= 1.05f -> t("سعر عادل", "Fair price") to Yellow
        else -> t("سعر مرتفع", "High price") to Red
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = t("رجوع", "Back"),
                    tint = Gold,
                    modifier = Modifier
                        .size(22.dp)
                        .clickable { onBack() }
                )
                Text(
                    t("المحل أعطاك سعراً؟", "Shop gave you a price?"),
                    color = White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = null,
                    tint = Gray,
                    modifier = Modifier
                        .size(22.dp)
                        .rotate(180f)
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(t("العيار", "Karat"), color = Gray, fontSize = 10.sp)
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .border(1.dp, Border, RoundedCornerShape(9.dp))
                    .clickable { showKaratPicker = true }
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(karatLabel(karat), color = White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("˅", color = Gold, fontSize = 13.sp)
            }

            Spacer(Modifier.height(12.dp))

            Text(t("الوزن (جرام)", "Weight (grams)"), color = Gray, fontSize = 10.sp)
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .border(1.dp, Border, RoundedCornerShape(9.dp)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SmallActionButton("−") { weight = (weight - 1).coerceAtLeast(0.1) }
                NumericInputField(
                    value = weight,
                    onValueChanged = { weight = it },
                    fontSize = 16.sp,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
                SmallActionButton("+") { weight += 1 }
            }

            Spacer(Modifier.height(12.dp))

            CountryTaxSelector(
                selectedCountry = dealCountryTax,
                onCountrySelected = { dealCountryTax = it },
                taxPercent = dealTaxPercent,
                onTaxPercentChanged = { dealTaxPercent = it },
                isTaxExempt = isDealTaxExempt
            )

            Spacer(Modifier.height(6.dp))
            Text(
                t("إظهار المزيد", "Show more"),
                color = Gray,
                fontSize = 10.sp,
                modifier = Modifier.clickable { showMore = !showMore }
            )

            if (showMore) {
                Spacer(Modifier.height(6.dp))
                Text(
                    t("بيانات إضافية عن الحلية ستظهر هنا قريباً", "More details about the item will appear here soon"),
                    color = Gray,
                    fontSize = 10.sp
                )
            }

            Spacer(Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Border)
            )
            Spacer(Modifier.height(14.dp))

            Text(t("عرض المحل (ريال)", "Shop offer (SAR)"), color = Gray, fontSize = 10.sp)
            Spacer(Modifier.height(6.dp))
            NumericInputField(
                value = shopPrice,
                onValueChanged = { shopPrice = it },
                fontSize = 20.sp,
                minValue = 0.0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .border(1.dp, Border, RoundedCornerShape(9.dp))
            )

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Switch(
                    checked = includingTax,
                    onCheckedChange = { includingTax = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Black,
                        checkedTrackColor = Gold,
                        uncheckedThumbColor = Gray,
                        uncheckedTrackColor = CardBlack
                    )
                )
                Text(t("شامل الضريبة؟", "Tax included?"), color = White, fontSize = 12.sp)
            }

            Spacer(Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Border, RoundedCornerShape(12.dp))
                    .background(CardBlack)
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(tierColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(tierLabel, color = tierColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(10.dp))

                Text(t("السعر العادل (شامل الضريبة)", "Fair price (incl. tax)"), color = Gray, fontSize = 11.sp)
                Text(
                    "${fmt(fairTotal, 2, grouped = true)} ${t("ريال", "SAR")}",
                    color = Gold,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(4.dp))
                if (savings >= 0) {
                    Text(t("تدفع أقل من العادل", "You pay less than fair"), color = Green, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(
                        t("وفرت ${fmt(savings, 2, grouped = true)} ريال", "You saved ${fmt(savings, 2, grouped = true)} SAR"),
                        color = Green,
                        fontSize = 10.sp
                    )
                } else {
                    Text(t("تدفع أكثر من العادل", "You pay more than fair"), color = Red, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(
                        t("زيادة ${fmt(-savings, 2, grouped = true)} ريال", "Extra ${fmt(-savings, 2, grouped = true)} SAR"),
                        color = Red,
                        fontSize = 10.sp
                    )
                }

                Spacer(Modifier.height(12.dp))

                DealGauge(ratio = ratio)

                Spacer(Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Border)
                )
                Spacer(Modifier.height(10.dp))

                CalculatorRow(t("الإجمالي (بدون ضريبة)", "Total (excl. tax)"), "${fmt(fairSubtotal, 2, grouped = true)} ${t("ريال", "SAR")}")
                CalculatorRow(t("الإجمالي (شامل الضريبة)", "Total (incl. tax)"), "${fmt(fairTotal, 2, grouped = true)} ${t("ريال", "SAR")}")
                CalculatorRow(t("سعر الذهب", "Gold price"), "${fmt(fairBeforeVat, 2, grouped = true)} ${t("ريال", "SAR")}")
                CalculatorRow(t("المصنعية", "Workmanship"), "${fmt(fairManufacturing, 2, grouped = true)} ${t("ريال", "SAR")}")
                val shopMargin = shopPriceWithTax - fairTotal
                val shopMarginPerGram = if (weight > 0) shopMargin / weight else 0.0
                CalculatorRow(
                    t("ريع المحل", "Shop margin"),
                    "${fmt(shopMargin, 2, grouped = true)} ${t("ريال", "SAR")} (${fmt(shopMarginPerGram, 2)} ${t("/جم", "/g")})"
                )
                CalculatorRow(
                    if (isDealTaxExempt) t("ضريبة القيمة المضافة (معفى)", "VAT (exempt)") else t("ضريبة القيمة المضافة (${fmt(dealTaxPercent, 0)}%)", "VAT (${fmt(dealTaxPercent, 0)}%)"),
                    "${fmt(fairVat, 2, grouped = true)} ${t("ريال", "SAR")}"
                )
            }

            Spacer(Modifier.height(14.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Border, RoundedCornerShape(12.dp))
                    .background(CardBlack)
                    .padding(14.dp)
            ) {
                Text(t("أسعار للتفاوض (شامل الضريبة)", "Negotiation prices (incl. tax)"), color = White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))

                NegotiationRow(t("صفقة ممتازة", "Great deal"), fairTotal, shopPriceWithTax, Green)
                NegotiationRow(t("سعر عادل", "Fair price"), fairTotal * 1.05, shopPriceWithTax, Yellow)
                NegotiationRow(t("الحد الأقصى", "Maximum"), fairTotal * 1.10, shopPriceWithTax, Red)
            }

            Spacer(Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Gold)
                    .clickable { showSaveDialog = true },
                contentAlignment = Alignment.Center
            ) {
                Text(t("حفظ في المجموعة", "Save to collection"), color = Black, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))
        }

        if (showSaveDialog) {
            SaveDealDialog(
                totalPrice = shopPriceWithTax,
                tierLabel = tierLabel,
                tierColor = tierColor,
                onDismiss = { showSaveDialog = false },
                onConfirm = { shopName ->
                    onSaveDeal(
                        SavedDeal(
                            shopName = shopName,
                            totalPrice = shopPriceWithTax,
                            tierLabel = tierLabel,
                            tierColor = tierColor
                        )
                    )
                    showSaveDialog = false
                }
            )
        }

        if (showKaratPicker) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.65f))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { showKaratPicker = false },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 28.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, Border, RoundedCornerShape(14.dp))
                        .background(CardBlack)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { }
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        t("اختر العيار", "Choose karat"),
                        color = White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        textAlign = TextAlign.End
                    )
                    listOf("24K", "22K", "21K", "18K").forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    karat = option
                                    showKaratPicker = false
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                karatLabel(option),
                                color = if (option == karat) Gold else White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (option == karat) {
                                Text("✓", color = Gold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// نافذة صغيرة لإدخال اسم المحل قبل الحفظ في القائمة
@Composable
private fun SaveDealDialog(
    totalPrice: Double,
    tierLabel: String,
    tierColor: Color,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var shopName by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable(
                indication = null,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            ) { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 28.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, Border, RoundedCornerShape(14.dp))
                .background(CardBlack)
                .clickable(
                    indication = null,
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                ) { }
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(t("حفظ السعر", "Save Price"), color = White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = t("إغلاق", "Close"),
                    tint = Gray,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { onDismiss() }
                )
            }

            Spacer(Modifier.height(6.dp))
            Text(
                "${fmt(totalPrice, 2, grouped = true)} ${t("ريال", "SAR")} • $tierLabel",
                color = tierColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(14.dp))
            Text(t("اسم المحل", "Shop name"), color = Gray, fontSize = 10.sp)
            Spacer(Modifier.height(6.dp))

            SelectableTextField(
                value = shopName,
                onValueChange = { shopName = it },
                placeholder = t("مثال: مجوهرات الأصيل", "e.g. Al-Asil Jewelry"),
                modifier = Modifier.fillMaxWidth().height(42.dp)
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .border(1.dp, Border, RoundedCornerShape(9.dp))
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(t("إلغاء", "Cancel"), color = Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(Gold)
                        .clickable {
                            val finalName = shopName.trim().ifEmpty { t("محل بدون اسم", "Unnamed shop") }
                            onConfirm(finalName)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(t("حفظ", "Save"), color = Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// مقياس أفقي (ممتاز - عادل - مرتفع) مع مؤشر دائري يبيّن موقع سعر المحل
@Composable
private fun DealGauge(ratio: Float) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(t("ممتاز", "Great"), color = Green, fontSize = 9.sp)
            Text(t("عادل", "Fair"), color = Yellow, fontSize = 9.sp)
            Text(t("مرتفع", "High"), color = Red, fontSize = 9.sp)
        }
        Spacer(Modifier.height(4.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
        ) {
            val barHeight = size.height
            val third = size.width / 3f
            drawRoundRect(
                color = Green,
                topLeft = Offset(0f, 0f),
                size = androidx.compose.ui.geometry.Size(third, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(barHeight / 2f, barHeight / 2f)
            )
            drawRect(
                color = Yellow,
                topLeft = Offset(third, 0f),
                size = androidx.compose.ui.geometry.Size(third, barHeight)
            )
            drawRoundRect(
                color = Red,
                topLeft = Offset(third * 2f, 0f),
                size = androidx.compose.ui.geometry.Size(third, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(barHeight / 2f, barHeight / 2f)
            )

            val clampedRatio = ratio.coerceIn(0.85f, 1.15f)
            val position = (clampedRatio - 0.85f) / (1.15f - 0.85f)
            val indicatorX = (size.width * position).coerceIn(6f, size.width - 6f)
            drawCircle(color = White, radius = barHeight * 0.9f, center = Offset(indicatorX, barHeight / 2f))
            drawCircle(color = Black, radius = barHeight * 0.45f, center = Offset(indicatorX, barHeight / 2f))
        }
    }
}

// سطر ضمن بطاقة "أسعار للتفاوض": اسم المستوى + سعره + مقدار التوفير مقارنة بعرض المحل
@Composable
private fun NegotiationRow(label: String, price: Double, shopPriceWithTax: Double, tint: Color) {
    val savings = shopPriceWithTax - price
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(tint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(tint)
                )
            }
            Text(label, color = White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                "${fmt(price, 2, grouped = true)} ${t("ريال", "SAR")}",
                color = White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            if (savings > 0) {
                Text(t("وفر ${fmt(savings, 2, grouped = true)}", "Save ${fmt(savings, 2, grouped = true)}"), color = Green, fontSize = 9.sp)
            }
        }
    }
}

// ==================== شاشة "إضافة قطعة" (تُحفظ في المحفظة والزكاة معاً) ====================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddGoldItemScreen(
    editingItem: GoldItem?,
    onBack: () -> Unit,
    onSave: (GoldItem) -> Unit,
    onDelete: (() -> Unit)? = null,
    prefillItem: GoldItem? = null
) {
    val isEditing = editingItem != null
    // عند الحفظ من الحاسبة (prefillItem) تُملأ الحقول بنفس قيم الحساب
    // الأخير، لكن هذه تبقى "إضافة" جديدة وليست تعديلاً — بلا زر حذف
    val initialValues = editingItem ?: prefillItem
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf(initialValues?.name ?: "") }
    var selectedEmoji by remember { mutableStateOf(initialValues?.emoji ?: pieceEmojiOptions.first().first) }
    var karat by remember { mutableStateOf(initialValues?.karat ?: "21K") }
    var weight by remember { mutableDoubleStateOf(initialValues?.weightGrams ?: 5.0) }
    // عند التعديل، السعر المحفوظ (purchasePriceWithTax) شامل الضريبة أصلاً
    var purchasePrice by remember { mutableDoubleStateOf(initialValues?.purchasePriceWithTax ?: 3000.0) }
    var includingTax by remember { mutableStateOf(true) }
    var manufacturing by remember { mutableDoubleStateOf(initialValues?.manufacturingPerGram ?: 35.0) }
    var purchaseDate by remember { mutableStateOf(initialValues?.purchaseDate ?: todayDateText()) }
    var notes by remember { mutableStateOf(initialValues?.notes ?: "") }
    var isSold by remember { mutableStateOf(initialValues?.isSold ?: false) }
    var showDatePicker by remember { mutableStateOf(false) }

    // الذهب الاستثماري عيار 24 (سبائك/عملات) معفى من ضريبة القيمة المضافة
    // في السعودية، على عكس المشغولات (22/21/18)، فلا داعي لبلد الصنع أو
    // خيار "شامل الضريبة" هنا أصلاً
    val isTaxExempt = karat == "24K"

    val karatPrice = GoldMarket.prices.first { it.karat == karat }.price
    val currentBeforeVat = karatPrice * weight
    val currentManufacturing = manufacturing * weight
    val currentSubtotal = currentBeforeVat + currentManufacturing
    val currentVat = if (isTaxExempt) 0.0 else currentSubtotal * 0.15
    val currentTotal = currentSubtotal + currentVat

    val purchasePriceWithTax = when {
        isTaxExempt -> purchasePrice
        includingTax -> purchasePrice
        else -> purchasePrice * 1.15
    }
    val profit = currentTotal - purchasePriceWithTax
    val profitPercent = if (purchasePriceWithTax > 0) (profit / purchasePriceWithTax) * 100.0 else 0.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = t("رجوع", "Back"),
                tint = Gold,
                modifier = Modifier
                    .size(22.dp)
                    .clickable { onBack() }
            )
            Text(
                if (isEditing) t("تعديل القطعة", "Edit Item") else t("إضافة قطعة", "Add Item"),
                color = White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            if (isEditing && onDelete != null) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = t("حذف القطعة", "Delete Item"),
                    tint = Red,
                    modifier = Modifier
                        .size(22.dp)
                        .clickable { showDeleteConfirm = true }
                )
            } else {
                Spacer(Modifier.size(22.dp))
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(t("اسم القطعة", "Item Name"), color = Gray, fontSize = 10.sp)
        Spacer(Modifier.height(6.dp))
        SelectableTextField(
            value = name,
            onValueChange = { name = it },
            placeholder = t("مثال: خاتم - سوار - سبيكة", "e.g. Ring - Bracelet - Bar"),
            modifier = Modifier.fillMaxWidth().height(42.dp)
        )

        Spacer(Modifier.height(12.dp))

        Text(t("شكل القطعة", "Item Shape"), color = Gray, fontSize = 10.sp)
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            pieceEmojiOptions.forEach { (emoji, label) ->
                val selected = emoji == selectedEmoji
                Column(
                    modifier = Modifier
                        .width(52.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .border(
                            width = if (selected) 1.5.dp else 1.dp,
                            color = if (selected) Gold else Border,
                            shape = RoundedCornerShape(9.dp)
                        )
                        .background(if (selected) GoldDark.copy(alpha = 0.2f) else Color.Transparent)
                        .clickable { selectedEmoji = emoji }
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(emoji, fontSize = 18.sp)
                    Spacer(Modifier.height(2.dp))
                    Text(pieceShapeLabel(label), color = if (selected) Gold else Gray, fontSize = 8.sp, maxLines = 1)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Text(t("العيار", "Karat"), color = Gray, fontSize = 10.sp)
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("24K", "22K", "21K", "18K").forEach { k ->
                ChoiceButton(
                    text = karatLabel(k),
                    selected = k == karat,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                ) { karat = k }
            }
        }

        Spacer(Modifier.height(12.dp))

        Text(t("الوزن (جرام)", "Weight (grams)"), color = Gray, fontSize = 10.sp)
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .clip(RoundedCornerShape(9.dp))
                .border(1.dp, Border, RoundedCornerShape(9.dp)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SmallActionButton("−") { weight = (weight - 1).coerceAtLeast(0.1) }
            NumericInputField(
                value = weight,
                onValueChanged = { weight = it },
                fontSize = 16.sp,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            SmallActionButton("+") { weight += 1 }
        }

        Spacer(Modifier.height(12.dp))

        // بلد الصنع مرتبط بالمشغولات فقط (22/21/18)؛ ذهب 24 عيار الاستثماري
        // معفى من الضريبة أصلاً فلا داعي له
        if (!isTaxExempt) {
            Text(t("بلد الصنع", "Country of origin"), color = Gray, fontSize = 10.sp)
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .border(1.dp, Border, RoundedCornerShape(9.dp))
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(t("السعودية", "Saudi Arabia"), color = White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Icon(
                    imageVector = Icons.Outlined.Language,
                    contentDescription = null,
                    tint = Gray,
                    modifier = Modifier.size(15.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
        }

        Text(t("سعر الشراء (ريال)", "Purchase Price (SAR)"), color = Gray, fontSize = 10.sp)
        Spacer(Modifier.height(6.dp))
        NumericInputField(
            value = purchasePrice,
            onValueChanged = { purchasePrice = it },
            fontSize = 18.sp,
            minValue = 0.0,
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .clip(RoundedCornerShape(9.dp))
                .border(1.dp, Border, RoundedCornerShape(9.dp))
        )

        if (!isTaxExempt) {
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Switch(
                    checked = includingTax,
                    onCheckedChange = { includingTax = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Black,
                        checkedTrackColor = Gold,
                        uncheckedThumbColor = Gray,
                        uncheckedTrackColor = CardBlack
                    )
                )
                Text(t("السعر شامل الضريبة؟", "Price includes tax?"), color = White, fontSize = 12.sp)
            }
        } else {
            Spacer(Modifier.height(6.dp))
            Text(t("ذهب استثماري 24 عيار — معفى من الضريبة", "Investment 24K gold — tax exempt"), color = Gray, fontSize = 9.sp)
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(t("المصنعية (للجرام) ريال", "Workmanship (per gram) SAR"), color = White, fontSize = 10.sp)
            NumericInputField(
                value = manufacturing,
                onValueChanged = { manufacturing = it.coerceAtMost(500.0) },
                fontSize = 12.sp,
                minValue = 0.0,
                modifier = Modifier
                    .width(70.dp)
                    .height(30.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .border(1.dp, Border, RoundedCornerShape(6.dp))
            )
        }

        Spacer(Modifier.height(12.dp))

        Text(t("تاريخ الشراء", "Purchase Date"), color = Gray, fontSize = 10.sp)
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .clip(RoundedCornerShape(9.dp))
                .border(1.dp, Border, RoundedCornerShape(9.dp))
                .clickable { showDatePicker = true }
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.CalendarMonth,
                contentDescription = null,
                tint = Gold,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(purchaseDate, color = White, fontSize = 12.sp, modifier = Modifier.weight(1f))
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = isSold,
                onCheckedChange = { isSold = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Black,
                    checkedTrackColor = Red,
                    uncheckedThumbColor = Gray,
                    uncheckedTrackColor = CardBlack
                )
            )
            Column(horizontalAlignment = Alignment.End) {
                Text(t("قطعة مباعة", "Item sold"), color = White, fontSize = 12.sp)
                Text(t("لا تُحسب ضمن إجمالي الزكاة", "Not counted in Zakat total"), color = Gray, fontSize = 9.sp)
            }
        }

        Spacer(Modifier.height(12.dp))

        Text(t("ملاحظات (اختياري)", "Notes (optional)"), color = Gray, fontSize = 10.sp)
        Spacer(Modifier.height(6.dp))
        SelectableTextField(
            value = notes,
            onValueChange = { notes = it },
            placeholder = t("اكتب ملاحظة...", "Write a note..."),
            modifier = Modifier.fillMaxWidth().height(42.dp)
        )

        Spacer(Modifier.height(16.dp))

        // ---- بطاقة معاينة القيمة الحالية (محسوبة من السعر العالمي الحي) ----
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, Border, RoundedCornerShape(12.dp))
                .background(CardBlack)
                .padding(14.dp)
        ) {
            Text(t("معاينة القيمة الحالية", "Current Value Preview"), color = White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))

            Column {
                Text(t("سعر جرام الذهب (${karatLabel(karat)})", "Gold price per gram (${karatLabel(karat)})"), color = Gray, fontSize = 9.sp)
                Text(
                    "${fmt(karatPrice, 2)} ${t("ريال", "SAR")}",
                    color = Gold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(10.dp))
            CalculatorRow(t("قيمة الذهب (بدون مصنعية)", "Gold value (excl. workmanship)"), "${fmt(currentBeforeVat, 2, grouped = true)} ${t("ريال", "SAR")}")
            CalculatorRow(t("قيمة المصنعية", "Workmanship value"), "${fmt(currentManufacturing, 2, grouped = true)} ${t("ريال", "SAR")}")
            if (isTaxExempt) {
                CalculatorRow(t("ضريبة القيمة المضافة", "VAT"), t("معفى", "Exempt"))
            } else {
                CalculatorRow(t("ضريبة القيمة المضافة (15%)", "VAT (15%)"), "${fmt(currentVat, 2, grouped = true)} ${t("ريال", "SAR")}")
            }

            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Border)
            )
            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(t("القيمة الحالية للقطعة", "Item's Current Value"), color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(
                    "${fmt(currentTotal, 2, grouped = true)} ${t("ريال", "SAR")}",
                    color = Gold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(t("الربح / الخسارة الحالية", "Current Profit / Loss"), color = Gray, fontSize = 10.sp)
                Text(
                    "${if (profit >= 0) "+" else ""}${fmt(profit, 2, grouped = true)} ${t("ريال", "SAR")} (${fmt(profitPercent, 2)}%)",
                    color = if (profit >= 0) Green else Red,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, Border, RoundedCornerShape(10.dp))
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Text(t("إلغاء", "Cancel"), color = Gray, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Box(
                modifier = Modifier
                    .weight(2f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Gold)
                    .clickable {
                        val finalName = name.trim().ifEmpty { t("قطعة ذهب", "Gold item") }
                        onSave(
                            GoldItem(
                                name = finalName,
                                emoji = selectedEmoji,
                                karat = karat,
                                weightGrams = weight,
                                purchasePriceWithTax = purchasePriceWithTax,
                                manufacturingPerGram = manufacturing,
                                purchaseDate = purchaseDate,
                                notes = notes,
                                isSold = isSold
                            )
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (isEditing) t("حفظ التعديلات", "Save Changes") else t("حفظ في المحفظة", "Save to Portfolio"),
                    color = Black,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(16.dp))
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        purchaseDate = dateTextFromEpochMillis(millis)
                    }
                    showDatePicker = false
                }) {
                    Text(t("موافق", "OK"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(t("إلغاء", "Cancel"))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showDeleteConfirm && onDelete != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .clickable(
                    indication = null,
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                ) { showDeleteConfirm = false },
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 28.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, Border, RoundedCornerShape(14.dp))
                    .background(CardBlack)
                    .clickable(
                        indication = null,
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    ) { }
                    .padding(18.dp)
            ) {
                Text(t("حذف القطعة؟", "Delete Item?"), color = White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text(t("لا يمكن التراجع عن هذا الإجراء.", "This action cannot be undone."), color = Gray, fontSize = 11.sp)
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .border(1.dp, Border, RoundedCornerShape(9.dp))
                            .clickable { showDeleteConfirm = false },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(t("إلغاء", "Cancel"), color = Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(Red)
                            .clickable { onDelete() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(t("حذف نهائياً", "Delete Permanently"), color = White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==================== شاشة الرسم البياني الكاملة ====================
private val chartPeriods = listOf("24 ساعة", "أسبوع", "شهر", "3 شهور", "6 شهور", "سنة", "سنتان", "5 سنين")

// النصوص أعلاه تُستخدم كمفاتيح مقارنة في كل مكان (periodDaysFor، اختيار
// الفترة الحالية...) فتبقى كما هي؛ هذه الدالة فقط تُترجم النص المعروض
// على الزر دون المساس بالمفتاح نفسه
private fun periodLabel(period: String): String = when (period) {
    "24 ساعة" -> t("24 ساعة", "24H")
    "أسبوع" -> t("أسبوع", "1W")
    "شهر" -> t("شهر", "1M")
    "3 شهور" -> t("3 شهور", "3M")
    "6 شهور" -> t("6 شهور", "6M")
    "سنة" -> t("سنة", "1Y")
    "سنتان" -> t("سنتان", "2Y")
    "5 سنين" -> t("5 سنين", "5Y")
    else -> period
}

@Composable
private fun PriceChartFullScreen(
    selectedPeriod: String,
    onPeriodSelected: (String) -> Unit,
    selectedKarat: String,
    onBack: () -> Unit
) {
    var showAnalysis by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = t("رجوع", "Back"),
                tint = Gold,
                modifier = Modifier
                    .size(22.dp)
                    .clickable { onBack() }
            )
            Text(
                if (showAnalysis) t("التحليل الفني", "Technical Analysis") else t("تتبع الأسعار", "Price Tracking"),
                color = Gold,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(10.dp))

        // مفتاح التبديل بين تتبع الأسعار (الوضع الحالي) والتحليل الفني بالذكاء الاصطناعي
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(9.dp))
                .border(1.dp, Border, RoundedCornerShape(9.dp))
                .padding(2.dp)
        ) {
            listOf(false to t("تتبع الأسعار", "Price Tracking"), true to t("التحليل الفني", "Technical Analysis")).forEach { (analysisMode, label) ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(7.dp))
                        .background(if (showAnalysis == analysisMode) Gold else Color.Transparent)
                        .clickable { showAnalysis = analysisMode }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        label,
                        color = if (showAnalysis == analysisMode) Black else Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // قائمة الفترات الزمنية (مشتركة بين الوضعين)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            chartPeriods.forEach { period ->
                Box(
                    modifier = Modifier
                        .height(34.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (selectedPeriod == period) Gold else Color.Transparent)
                        .border(1.dp, Border, RoundedCornerShape(6.dp))
                        .clickable { onPeriodSelected(period) }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        periodLabel(period),
                        color = if (selectedPeriod == period) Black else White,
                        fontSize = 12.sp,
                        fontWeight = if (selectedPeriod == period) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        if (showAnalysis) {
            TechnicalAnalysisContent(period = selectedPeriod, karat = selectedKarat)
        } else {
            GoldMarket.prices.forEach { item ->
                KaratChartCard(
                    karat = item.karat,
                    price = item.price,
                    percent = item.percent,
                    seed = karatChartSeeds[item.karat] ?: 1,
                    period = selectedPeriod
                )
                Spacer(Modifier.height(10.dp))
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

// ==================== شاشة التحليل الفني بالذكاء الاصطناعي ====================
// التوصية هنا مبنية على بيانات فعلية من التطبيق نفسه (فجوة الافتتاح/الإغلاق
// المشتقة من نفس سلسلة الرسم البياني، أهم خبر من قائمة الأخبار، وأقرب موعد
// فيدرالي فعلي من التقويم) بدل مؤشرات فنية معقدة (متوسطات متحركة/RSI)
@Composable
private fun TechnicalAnalysisContent(period: String, karat: String) {
    val karatPrice = GoldMarket.prices.first { it.karat == karat }
    val stats = karatPeriodStats(karatPrice.price, karat, period)
    val stats24 = karatPeriodStats(GoldMarket.prices.first { it.karat == "24K" }.price, "24K", period)
    val stats21 = karatPeriodStats(GoldMarket.prices.first { it.karat == "21K" }.price, "21K", period)

    val trendUp = stats.closePrice >= stats.openPrice
    val changePercent = if (stats.openPrice != 0.0)
        (stats.closePrice - stats.openPrice) / stats.openPrice * 100.0
    else 0.0

    val topNews = fullNewsList.firstOrNull()
    val fedDate = nextFedMeetingDate()
    val daysUntilFed = fedDate?.let { todayLocalDate().daysUntil(it) }

    Column(modifier = Modifier.fillMaxWidth()) {
        // ---- بطاقة السعر والرسم البياني ----
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, Border, RoundedCornerShape(12.dp))
                .background(CardBlack)
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    t("سعر الذهب (عيار ${karat.removeSuffix("K")})", "Gold Price (${karat.removeSuffix("K")}K)"),
                    color = White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        fmt(karatPrice.price, 2) + " " + t("ريال", "SAR"),
                        color = White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${if (karatPrice.change >= 0) "+" else ""}${fmt(karatPrice.change, 2)} " +
                                "(${fmt(karatPrice.percent, 2)}%) ${if (karatPrice.change >= 0) "▲" else "▼"}",
                        color = if (karatPrice.change >= 0) Green else Red,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
            ) {
                KaratChartCanvas(
                    modifier = Modifier.fillMaxSize(),
                    basePrice = karatPrice.price,
                    seed = karatChartSeeds[karat] ?: 1,
                    period = period,
                    realPoints = realChartPointsFor(karat, period),
                    realBars = realBarsFor(period),
                    karat = karat
                )
            }

            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(9.dp))
                    .border(1.dp, Border, RoundedCornerShape(9.dp))
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = null,
                        tint = Gray,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(t("الفترة", "Period"), color = Gray, fontSize = 9.sp)
                }
                Text(periodRangeText(period), color = White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (stats.isReal) t("⚡ بيانات تاريخية حقيقية", "⚡ Real historical data") else t("≈ تقدير مبني على زخم آخر 30 يوماً الحقيقية", "≈ Estimate based on real last-30-day momentum"),
                    color = if (stats.isReal) Green else Yellow,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OpenCloseCard(title = t("سعر الذهب عيار 24", "24K Gold Price"), stats = stats24, modifier = Modifier.weight(1f))
                OpenCloseCard(title = t("سعر الذهب عيار 21", "21K Gold Price"), stats = stats21, modifier = Modifier.weight(1f))
            }
        }

        Spacer(Modifier.height(10.dp))

        // ---- بطاقة التحليل الفني بالذكاء الاصطناعي ----
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, Border, RoundedCornerShape(12.dp))
                .background(CardBlack)
                .padding(14.dp)
        ) {
            Text(
                t("تحليل فني بالذكاء الاصطناعي", "AI Technical Analysis"),
                color = White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )

            Spacer(Modifier.height(10.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(9.dp))
                    .background((if (trendUp) Green else Red).copy(alpha = 0.12f))
                    .border(1.dp, GoldDark, RoundedCornerShape(9.dp))
                    .padding(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        if (trendUp) "↗" else "↘",
                        color = if (trendUp) Green else Red,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        t("الاتجاه العام: ${if (trendUp) "صاعد" else "هابط"}", "Overall trend: ${if (trendUp) "Up" else "Down"}"),
                        color = Gold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    t(
                        "استمرار الزخم ${if (trendUp) "الإيجابي" else "السلبي"} طالما بقي سعر الإغلاق " +
                                "${if (trendUp) "أعلى" else "أدنى"} من سعر الافتتاح لنفس الفترة.",
                        "${if (trendUp) "Positive" else "Negative"} momentum continues as long as the closing " +
                                "price stays ${if (trendUp) "above" else "below"} the opening price for the same period."
                    ),
                    color = Gray,
                    fontSize = 10.sp,
                    lineHeight = 15.sp,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(12.dp))
            Text(
                t("أهم النقاط (فترة: $period)", "Key Points (period: ${periodLabel(period)})"),
                color = White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
            Spacer(Modifier.height(8.dp))

            AnalysisPoint(
                t(
                    "افتتح عند ${fmt(stats.openPrice, 2)} وأغلق عند ${fmt(stats.closePrice, 2)} ريال — " +
                            "إغلاق ${if (trendUp) "أعلى" else "أدنى"} من الافتتاح بـ " +
                            "${fmt(kotlin.math.abs(changePercent), 2)}%.",
                    "Opened at ${fmt(stats.openPrice, 2)} and closed at ${fmt(stats.closePrice, 2)} SAR — " +
                            "closing ${if (trendUp) "above" else "below"} the open by " +
                            "${fmt(kotlin.math.abs(changePercent), 2)}%."
                )
            )
            if (topNews != null) {
                AnalysisPoint(t("أهم خبر مؤثر الآن: ${topNews.text} (${topNews.time}).", "Top influencing news now: ${topNews.text} (${topNews.time})."))
            }
            AnalysisPoint(
                if (daysUntilFed != null)
                    t(
                        "اجتماع الفيدرالي القادم بعد $daysUntilFed يوماً (${fedDate!!.toPeriodDisplayText()}) — " +
                                "قد يزيد التذبذب قرب الإعلان.",
                        "Next Fed meeting in $daysUntilFed days (${fedDate!!.toPeriodDisplayText()}) — " +
                                "volatility may increase near the announcement."
                    )
                else
                    t("لا يوجد اجتماع فيدرالي مجدول قريباً ضمن التقويم الحالي.", "No Fed meeting scheduled soon in the current calendar.")
            )
            AnalysisPoint(
                t(
                    "أقرب دعم عند ${fmt(stats.periodLow, 2)} ريال، وأقرب مقاومة عند " +
                            "${fmt(stats.periodHigh, 2)} ريال (أدنى وأعلى سعر خلال الفترة).",
                    "Nearest support at ${fmt(stats.periodLow, 2)} SAR, nearest resistance at " +
                            "${fmt(stats.periodHigh, 2)} SAR (lowest and highest price during the period)."
                )
            )

            Spacer(Modifier.height(6.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(9.dp))
                    .background(Gold.copy(alpha = 0.08f))
                    .border(1.dp, GoldDark, RoundedCornerShape(9.dp))
                    .padding(10.dp)
            ) {
                Text(
                    t("توقعات الذكاء الاصطناعي", "AI Forecast"),
                    color = Gold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    buildString {
                        if (trendUp) {
                            append(
                                t(
                                    "في حال استمر الإغلاق فوق الافتتاح ونبرة الأخبار إيجابية، يُتوقع اختبار " +
                                            "مستوى ${fmt(stats.periodHigh, 2)} ريال خلال الفترة القادمة. ",
                                    "If the close stays above the open and the news tone remains positive, a test of " +
                                            "the ${fmt(stats.periodHigh, 2)} SAR level is expected in the coming period. "
                                )
                            )
                        } else {
                            append(
                                t(
                                    "في حال استمر الإغلاق دون الافتتاح، فقد يتجه السعر لاختبار مستوى " +
                                            "${fmt(stats.periodLow, 2)} ريال خلال الفترة القادمة. ",
                                    "If the close stays below the open, the price may head toward testing the " +
                                            "${fmt(stats.periodLow, 2)} SAR level in the coming period. "
                                )
                            )
                        }
                        if (daysUntilFed != null && daysUntilFed <= 14) {
                            append(t("مع اقتراب اجتماع الفيدرالي بعد $daysUntilFed يوماً، يُتوقع ارتفاع التذبذب حول الإعلان.", "With the Fed meeting approaching in $daysUntilFed days, volatility is expected to rise around the announcement."))
                        } else {
                            append(t("لا يوجد حدث فيدرالي وشيك يُتوقع أن يزيد التذبذب حالياً.", "No upcoming Fed event is currently expected to increase volatility."))
                        }
                    },
                    color = Gray,
                    fontSize = 10.sp,
                    lineHeight = 16.sp,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(10.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = Gold,
                    modifier = Modifier
                        .size(13.dp)
                        .padding(top = 1.dp)
                )
                Text(
                    t("ملاحظة: هذا التحليل يعتمد على بيانات الأسعار والأخبار ومواعيد الفيدرالي، وليس توصية استثمارية ملزمة.", "Note: this analysis is based on price, news, and Fed meeting data, and is not binding investment advice."),
                    color = Gray,
                    fontSize = 9.sp,
                    lineHeight = 14.sp,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun OpenCloseCard(title: String, stats: KaratPeriodStats, modifier: Modifier = Modifier) {
    val changePercent = if (stats.openPrice != 0.0)
        (stats.closePrice - stats.openPrice) / stats.openPrice * 100.0
    else 0.0
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(9.dp))
            .border(1.dp, Border, RoundedCornerShape(9.dp))
            .padding(9.dp)
    ) {
        Text(
            title,
            color = Gold,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(fmt(stats.openPrice, 2) + " " + t("ريال", "SAR"), color = White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            Text(t("افتتاح", "Open"), color = Gray, fontSize = 8.5.sp)
        }
        Spacer(Modifier.height(3.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(fmt(stats.closePrice, 2) + " " + t("ريال", "SAR"), color = White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            Text(t("إغلاق", "Close"), color = Gray, fontSize = 8.5.sp)
        }
        Spacer(Modifier.height(7.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background((if (changePercent >= 0) Green else Red).copy(alpha = 0.15f))
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "${if (changePercent >= 0) "+" else ""}${fmt(changePercent, 2)}%",
                color = if (changePercent >= 0) Green else Red,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun AnalysisPoint(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(15.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Green.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Text("✓", color = Green, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
        Text(
            text,
            color = Gray,
            fontSize = 10.sp,
            lineHeight = 15.sp,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

// بطاقة رسم بياني لعيار ذهب واحد، بسعره الحالي بالريال السعودي
@Composable
private fun KaratChartCard(
    karat: String,
    price: Double,
    percent: Double,
    seed: Int,
    period: String
) {
    val karatNumber = karat.removeSuffix("K")
    val title = t("عيار $karatNumber - ريال سعودي", "${karatNumber}K - Saudi Riyal")
    AppCard(title = title, modifier = Modifier.fillMaxWidth().height(215.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                fmt(price, 2) + " " + t("ريال", "SAR"),
                color = White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "▲ ${fmt(percent, 2)}%",
                color = Green,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(6.dp))
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            KaratChartCanvas(
                modifier = Modifier.fillMaxSize(),
                basePrice = price,
                seed = seed,
                period = period,
                realPoints = realChartPointsFor(karat, period)
            )
        }
    }
}

// بذرة عشوائية ثابتة لكل عيار حتى يبقى شكل الرسم نفسه بين إعادة الرسم
// (تُستخدم في شاشتي "تتبع الأسعار" و"التحليل الفني" معاً)
private val karatChartSeeds = mapOf("24K" to 1, "22K" to 2, "21K" to 3, "18K" to 4)

private fun pointCountFor(period: String): Int = when (period) {
    "24 ساعة" -> 24
    "أسبوع" -> 28
    "شهر" -> 30
    "3 شهور" -> 45
    "6 شهور" -> 60
    "سنة" -> 60
    "سنتان" -> 60
    else -> 60
}

// سلسلة نسب (0..1) مبنية على بذرة عشوائية ثابتة لكل عيار/فترة — تمثيل
// توضيحي لحركة السعر (لا يوجد مزوّد بيانات تاريخية فعلي حتى الآن)، تُستخدم
// لرسم الشارت ولاشتقاق الافتتاح/الإغلاق والدعم/المقاومة في شاشة التحليل
// الفني بشكل متّسق مع بعضها
private fun generateSeriesRatios(seed: Int, period: String): List<Float> {
    val pointCount = pointCountFor(period)
    return (0 until pointCount).map { i ->
        val t = i / (pointCount - 1).toFloat()
        val trend = 0.14f + t * 0.72f
        val ripple = (kotlin.math.sin(t * (10f + seed) + seed) * 0.03f) +
                (kotlin.math.sin(t * (4f + seed) + seed * 2) * 0.04f)
        (trend + ripple).coerceIn(0.05f, 0.98f)
    }
}

// تحويل سعر الأونصة بالدولار (كما يرجعه /v1/bars) لسعر الجرام بالريال
// السعودي لعيار معيّن — نفس معادلة GoldMarket.kt (نسبة النقاء × سعر
// الصرف الثابت 3.75)، لكنها هنا محلية لأن الشموع التاريخية بالأونصة
// بينما GoldMarket.kt يستقبل السعر بالجرام جاهزاً من مزوّد آخر
private const val TROY_OUNCE_GRAMS = 31.1034768
private const val HISTORY_USD_TO_SAR = 3.75
private val karatPurity = mapOf("24K" to 1.0, "22K" to 22.0 / 24.0, "21K" to 21.0 / 24.0, "18K" to 18.0 / 24.0)

internal fun usdPerOunceToSarPerGram(usdPerOunce: Double, karat: String): Double {
    val purity = karatPurity[karat] ?: 1.0
    return (usdPerOunce / TROY_OUNCE_GRAMS) * purity * HISTORY_USD_TO_SAR
}

// الشموع الحقيقية (GoldHistory) التي تقع ضمن الفترة المطلوبة، أو null إن
// لم تتوفر (الفترة أطول من سقف الخطة المجانية 30 يوماً، أو لم يجلب
// التطبيق بيانات بعد)
private fun realBarsFor(period: String): List<HistoryBar>? {
    val bars = GoldHistory.dailyBarsUsdPerOunce
    if (bars.isEmpty()) return null
    val cutoff = todayLocalDate().minus((periodDaysFor(period) - 1).coerceAtLeast(0), DateTimeUnit.DAY)
    val relevant = bars.filter { it.date >= cutoff }
    return relevant.ifEmpty { null }
}

// نقاط رسم حقيقية (تسمية تاريخ قصيرة، سعر الجرام بالريال) لعيار وفترة
// معيّنة — تُستخدم مباشرة في KaratChartCanvas بدل السلسلة التوضيحية
// عندما تتوفر بيانات حقيقية لنفس الفترة
private fun realChartPointsFor(karat: String, period: String): List<Pair<String, Double>>? {
    val bars = realBarsFor(period) ?: return null
    return bars.map { bar ->
        val label = "${bar.date.dayOfMonth.toString().padStart(2, '0')}/" +
                bar.date.monthNumber.toString().padStart(2, '0')
        label to usdPerOunceToSarPerGram(bar.close, karat)
    }
}

private data class KaratPeriodStats(
    val openPrice: Double,
    val closePrice: Double,
    val periodLow: Double,
    val periodHigh: Double,
    // true = مبنية على بيانات تاريخية حقيقية من مزوّد الأسعار، false = تقدير
    // ذكي مبني على زخم آخر 30 يوماً الحقيقية (للفترات الأطول من شهر، التي
    // تحتاج اشتراكاً مدفوعاً عند نفس المزوّد للحصول على بيانات حقيقية لها)
    val isReal: Boolean
)

// شمعة/شموع الافتتاح والإغلاق فقط لفترة "24 ساعة": نستخدم أمس (آخر يوم
// تداول مكتمل فعلياً) بدل اليوم نفسه، لأن شمعة اليوم غالباً غير مكتملة
// بعد عند مزوّد البيانات فتكون فارغة أو غير دقيقة — هذا يخص حساب
// الافتتاح/الإغلاق فقط، ولا يغيّر الرسم البياني أو تسميات الفترة نفسها
private fun openCloseBarsFor(period: String): List<HistoryBar>? {
    if (period != "24 ساعة") return realBarsFor(period)
    val bars = GoldHistory.dailyBarsUsdPerOunce
    if (bars.isEmpty()) return null
    val today = todayLocalDate()
    val lastCompleteBar = bars.filter { it.date < today }.maxByOrNull { it.date }
        ?: bars.maxByOrNull { it.date }
    return lastCompleteBar?.let { listOf(it) }
}

private fun karatPeriodStats(basePrice: Double, karat: String, period: String): KaratPeriodStats {
    val realBars = openCloseBarsFor(period)
    if (realBars != null) {
        val openSar = usdPerOunceToSarPerGram(realBars.first().open, karat)
        val closeSar = usdPerOunceToSarPerGram(realBars.last().close, karat)
        val lowSar = usdPerOunceToSarPerGram(realBars.minOf { it.low }, karat)
        val highSar = usdPerOunceToSarPerGram(realBars.maxOf { it.high }, karat)
        return KaratPeriodStats(openSar, closeSar, lowSar, highSar, isReal = true)
    }
    return estimatedPeriodStats(basePrice, karat, period)
}

// تقدير للفترات الأطول من 30 يوماً (لا بيانات حقيقية مجانية لها): بدل
// رسم عشوائي غير مرتبط بالواقع، نمدّد زخم آخر 30 يوماً الحقيقية (نسبة
// التغيّر الفعلية) مع إخماده كلما طالت الفترة، حتى يبقى التقدير مرتبطاً
// بحركة السوق الأخيرة الحقيقية بدل رقم عشوائي بحت
private fun estimatedPeriodStats(basePrice: Double, karat: String, period: String): KaratPeriodStats {
    val realBars = GoldHistory.dailyBarsUsdPerOunce
    val recentTrendPercent = if (realBars.size >= 2) {
        val firstOpen = realBars.first().open
        val lastClose = realBars.last().close
        if (firstOpen != 0.0) (lastClose - firstOpen) / firstOpen else 0.0
    } else 0.0

    val days = periodDaysFor(period)
    val damped = recentTrendPercent * kotlin.math.sqrt(days / 30.0).coerceAtMost(3.0)
    val openEstimate = basePrice / (1.0 + damped)
    val periodLow = minOf(openEstimate, basePrice) * 0.985
    val periodHigh = maxOf(openEstimate, basePrice) * 1.015
    return KaratPeriodStats(
        openPrice = openEstimate,
        closePrice = basePrice,
        periodLow = periodLow,
        periodHigh = periodHigh,
        isReal = false
    )
}

private fun LocalDate.toPeriodDisplayText(): String =
    "$year/${monthNumber.toString().padStart(2, '0')}/${dayOfMonth.toString().padStart(2, '0')}"

private fun periodDaysFor(period: String): Int = when (period) {
    "24 ساعة" -> 1
    "أسبوع" -> 7
    "شهر" -> 30
    "3 شهور" -> 90
    "6 شهور" -> 180
    "سنة" -> 365
    "سنتان" -> 730
    else -> 1825
}

private fun periodRangeText(period: String): String {
    val end = todayLocalDate()
    val start = end.minus(periodDaysFor(period), DateTimeUnit.DAY)
    return "${start.toPeriodDisplayText()} - ${end.toPeriodDisplayText()}"
}

// أقرب اجتماع قادم للفيدرالي كتاريخ فعلي (وليس نصاً منسّقاً فقط)، يُستخدم
// لحساب عدد الأيام المتبقية في شاشة التحليل الفني، وفي إشعار تذكير
// اجتماع الفيدرالي (PriceNotifications.kt)
internal fun nextFedMeetingDate(): LocalDate? {
    val today = todayLocalDate()
    return fedMeetingsRaw
        .map { LocalDate(it.year, it.month, it.day) }
        .filter { it >= today }
        .minOrNull()
}

// تسميات محور الوقت أسفل الرسم، حسب الفترة المختارة (زي فيديو المرجع)
private fun xAxisLabelsFor(period: String): List<String> = when (period) {
    "24 ساعة" -> listOf("15:00", "18:00", "21:00", "00:00", "03:00", "06:00", "09:00", "12:00")
    "أسبوع" -> if (AppLanguage.current == AppLang.EN) listOf("Sat", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri")
        else listOf("سبت", "أحد", "اثنين", "ثلاثاء", "أربعاء", "خميس", "جمعة")
    "شهر" -> listOf("1", "5", "10", "15", "20", "25", "30")
    "3 شهور" -> if (AppLanguage.current == AppLang.EN) listOf("Month 1", "Month 2", "Month 3")
        else listOf("الشهر 1", "الشهر 2", "الشهر 3")
    "6 شهور" -> listOf("1", "2", "3", "4", "5", "6")
    "سنة" -> if (AppLanguage.current == AppLang.EN) listOf("Jan", "Mar", "May", "Jul", "Sep", "Nov")
        else listOf("يناير", "مارس", "مايو", "يوليو", "سبتمبر", "نوفمبر")
    "سنتان" -> listOf("2025", "2026")
    else -> listOf("2022", "2023", "2024", "2025", "2026")
}

// يرسم تسمية محور (سعرية أو زمنية) عبر TextMeasurer بدل android.graphics.Paint
// حتى يبقى الكود قابلاً للتصريف على iOS مستقبلاً بلا أي تبعية لمنصّة أندرويد
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawAxisLabel(
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    text: String,
    x: Float,
    y: Float,
    centered: Boolean,
    fontSize: androidx.compose.ui.unit.TextUnit = 9.sp
) {
    val layout = textMeasurer.measure(
        text = text,
        style = TextStyle(color = Gray, fontSize = fontSize)
    )
    val topLeftX = if (centered) x - layout.size.width / 2f else x
    val topLeftY = y - layout.size.height / 2f
    drawText(layout, topLeft = Offset(topLeftX, topLeftY))
}

// بطاقة السعر الصغيرة التي تتحرك مع الإصبع أثناء اللمس/السحب على الرسم
private data class ChartTooltip(
    val pointX: Float,
    val pointY: Float,
    val boxLeftX: Float,
    val label: String,
    val price: Double
)

private val ChartTooltipWidth = 96.dp

@Composable
private fun ChartTooltipCard(info: ChartTooltip) {
    Column(
        modifier = Modifier
            .width(ChartTooltipWidth)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, Border, RoundedCornerShape(8.dp))
            .background(CardBlack)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.CalendarMonth,
                contentDescription = null,
                tint = Gray,
                modifier = Modifier.size(11.dp)
            )
            Text(info.label, color = White, fontSize = 10.sp, maxLines = 1)
        }
        Spacer(Modifier.height(3.dp))
        Text(
            "${fmt(info.price, 2)} ${t("ريال", "SAR")}",
            color = Gold,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

private data class CandleTooltip(
    val x: Float,
    val label: String,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double
)

@Composable
private fun CandleTooltipCard(info: CandleTooltip) {
    val isUp = info.close >= info.open
    Column(
        modifier = Modifier
            .width(128.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, Border, RoundedCornerShape(8.dp))
            .background(CardBlack)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.CalendarMonth,
                contentDescription = null,
                tint = Gray,
                modifier = Modifier.size(11.dp)
            )
            Text(info.label, color = White, fontSize = 10.sp, maxLines = 1)
            Spacer(Modifier.weight(1f))
            Text(
                if (isUp) "▲" else "▼",
                color = if (isUp) Green else Red,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(t("افتتاح ${fmt(info.open, 2)}", "Open ${fmt(info.open, 2)}"), color = Gray, fontSize = 8.5.sp, maxLines = 1)
        }
        Text(t("إغلاق ${fmt(info.close, 2)}", "Close ${fmt(info.close, 2)}"), color = if (isUp) Green else Red, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(t("أعلى ${fmt(info.high, 2)}", "High ${fmt(info.high, 2)}"), color = Gray, fontSize = 8.5.sp, maxLines = 1)
        }
        Text(t("أدنى ${fmt(info.low, 2)}", "Low ${fmt(info.low, 2)}"), color = Gray, fontSize = 8.5.sp, maxLines = 1)
    }
}

// رسم شموع يابانية (Candlestick) من بيانات OHLC حقيقية — يدعم التكبير
// والتصغير بحركة القرص (Pinch) والسحب الأفقي للتنقل بين الفترات، وإصبع
// واحد يعرض بطاقة تفاصيل الشمعة تحت اللمس (نفس فكرة الخط البسيط، لكن
// بمعلومات OHLC كاملة). "إعادة ضبط" تظهر فقط أثناء التكبير الفعلي
@Composable
private fun CandlestickChart(modifier: Modifier, bars: List<HistoryBar>, karat: String) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    var scale by remember(bars) { mutableStateOf(1f) }
    var startIndexFloat by remember(bars) { mutableStateOf(0f) }
    var tooltip by remember(bars) { mutableStateOf<CandleTooltip?>(null) }

    val totalCount = bars.size
    val minVisible = 5.coerceAtMost(totalCount)

    fun dateLabel(bar: HistoryBar): String =
        "${bar.date.dayOfMonth.toString().padStart(2, '0')}/${bar.date.monthNumber.toString().padStart(2, '0')}"

    // clipToBounds تضمن عدم تجاوز أي عنصر داخلي (بطاقة التلميح أو زر
    // إعادة الضبط) للمساحة المخصَّصة للرسم البياني بصرياً مهما كان
    // موضعه أو حجمه
    Box(modifier = modifier.clipToBounds()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 4.dp, bottom = 24.dp)
                .pointerInput(bars) {
                    detectTransformGestures(panZoomLock = false) { centroid, pan, zoom, _ ->
                        val newScale = (scale * zoom).coerceIn(1f, 6f)
                        val newVisibleCount = (totalCount / newScale).roundToInt().coerceIn(minVisible, totalCount)
                        val newMaxStart = (totalCount - newVisibleCount).coerceAtLeast(0)
                        val barsPerPixel = newVisibleCount.toFloat() / size.width.coerceAtLeast(1).toFloat()
                        val newStart = (startIndexFloat - pan.x * barsPerPixel).coerceIn(0f, newMaxStart.toFloat())
                        scale = newScale
                        startIndexFloat = newStart

                        // تحديث التلميح لموضع اللمس الحالي — يعمل بإصبع واحد
                        // (سحب عادي) وأثناء التكبير بإصبعين معاً
                        val startIdx = newStart.roundToInt().coerceIn(0, newMaxStart)
                        val visible = bars.subList(startIdx, (startIdx + newVisibleCount).coerceAtMost(totalCount))
                        if (visible.isNotEmpty()) {
                            val left = 52f
                            val right = size.width - 6f
                            val w = (right - left).coerceAtLeast(1f)
                            val clampedX = centroid.x.coerceIn(left, right)
                            val idx = (((clampedX - left) / w) * (visible.size - 1))
                                .roundToInt()
                                .coerceIn(0, visible.size - 1)
                            val bar = visible[idx]
                            val barX = left + w * idx / (visible.size - 1).coerceAtLeast(1)
                            tooltip = CandleTooltip(
                                x = barX,
                                label = dateLabel(bar),
                                open = usdPerOunceToSarPerGram(bar.open, karat),
                                high = usdPerOunceToSarPerGram(bar.high, karat),
                                low = usdPerOunceToSarPerGram(bar.low, karat),
                                close = usdPerOunceToSarPerGram(bar.close, karat)
                            )
                        }
                    }
                }
        ) {
            val visibleCount = (totalCount / scale).roundToInt().coerceIn(minVisible, totalCount)
            val maxStart = (totalCount - visibleCount).coerceAtLeast(0)
            val startIndex = startIndexFloat.roundToInt().coerceIn(0, maxStart)
            val visibleBars = bars.subList(startIndex, (startIndex + visibleCount).coerceAtMost(totalCount))
            if (visibleBars.isEmpty()) return@Canvas

            val highs = visibleBars.map { usdPerOunceToSarPerGram(it.high, karat) }
            val lows = visibleBars.map { usdPerOunceToSarPerGram(it.low, karat) }
            val minPrice = lows.min()
            val maxPriceRaw = highs.max()
            val maxPrice = if (maxPriceRaw <= minPrice) minPrice * 1.001 else maxPriceRaw

            val left = 52f
            val right = size.width - 6f
            val top = 6f
            val bottom = size.height - 6f
            val w = (right - left).coerceAtLeast(1f)
            val h = (bottom - top).coerceAtLeast(1f)

            val priceSteps = 5
            for (i in 0..priceSteps) {
                val y = top + h * i / priceSteps
                drawLine(
                    color = GoldDark.copy(alpha = 0.4f),
                    start = Offset(left, y),
                    end = Offset(right, y),
                    strokeWidth = 1f
                )
                val labelValue = maxPrice - (maxPrice - minPrice) * i / priceSteps
                drawAxisLabel(textMeasurer, fmt(labelValue, 0), x = 0f, y = y, centered = false)
            }

            fun priceToY(price: Double): Float =
                (bottom - h * ((price - minPrice) / (maxPrice - minPrice))).toFloat()

            val slotWidth = w / visibleBars.size
            val candleWidth = (slotWidth * 0.6f).coerceAtLeast(1.5f)

            visibleBars.forEachIndexed { index, bar ->
                val centerX = left + slotWidth * index + slotWidth / 2f
                val openPrice = usdPerOunceToSarPerGram(bar.open, karat)
                val closePrice = usdPerOunceToSarPerGram(bar.close, karat)
                val highPrice = usdPerOunceToSarPerGram(bar.high, karat)
                val lowPrice = usdPerOunceToSarPerGram(bar.low, karat)
                val isUp = closePrice >= openPrice
                val color = if (isUp) Green else Red

                drawLine(
                    color = color,
                    start = Offset(centerX, priceToY(highPrice)),
                    end = Offset(centerX, priceToY(lowPrice)),
                    strokeWidth = 1.5f
                )

                val bodyTop = priceToY(maxOf(openPrice, closePrice))
                val bodyBottom = priceToY(minOf(openPrice, closePrice))
                drawRect(
                    color = color,
                    topLeft = Offset(centerX - candleWidth / 2f, bodyTop),
                    size = androidx.compose.ui.geometry.Size(candleWidth, (bodyBottom - bodyTop).coerceAtLeast(1.5f))
                )
            }

            val labelCount = 6.coerceAtMost(visibleBars.size)
            if (labelCount > 0) {
                val step = (visibleBars.size - 1).coerceAtLeast(1) / labelCount.coerceAtLeast(1).toFloat()
                for (i in 0 until labelCount) {
                    val idx = (i * step).roundToInt().coerceIn(0, visibleBars.size - 1)
                    val x = left + slotWidth * idx + slotWidth / 2f
                    drawAxisLabel(textMeasurer, dateLabel(visibleBars[idx]), x = x, y = bottom + 18f, centered = true)
                }
            }

            tooltip?.let { info ->
                drawLine(
                    color = White.copy(alpha = 0.4f),
                    start = Offset(info.x, top),
                    end = Offset(info.x, bottom),
                    strokeWidth = 1f
                )
            }
        }

        tooltip?.let { info ->
            val tooltipWidthPx = with(density) { 128.dp.toPx() }
            val boxLeftX = (info.x - tooltipWidthPx / 2f).coerceAtLeast(0f)
            // لما يكون زر "إعادة ضبط" ظاهراً (أثناء التكبير)، ننزل بطاقة
            // التلميح لتحت شوي حتى لا تتراكب معه (الزر ثابت أعلى يمين الرسم)
            val topPadding = if (scale > 1.01f) 30.dp else 4.dp
            Box(
                modifier = Modifier
                    .padding(top = topPadding, bottom = 24.dp)
                    .offset { IntOffset(boxLeftX.roundToInt(), 4) }
            ) {
                CandleTooltipCard(info)
            }
        }

        if (scale > 1.01f) {
            Row(
                modifier = Modifier
                    // TopStart (يمين الشاشة فعلياً بما إن التطبيق RTL) بدل
                    // TopEnd — تسميات المحور السعري الأعلى تُرسم داخل الـ
                    // Canvas عند x=0 (يسار فعلي دائماً بغض النظر عن اتجاه
                    // الواجهة)، فلو حطينا الزر بنفس الجهة يتصادمان بصرياً
                    .align(Alignment.TopStart)
                    .padding(top = 4.dp, start = 4.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(CardBlack)
                    .border(1.dp, Border, RoundedCornerShape(6.dp))
                    .clickable {
                        scale = 1f
                        startIndexFloat = 0f
                    }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(t("إعادة ضبط", "Reset"), color = Gold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// رسم منحنى لعيار معيّن حول سعره الحالي، بمحور سعري مبني على basePrice
// وتذبذب مختلف حسب seed لكل عيار وحسب الفترة المختارة — بنفس أسلوب
// الرسم المرجعي: خط كريمي فاتح، تعبئة متدرجة تحت الخط، شبكة خطوط كاملة،
// محور سعري يسار، ومحور وقت أسفل يتغيّر حسب الفترة المختارة. قابل للمس
// والسحب مباشرة: يظهر خط دليل + مؤشر دائري + بطاقة سعر تتحرك مع الإصبع
@Composable
private fun KaratChartCanvas(
    modifier: Modifier,
    basePrice: Double,
    seed: Int,
    period: String,
    // نقاط حقيقية (تسمية تاريخ، سعر الجرام بالريال) — عند توفرها تُرسم
    // بدل السلسلة التوضيحية، بنفس أسلوب الرسم وتجربة اللمس/السحب تماماً
    realPoints: List<Pair<String, Double>>? = null,
    // شموع OHLC حقيقية — عند توفرها (فترة ≤ 30 يوماً ومزوّد البيانات
    // متوفر)، تُرسم كرسم شموع يابانية تفاعلي (تكبير/تصغير وسحب) بدل الخط
    realBars: List<HistoryBar>? = null,
    karat: String = "24K"
) {
    if (realBars != null && realBars.size >= 2) {
        CandlestickChart(modifier = modifier, bars = realBars, karat = karat)
        return
    }
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val useReal = realPoints != null && realPoints.isNotEmpty()
    val realPrices = if (useReal) realPoints!!.map { it.second } else null

    val minPrice = realPrices?.min() ?: (basePrice * 0.94)
    val maxPriceRaw = realPrices?.max() ?: (basePrice * 1.06)
    val maxPrice = if (maxPriceRaw <= minPrice) minPrice * 1.001 else maxPriceRaw

    val points = remember(basePrice, seed, period, realPoints) {
        if (useReal) {
            realPrices!!.map { price -> ((price - minPrice) / (maxPrice - minPrice)).toFloat().coerceIn(0f, 1f) }
        } else {
            generateSeriesRatios(seed, period)
        }
    }
    val lineColor = Color(0xFFEDE6B0)
    val xLabels = if (useReal) {
        val allLabels = realPoints!!.map { it.first }
        val labelCount = 6.coerceAtMost(allLabels.size)
        if (labelCount <= 1) allLabels
        else (0 until labelCount).map { i -> allLabels[i * (allLabels.size - 1) / (labelCount - 1)] }
    } else {
        xAxisLabelsFor(period)
    }

    var tooltip by remember(basePrice, seed, period, realPoints) { mutableStateOf<ChartTooltip?>(null) }

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 4.dp, bottom = 24.dp)
                .pointerInput(points, xLabels) {
                    val tooltipWidthPx = with(density) { ChartTooltipWidth.toPx() }

                    fun updateTooltip(touchX: Float) {
                        val left = 52f
                        val right = size.width - 6f
                        val top = 6f
                        val bottom = size.height - 6f
                        val w = right - left
                        val h = bottom - top

                        val clampedX = touchX.coerceIn(left, right)
                        val index = (((clampedX - left) / w) * (points.size - 1))
                            .roundToInt()
                            .coerceIn(0, points.size - 1)

                        val pointX = left + w * index / (points.size - 1)
                        val pointY = bottom - h * points[index]
                        val price = minPrice + (maxPrice - minPrice) * points[index]

                        val frac = index / (points.size - 1).toFloat()
                        val labelIndex = (frac * (xLabels.size - 1))
                            .roundToInt()
                            .coerceIn(0, xLabels.size - 1)

                        val boxLeftX = (pointX - tooltipWidthPx / 2f)
                            .coerceIn(0f, (size.width - tooltipWidthPx).coerceAtLeast(0f))

                        tooltip = ChartTooltip(
                            pointX = pointX,
                            pointY = pointY,
                            boxLeftX = boxLeftX,
                            label = xLabels[labelIndex],
                            price = price
                        )
                    }

                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        updateTooltip(down.position.x)
                        drag(down.id) { change ->
                            updateTooltip(change.position.x)
                            change.consume()
                        }
                    }
                }
        ) {
            val left = 52f
            val right = size.width - 6f
            val top = 6f
            val bottom = size.height - 6f
            val w = right - left
            val h = bottom - top

            // شبكة أفقية + تسميات سعرية (5 مستويات)
            val priceSteps = 5
            for (i in 0..priceSteps) {
                val y = top + h * i / priceSteps
                drawLine(
                    color = GoldDark.copy(alpha = 0.4f),
                    start = Offset(left, y),
                    end = Offset(right, y),
                    strokeWidth = 1f
                )
                val labelValue = maxPrice - (maxPrice - minPrice) * i / priceSteps
                drawAxisLabel(textMeasurer, fmt(labelValue, 0), x = 0f, y = y, centered = false)
            }

            // شبكة عمودية بعدد تسميات المحور الأفقي
            val vLines = (xLabels.size - 1).coerceAtLeast(1)
            for (i in 0..vLines) {
                val x = left + w * i / vLines
                drawLine(
                    color = GoldDark.copy(alpha = 0.3f),
                    start = Offset(x, top),
                    end = Offset(x, bottom),
                    strokeWidth = 1f
                )
            }

            val screenPoints = points.mapIndexed { index, value ->
                Offset(
                    x = left + w * index / (points.size - 1),
                    y = bottom - h * value
                )
            }

            val line = Path().apply {
                moveTo(screenPoints.first().x, screenPoints.first().y)
                for (i in 0 until screenPoints.size - 1) {
                    val p0 = screenPoints[i]
                    val p1 = screenPoints[i + 1]
                    val midX = (p0.x + p1.x) / 2f
                    cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
                }
            }

            val fill = Path().apply {
                addPath(line)
                lineTo(screenPoints.last().x, bottom)
                lineTo(screenPoints.first().x, bottom)
                close()
            }
            drawPath(
                path = fill,
                brush = Brush.verticalGradient(
                    colors = listOf(lineColor.copy(alpha = 0.35f), lineColor.copy(alpha = 0f)),
                    startY = top,
                    endY = bottom
                )
            )

            drawPath(path = line, color = lineColor, style = Stroke(width = 2.2f, cap = StrokeCap.Round))

            tooltip?.let { info ->
                drawLine(
                    color = White.copy(alpha = 0.4f),
                    start = Offset(info.pointX, top),
                    end = Offset(info.pointX, bottom),
                    strokeWidth = 1f
                )
                drawCircle(color = White, radius = 4f, center = Offset(info.pointX, info.pointY))
                drawCircle(color = lineColor, radius = 2.2f, center = Offset(info.pointX, info.pointY))
            }

            // تسميات محور الوقت أسفل الرسم
            xLabels.forEachIndexed { index, label ->
                val x = left + w * index / (xLabels.size - 1).coerceAtLeast(1)
                drawAxisLabel(textMeasurer, label, x = x, y = bottom + 18f, centered = true)
            }
        }

        tooltip?.let { info ->
            Box(
                modifier = Modifier
                    .padding(top = 4.dp, bottom = 24.dp)
                    .offset { IntOffset(info.boxLeftX.roundToInt(), 4) }
            ) {
                ChartTooltipCard(info)
            }
        }
    }
}

// ==================== شاشة الأخبار الكاملة ====================
// أخبار حقيقية عن الذهب (GoldNews.kt) بدل قائمة وهمية ثابتة — بلا مؤشر
// إيجابي/سلبي حقيقي (لا تحليل مشاعر فعلي)، فتُعرض كلها بنقطة ذهبية محايدة
private data class NewsItem(val dot: Color, val text: String, val time: String)

private val fullNewsList: List<NewsItem>
    get() = GoldNews.articles.map { NewsItem(Gold, it.title, it.publishedAt) }

@Composable
private fun NewsScreen(onBack: () -> Unit) {
    val newsList = fullNewsList
    val isLoading = GoldNews.isLoading
    val error = GoldNews.lastError
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = t("رجوع", "Back"),
                tint = Gold,
                modifier = Modifier
                    .size(20.dp)
                    .clickable { onBack() }
            )
            Text(
                t("الأخبار", "News"),
                color = Gold,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        if (newsList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    if (isLoading) t("جارٍ تحميل الأخبار...", "Loading news...") else (error ?: t("لا توجد أخبار متوفرة حالياً", "No news available right now")),
                    color = Gray,
                    fontSize = 12.sp
                )
            }
            return
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(newsList) { news ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(9.dp))
                        .border(1.dp, Border, RoundedCornerShape(9.dp))
                        .background(CardBlack)
                        .padding(10.dp)
                ) {
                    NewsRow(dot = news.dot, text = news.text, time = news.time)
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

// ==================== شاشة المحفظة الكاملة ====================
// بيانات تعريفية أولية (نفس شكل GoldItem تماماً) تُحمَّل في savedGoldItems
// عند بدء التطبيق، عشان تبقى قابلة للتعديل والحذف زي أي قطعة يضيفها المستخدم
// — مفيش تمييز بين "بيانات جاهزة" و"بيانات المستخدم" بعد كده
private val defaultGoldItems = listOf(
    GoldItem("سبيكة ذهب", "🟨", "24K", 10.0, 4037.50, 0.0, "01 / 01 / 2025", ""),
    GoldItem("خاتم ذهب", "💍", "21K", 5.28, 1864.32, 0.0, "01 / 01 / 2025", ""),
    GoldItem("سوار ذهب", "⭕", "22K", 8.0, 2960.80, 0.0, "01 / 01 / 2025", ""),
    GoldItem("قلادة ذهب", "📿", "18K", 6.0, 1814.28, 0.0, "01 / 01 / 2025", ""),
    GoldItem("عملة ذهبية", "🪙", "24K", 4.0, 1615.00, 0.0, "01 / 01 / 2025", ""),
    GoldItem("أقراط ذهب", "👂", "21K", 2.0, 706.56, 0.0, "01 / 01 / 2025", "")
)

// ==================== حفظ محلي دائم لقطع المحفظة ====================
// يُخزَّن ملف JSON بسيط على جهاز المستخدم فقط عبر AppStorage (لا سحابة
// ولا خادم)، حتى تبقى قطع المحفظة والزكاة محفوظة بين جلسات التطبيق
// بدل أن تُفقد عند إغلاقه كما كان سابقاً
internal const val goldItemsStorageFile = "gold_items.json"

private fun loadSavedGoldItems(): List<GoldItem> {
    val text = AppStorage.readText(goldItemsStorageFile) ?: return defaultGoldItems
    return try {
        Json.decodeFromString<List<GoldItem>>(text)
    } catch (e: Exception) {
        // ملف تالف أو من نسخة قديمة غير متوافقة — نرجع للبيانات الافتراضية
        // بدل تعطّل التطبيق عند بدء التشغيل
        defaultGoldItems
    }
}

internal fun persistGoldItems(items: List<GoldItem>) {
    AppStorage.writeText(goldItemsStorageFile, Json.encodeToString(items))
}

// يرفع المحفظة الحالية للسحابة فوراً بعد أي تعديل محلي (إضافة/تعديل/حذف
// قطعة)، بصمت وبدون انتظار — فقط إن كان المستخدم مسجّل دخول بالإيميل
private fun uploadPortfolioIfSignedIn(items: List<GoldItem>, scope: kotlinx.coroutines.CoroutineScope) {
    if (AuthService.currentUserEmail == null) return
    scope.launch {
        try {
            val nowMillis = Clock.System.now().toEpochMilliseconds()
            PortfolioSync.upload(Json.encodeToString(items), nowMillis)
            persistPortfolioUpdatedAt(nowMillis)
        } catch (e: Exception) {
            reportSilentError("uploadPortfolioIfSignedIn failed: ${e.message}")
        }
    }
}

// يزامن المحفظة المحلية مع نسخة السحابة عند تسجيل الدخول أو بدء التطبيق
// وهو مسجّل دخول أصلاً: يُنزّل ويستبدل المحلية لو كانت نسخة السحابة أحدث
// تعديلاً، أو يرفع نسخته المحلية لو كانت هي الأحدث (أو لا توجد نسخة سحابية بعد)
private suspend fun syncPortfolioWithCloud(
    localItems: androidx.compose.runtime.snapshots.SnapshotStateList<GoldItem>
) {
    if (AuthService.currentUserEmail == null) return
    try {
        val localUpdatedAt = loadPortfolioUpdatedAt()
        val cloud = PortfolioSync.download()
        if (cloud != null && cloud.second > localUpdatedAt) {
            val cloudItems = Json.decodeFromString<List<GoldItem>>(cloud.first)
            localItems.clear()
            localItems.addAll(cloudItems)
            persistGoldItems(cloudItems)
            persistPortfolioUpdatedAt(cloud.second)
        } else {
            val nowMillis = Clock.System.now().toEpochMilliseconds()
            PortfolioSync.upload(Json.encodeToString(localItems.toList()), nowMillis)
            persistPortfolioUpdatedAt(nowMillis)
        }
    } catch (e: Exception) {
        reportSilentError("syncPortfolioWithCloud failed: ${e.message}")
    }
}

private const val userProfileStorageFile = "user_profile.json"

private fun loadUserProfile(): UserProfile {
    val text = AppStorage.readText(userProfileStorageFile) ?: return UserProfile()
    return try {
        Json.decodeFromString<UserProfile>(text)
    } catch (e: Exception) {
        UserProfile()
    }
}

private fun persistUserProfile(profile: UserProfile) {
    AppStorage.writeText(userProfileStorageFile, Json.encodeToString(profile))
}

// يحسب القيمة الحالية لقطعة بسعر السوق الحي (ذهب + مصنعية + ضريبة، معفى لعيار 24)
private fun GoldItem.currentValue(): Double {
    val pricePerGram = GoldMarket.prices.first { it.karat == karat }.price
    val beforeVat = pricePerGram * weightGrams
    val manufacturingValue = manufacturingPerGram * weightGrams
    val subtotal = beforeVat + manufacturingValue
    val vat = if (karat == "24K") 0.0 else subtotal * 0.15
    return subtotal + vat
}

private data class PortfolioTotals(
    val totalValue: Double,
    val itemCount: Int,
    val totalWeight: Double,
    val totalCost: Double
)

// القطع "المباعة" لم تعد مِلكاً فعلياً، فلا تُحسب ضمن إجمالي المحفظة
private fun portfolioTotals(savedItems: List<GoldItem>): PortfolioTotals {
    val ownedItems = savedItems.filter { !it.isSold }
    return PortfolioTotals(
        totalValue = ownedItems.sumOf { it.currentValue() },
        itemCount = ownedItems.size,
        totalWeight = ownedItems.sumOf { it.weightGrams },
        totalCost = ownedItems.sumOf { it.purchasePriceWithTax }
    )
}

@Composable
private fun PortfolioScreen(
    savedItems: List<GoldItem>,
    onNavigateAddItem: () -> Unit,
    onEditItem: (Int) -> Unit,
    onBack: () -> Unit
) {
    val savedValues = savedItems.map { it to it.currentValue() }
    // القطع "المباعة" تبقى في السجل للمرجعية لكنها لم تعد مِلكاً فعلياً،
    // فلا تُحسب ضمن إجمالي قيمة/وزن/عدد قطع المحفظة
    val ownedValues = savedValues.filter { !it.first.isSold }
    val totalValue = ownedValues.sumOf { it.second }
    val totalWeight = ownedValues.sumOf { it.first.weightGrams }
    val itemCount = ownedValues.size
    val totalCost = ownedValues.sumOf { it.first.purchasePriceWithTax }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = t("رجوع", "Back"),
                    tint = Gold,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onBack() }
                )
                Text(
                    t("المحفظة", "Portfolio"),
                    color = Gold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Icon(
                imageVector = Icons.Outlined.Share,
                contentDescription = t("تصدير PDF", "Export PDF"),
                tint = Gold,
                modifier = Modifier
                    .size(20.dp)
                    .clickable {
                        PdfExport.exportReport(
                            title = t("تقرير المحفظة — Gold Vision", "Portfolio Report — Gold Vision"),
                            generatedAt = t("تاريخ التصدير: ${todayDateText()}", "Export date: ${todayDateText()}"),
                            summary = listOf(
                                PdfReportRow(t("قيمة المحفظة", "Portfolio Value"), "${fmt(totalValue, 2, grouped = true)} ${t("ريال", "SAR")}"),
                                PdfReportRow(t("عدد المنتجات", "Item Count"), t("$itemCount منتجات", "$itemCount items")),
                                PdfReportRow(t("إجمالي الوزن", "Total Weight"), "${fmt(totalWeight, 2)} ${t("جرام", "g")}")
                            ),
                            rows = savedValues.map { (item, value) ->
                                PdfReportRow(
                                    "${item.name} (${karatLabel(item.karat)} • ${fmt(item.weightGrams, 2)} ${t("جم", "g")})${if (item.isSold) t(" — مباعة", " — sold") else ""}",
                                    "${fmt(value, 2, grouped = true)} ${t("ريال", "SAR")}"
                                )
                            }
                        )
                    }
            )
        }

        Box(modifier = Modifier.padding(horizontal = 12.dp)) {
            PortfolioSummary(totalValue = totalValue, itemCount = itemCount, totalWeight = totalWeight, totalCost = totalCost)
        }

        Spacer(Modifier.height(8.dp))

        Box(modifier = Modifier.padding(horizontal = 12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .border(1.dp, Gold, RoundedCornerShape(9.dp))
                    .clickable { onNavigateAddItem() },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(t("إضافة قطعة", "Add Item"), color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("+", color = Gold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(savedValues) { index, (item, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(9.dp))
                        .border(1.dp, if (item.isSold) Red.copy(alpha = 0.4f) else Border, RoundedCornerShape(9.dp))
                        .background(CardBlack)
                        .clickable { onEditItem(index) }
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(RoundedCornerShape(13.dp))
                                .background((if (item.isSold) Red else GoldDark).copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(item.emoji, fontSize = 12.sp)
                        }
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(item.name, color = White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                if (item.isSold) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Red.copy(alpha = 0.15f))
                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                    ) {
                                        Text(t("مباع", "Sold"), color = Red, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Text(
                                "${item.karat} • ${fmt(item.weightGrams, 2)} ${t("جرام", "g")}",
                                color = Gray,
                                fontSize = 10.sp
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "${fmt(value, 2, grouped = true)} ${t("ريال", "SAR")}",
                            color = if (item.isSold) Red else Gold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        // ربح/خسارة القطعة: سعر الذهب اليومي الحالي مقابل سعر الشراء
                        // — لا معنى له للقطع المباعة (لم تعد تتبع السعر اليومي)
                        if (!item.isSold && item.purchasePriceWithTax > 0) {
                            val itemProfit = value - item.purchasePriceWithTax
                            val itemProfitPercent = (itemProfit / item.purchasePriceWithTax) * 100.0
                            Text(
                                "${if (itemProfit >= 0) "+" else ""}${fmt(itemProfit, 2, grouped = true)} ${t("ريال", "SAR")} (${fmt(itemProfitPercent, 2)}%)",
                                color = if (itemProfit >= 0) Green else Red,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

// ==================== شاشة الزكاة الكاملة ====================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ZakatScreen(
    savedItems: List<GoldItem>,
    onNavigateAddItem: () -> Unit,
    onBack: () -> Unit
) {
    var zakatPercent by remember { mutableDoubleStateOf(2.5) }
    var showAllItems by remember { mutableStateOf(true) }
    var currentZakatDate by remember { mutableStateOf("10 / 05 / 2025") }
    var previousZakatDate by remember { mutableStateOf("15 / 05 / 2024") }
    var showZakatInfo by remember { mutableStateOf(false) }
    var settingsExpanded by remember { mutableStateOf(false) }

    // أوزان الذهب المملوكة لكل عيار، تُدخل يدوياً أو تُملأ تلقائياً من
    // القطع المحفوظة في المحفظة عبر زر "استخدام الأوزان الموجودة في المحفظة"،
    // مع تاريخ شراء لكل عيار يُستخدم لحساب مرور الحول
    var weight24 by remember { mutableDoubleStateOf(0.0) }
    var weight22 by remember { mutableDoubleStateOf(0.0) }
    var weight21 by remember { mutableDoubleStateOf(0.0) }
    var weight18 by remember { mutableDoubleStateOf(0.0) }
    var date24 by remember { mutableStateOf(todayDateText()) }
    var date22 by remember { mutableStateOf(todayDateText()) }
    var date21 by remember { mutableStateOf(todayDateText()) }
    var date18 by remember { mutableStateOf(todayDateText()) }
    var activeDateKarat by remember { mutableStateOf<String?>(null) }

    // القطع "المباعة" لم تعد مِلكاً للمستخدم، فلا تُحسب ضمن الزكاة إطلاقاً
    val ownedItems = savedItems.filter { !it.isSold }

    fun earliestPurchaseDate(karat: String): String =
        ownedItems
            .filter { it.karat == karat }
            .mapNotNull { parseDisplayDate(it.purchaseDate) }
            .minOrNull()
            ?.toDisplayText()
            ?: todayDateText()

    fun fillWeightsFromPortfolio() {
        weight24 = ownedItems.filter { it.karat == "24K" }.sumOf { it.weightGrams }
        weight22 = ownedItems.filter { it.karat == "22K" }.sumOf { it.weightGrams }
        weight21 = ownedItems.filter { it.karat == "21K" }.sumOf { it.weightGrams }
        weight18 = ownedItems.filter { it.karat == "18K" }.sumOf { it.weightGrams }
        date24 = earliestPurchaseDate("24K")
        date22 = earliestPurchaseDate("22K")
        date21 = earliestPurchaseDate("21K")
        date18 = earliestPurchaseDate("18K")
    }

    // قائمة الأصناف في الزكاة مطابقة تماماً لقطع المحفظة المملوكة (غير المباعة):
    // أي حذف أو إضافة في المحفظة ينعكس هنا فوراً، وتكون فارغة إذا كانت المحفظة فارغة
    val allZakatItems = ownedItems.map { it.toZakatItem() }
    val displayedItems = if (showAllItems) allZakatItems else allZakatItems.take(3)

    val karatWeights = listOf("24K" to weight24, "22K" to weight22, "21K" to weight21, "18K" to weight18)
    val totalGoldValue = karatWeights.sumOf { (karat, w) ->
        GoldMarket.prices.first { it.karat == karat }.price * w
    }
    val totalWeight = karatWeights.sumOf { it.second }
    // نسبة النقاء تحوّل كل عيار لمكافئه من الذهب الخالص (عيار 24) قبل مقارنته
    // بالنصاب — فمثلاً 85 جراماً عيار 24 تعادل نحو 97 جراماً عيار 21
    val pureGoldEquivalent = karatWeights.sumOf { (karat, w) ->
        w * (karat.removeSuffix("K").toInt() / 24.0)
    }
    val nisabGrams = 85.0
    val nisabValue = GoldMarket.prices.first { it.karat == "24K" }.price * nisabGrams
    val exceedsNisab = pureGoldEquivalent >= nisabGrams || totalGoldValue >= nisabValue
    val totalZakat = if (exceedsNisab) totalGoldValue * (zakatPercent / 100.0) else 0.0
    val marketScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = t("رجوع", "Back"),
                    tint = Gold,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onBack() }
                )
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = t("تحديث", "Refresh"),
                    tint = Gold,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { marketScope.launch { GoldMarket.refresh() } }
                )
            }
            Text(t("الزكاة", "Zakat"), color = White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Share,
                    contentDescription = t("تصدير PDF", "Export PDF"),
                    tint = Gold,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable {
                            PdfExport.exportReport(
                                title = t("تقرير الزكاة — Gold Vision", "Zakat Report — Gold Vision"),
                                generatedAt = t("تاريخ التصدير: ${todayDateText()}", "Export date: ${todayDateText()}"),
                                summary = listOf(
                                    PdfReportRow(t("نصاب الزكاة", "Zakat Nisab"), "${fmt(nisabValue, 2, grouped = true)} ${t("ريال", "SAR")}"),
                                    PdfReportRow(t("إجمالي قيمة الذهب", "Total Gold Value"), "${fmt(totalGoldValue, 2, grouped = true)} ${t("ريال", "SAR")}"),
                                    PdfReportRow(t("إجمالي الوزن", "Total Weight"), "${fmt(totalWeight, 2)} ${t("جرام", "g")}"),
                                    PdfReportRow(
                                        t("حالة الزكاة", "Zakat Status"),
                                        if (exceedsNisab) t("واجبة", "Due") else t("غير واجبة (أقل من النصاب)", "Not due (below Nisab)")
                                    ),
                                    PdfReportRow(t("مبلغ الزكاة (${fmt(zakatPercent, 1)}%)", "Zakat Amount (${fmt(zakatPercent, 1)}%)"), "${fmt(totalZakat, 2, grouped = true)} ${t("ريال", "SAR")}")
                                ),
                                rows = allZakatItems.map { item ->
                                    val price = GoldMarket.prices.first { it.karat == item.karat }.price
                                    val itemValue = price * item.weightGrams
                                    PdfReportRow(
                                        "${item.name} (${karatLabel(item.karat)} • ${fmt(item.weightGrams, 2)} ${t("جم", "g")})",
                                        "${fmt(itemValue, 2, grouped = true)} ${t("ريال", "SAR")}"
                                    )
                                }
                            )
                        }
                )
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = t("معلومات عن زكاة الذهب", "About Gold Zakat"),
                    tint = Gold,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { showZakatInfo = true }
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        // ---- صندوق النصاب البارز: يتحدث حياً مع سعر الذهب العالمي ----
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, Border, RoundedCornerShape(12.dp))
                .background(CardBlack)
                .clickable { showZakatInfo = true }
                .padding(vertical = 12.dp, horizontal = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .border(1.dp, Gold, RoundedCornerShape(11.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = t("معلومات عن نصاب الزكاة", "About Zakat Nisab"),
                    tint = Gold,
                    modifier = Modifier.size(12.dp)
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(t("نصاب الزكاة (85 جم ذهب عيار 24)", "Zakat Nisab (85g of 24K gold)"), color = Gray, fontSize = 10.sp)
                Text(
                    "${fmt(nisabValue, 2, grouped = true)} ${t("ريال", "SAR")}",
                    color = Gold,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(t("يتحدّث مباشرة مع سعر الذهب العالمي", "Updates live with the global gold price"), color = Gray, fontSize = 8.5.sp)
            }
            Spacer(Modifier.width(22.dp))
        }

        Spacer(Modifier.height(10.dp))

        // ---- بطاقة الملخص العلوية: إجمالي قيمة الذهب / مبلغ الزكاة المستحق ----
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, Border, RoundedCornerShape(12.dp))
                .background(CardBlack)
                .padding(vertical = 16.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(19.dp))
                        .background(GoldDark.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ShowChart,
                        contentDescription = null,
                        tint = Gold,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column {
                    Text(t("إجمالي قيمة الذهب", "Total Gold Value"), color = Gray, fontSize = 10.sp)
                    Text(
                        fmt(totalGoldValue, 2, grouped = true),
                        color = Gold,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(t("ريال سعودي", "Saudi Riyal"), color = Gray, fontSize = 9.sp)
                }
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(52.dp)
                    .background(Border)
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(t("مبلغ الزكاة المستحق", "Zakat Amount Due"), color = Gray, fontSize = 10.sp)
                Text(
                    fmt(totalZakat, 2, grouped = true),
                    color = Gold,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(t("ريال سعودي", "Saudi Riyal"), color = Gray, fontSize = 9.sp)
                Text(
                    t("(${fmt(zakatPercent, 1)}%) من إجمالي قيمة الذهب", "(${fmt(zakatPercent, 1)}%) of total gold value"),
                    color = Gray,
                    fontSize = 8.5.sp
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        // ---- بطاقة تفاصيل الزكاة ----
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, Border, RoundedCornerShape(12.dp))
                .background(CardBlack)
                .padding(14.dp)
        ) {
            Text(
                t("تفاصيل الزكاة", "Zakat Details"),
                color = White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
            Spacer(Modifier.height(14.dp))

            Text(
                t("أوزان الذهب المملوكة (جرام)", "Owned Gold Weights (grams)"),
                color = Gray,
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ZakatKaratWeightInput("24K", weight24, date24, onDateClick = { activeDateKarat = "24K" }) { weight24 = it }
                ZakatKaratWeightInput("22K", weight22, date22, onDateClick = { activeDateKarat = "22K" }) { weight22 = it }
                ZakatKaratWeightInput("21K", weight21, date21, onDateClick = { activeDateKarat = "21K" }) { weight21 = it }
                ZakatKaratWeightInput("18K", weight18, date18, onDateClick = { activeDateKarat = "18K" }) { weight18 = it }
            }

            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background((if (exceedsNisab) Green else Gray).copy(alpha = 0.15f))
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (exceedsNisab) t("الزكاة واجبة", "Zakat is due") else t("الزكاة غير واجبة (أقل من النصاب)", "Zakat not due (below Nisab)"),
                    color = if (exceedsNisab) Green else Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                ZakatDetailItem(
                    title = t("نصاب الزكاة (الذهب)", "Zakat Nisab (Gold)"),
                    value = fmt(nisabValue, 2, grouped = true),
                    unit = t("ريال (85 جرام ع24)", "SAR (85g of 24K)"),
                    modifier = Modifier.weight(1f)
                )
                ZakatDetailItem(
                    title = t("إجمالي الوزن", "Total Weight"),
                    value = fmt(totalWeight, 3),
                    unit = t("جرام", "g"),
                    modifier = Modifier.weight(1f)
                )
                ZakatDetailItem(
                    title = t("إجمالي قيمة الذهب", "Total Gold Value"),
                    value = fmt(totalGoldValue, 2, grouped = true),
                    unit = t("ريال", "SAR"),
                    modifier = Modifier.weight(1f)
                )
                ZakatDetailItem(
                    title = t("مبلغ الزكاة (${fmt(zakatPercent, 1)}%)", "Zakat Amount (${fmt(zakatPercent, 1)}%)"),
                    value = fmt(totalZakat, 2, grouped = true),
                    unit = t("ريال", "SAR"),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        // ---- صندوق توضيحي عن شرط النصاب ----
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, Border, RoundedCornerShape(10.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = Gold,
                modifier = Modifier
                    .size(15.dp)
                    .padding(top = 1.dp)
            )
            Text(
                t(
                    "يجب الزكاة إذا بلغ الذهب المملوك النصاب الشرعي، وهو 85 جراماً من عيار 24 " +
                            "(أو ما يعادلها بالعيارات الأخرى)، أي ما قيمته الآن نحو " +
                            "${fmt(nisabValue, 2, grouped = true)} ريال.\n" +
                            "قيمة ذهبك الحالية (${fmt(totalGoldValue, 2, grouped = true)} ريال) " +
                            (if (exceedsNisab) "تتجاوز" else "لا تتجاوز") +
                            " النصاب.",
                    "Zakat is due if your owned gold reaches the Shariah Nisab of 85 grams of 24K gold " +
                            "(or its equivalent in other karats), currently worth about " +
                            "${fmt(nisabValue, 2, grouped = true)} SAR.\n" +
                            "Your current gold value (${fmt(totalGoldValue, 2, grouped = true)} SAR) " +
                            (if (exceedsNisab) "exceeds" else "does not exceed") +
                            " the Nisab."
                ),
                color = Gray,
                fontSize = 10.sp,
                lineHeight = 15.sp,
                textAlign = TextAlign.End
            )
        }

        Spacer(Modifier.height(10.dp))

        // ---- بطاقة تفاصيل الأصناف المحسوبة ----
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, Border, RoundedCornerShape(12.dp))
                .background(CardBlack)
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { showAllItems = !showAllItems }
                ) {
                    Text(
                        if (showAllItems) t("عرض أقل", "Show Less") else t("عرض الكل", "Show All"),
                        color = Gold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                        contentDescription = null,
                        tint = Gold,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Text(t("تفاصيل الأصناف المحسوبة", "Calculated Items Details"), color = White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .border(1.dp, Gold, RoundedCornerShape(9.dp))
                    .clickable { fillWeightsFromPortfolio() },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.AccountBalanceWallet,
                    contentDescription = null,
                    tint = Gold,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    t("استخدام الأوزان الموجودة في المحفظة", "Use weights from Portfolio"),
                    color = Gold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(t("مبلغ الزكاة", "Zakat"), color = White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Text(t("الحالة", "Status"), color = White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.1f), textAlign = TextAlign.Center)
                Text(t("قيمة الذهب", "Gold Value"), color = White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Text(t("الوزن (جم)", "Weight (g)"), color = White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.8f), textAlign = TextAlign.Center)
                Text(t("عيار", "Karat"), color = White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.6f), textAlign = TextAlign.Center)
                Text(t("الصنف", "Item"), color = White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f), textAlign = TextAlign.Center)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Border)
            )

            displayedItems.forEach { item ->
                ZakatItemRow(item = item, zakatPercent = zakatPercent, exceedsNisab = exceedsNisab)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Border.copy(alpha = 0.4f))
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .border(1.dp, Gold, RoundedCornerShape(9.dp))
                    .clickable { onNavigateAddItem() },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(t("إضافة صنف جديد", "Add New Item"), color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("+", color = Gold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // ---- بطاقة إعدادات حساب الزكاة: مطوية افتراضياً حتى لا تأخذ حيزاً كبيراً ----
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, Border, RoundedCornerShape(12.dp))
                .background(CardBlack)
                .padding(horizontal = 14.dp, vertical = if (settingsExpanded) 14.dp else 10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { settingsExpanded = !settingsExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (settingsExpanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                    contentDescription = if (settingsExpanded) t("طي الإعدادات", "Collapse Settings") else t("عرض الإعدادات", "Show Settings"),
                    tint = Gold,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    t("إعدادات حساب الزكاة", "Zakat Calculation Settings"),
                    color = White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (!settingsExpanded) return@Column
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(t("نسبة الزكاة", "Zakat Rate"), color = Gray, fontSize = 9.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, Border, RoundedCornerShape(8.dp))
                            .clickable {
                                zakatPercent = if (zakatPercent == 2.5) 2.577 else 2.5
                            },
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            t("(الذهب) ${fmt(zakatPercent, 2).trimEnd('0').trimEnd('.')}%", "(Gold) ${fmt(zakatPercent, 2).trimEnd('0').trimEnd('.')}%"),
                            color = White,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Spacer(Modifier.width(3.dp))
                        Text("˅", color = Gold, fontSize = 10.sp)
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(t("تاريخ حساب الزكاة", "Zakat Calculation Date"), color = Gray, fontSize = 9.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, Border, RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarMonth,
                            contentDescription = null,
                            tint = Gold,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(currentZakatDate, color = White, fontSize = 9.5.sp, maxLines = 1)
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(t("تاريخ حساب الزكاة", "Zakat Calculation Date"), color = Gray, fontSize = 9.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, Border, RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarMonth,
                            contentDescription = null,
                            tint = Gold,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(previousZakatDate, color = White, fontSize = 9.5.sp, maxLines = 1)
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(Green.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Green)
                    )
                }
                Text(
                    t("سيتم حساب حولان الحول 5 أيام وتحديده لك تلقائياً", "The Hawl completion will be calculated in 5 days and set for you automatically"),
                    color = Gray,
                    fontSize = 9.sp
                )
            }
        }

        Spacer(Modifier.height(16.dp))
    }

    if (showZakatInfo) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .clickable(
                    indication = null,
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                ) { showZakatInfo = false },
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, Border, RoundedCornerShape(14.dp))
                    .background(CardBlack)
                    .clickable(
                        indication = null,
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    ) { }
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(t("شروط وجوب زكاة الذهب", "Conditions for Gold Zakat"), color = White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = t("إغلاق", "Close"),
                        tint = Gray,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { showZakatInfo = false }
                    )
                }

                Spacer(Modifier.height(6.dp))
                Text(
                    t(
                        "النصاب الحالي: ${fmt(nisabValue, 2, grouped = true)} ريال (يعادل 85 جراماً من عيار 24)",
                        "Current Nisab: ${fmt(nisabValue, 2, grouped = true)} SAR (equivalent to 85g of 24K gold)"
                    ),
                    color = Gold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(14.dp))
                Text(t("شروط الوجوب", "Conditions for Zakat"), color = White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
                Spacer(Modifier.height(6.dp))
                Text(
                    t(
                        "• بلوغ النصاب: 85 جراماً من الذهب الخالص (عيار 24)، وما يعادلها بالعيارات الأخرى (نحو 97 جراماً لعيار 21).\n" +
                                "• مرور الحول: أن يمضي عام هجري كامل على امتلاك النصاب.\n" +
                                "• الملك التام: أن يكون الذهب مملوكاً بالكامل وغير مرهون.",
                        "• Reaching the Nisab: 85 grams of pure gold (24K), or its equivalent in other karats (about 97 grams for 21K).\n" +
                                "• Passage of a Hawl: a full Hijri year must pass while owning the Nisab.\n" +
                                "• Full ownership: the gold must be fully owned and not mortgaged."
                    ),
                    color = Gray,
                    fontSize = 10.5.sp,
                    lineHeight = 17.sp,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(14.dp))
                Text(t("حكم ذهب الزينة", "Ruling on Adornment Gold"), color = White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
                Spacer(Modifier.height(6.dp))
                Text(
                    t(
                        "• ذهب الاستعمال الشخصي (الحلي المعتاد): لا زكاة فيه عند جمهور أهل العلم.\n" +
                                "• ذهب الادخار أو الاستثمار: تجب فيه الزكاة اتفاقاً إذا بلغ النصاب وحال عليه الحول.",
                        "• Gold for personal use (customary jewelry): most scholars hold there is no Zakat on it.\n" +
                                "• Gold for savings or investment: Zakat is due on it by consensus if it reaches the Nisab and a Hawl passes."
                    ),
                    color = Gray,
                    fontSize = 10.5.sp,
                    lineHeight = 17.sp,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(14.dp))
                Text(
                    t("مقدار الزكاة الواجب إخراجه: 2.5% (ربع العشر) من قيمة الذهب.", "The Zakat amount due: 2.5% (a quarter of a tenth) of the gold's value."),
                    color = Gray,
                    fontSize = 10.5.sp,
                    lineHeight = 17.sp,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(Gold)
                        .clickable { showZakatInfo = false },
                    contentAlignment = Alignment.Center
                ) {
                    Text(t("حسناً", "OK"), color = Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    activeDateKarat?.let { karat ->
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { activeDateKarat = null },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val text = dateTextFromEpochMillis(millis)
                        when (karat) {
                            "24K" -> date24 = text
                            "22K" -> date22 = text
                            "21K" -> date21 = text
                            "18K" -> date18 = text
                        }
                    }
                    activeDateKarat = null
                }) {
                    Text(t("موافق", "OK"))
                }
            },
            dismissButton = {
                TextButton(onClick = { activeDateKarat = null }) {
                    Text(t("إلغاء", "Cancel"))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun ZakatDetailItem(title: String, value: String, unit: String, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            title,
            color = Gray,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            maxLines = 2,
            lineHeight = 13.sp
        )
        Spacer(Modifier.height(6.dp))
        Text(
            value,
            color = Gold,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(unit, color = Gray, fontSize = 10.sp, maxLines = 1)
    }
}

@Composable
private fun ZakatKaratWeightInput(
    karat: String,
    value: Double,
    dateText: String,
    onDateClick: () -> Unit,
    onValueChanged: (Double) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            karat.removeSuffix("K"),
            color = Gold,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(6.dp))
        NumericInputField(
            value = value,
            onValueChanged = onValueChanged,
            fontSize = 15.sp,
            minValue = 0.0,
            modifier = Modifier
                .width(66.dp)
                .height(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, Border, RoundedCornerShape(8.dp))
        )
        Spacer(Modifier.height(6.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            modifier = Modifier.clickable { onDateClick() }
        ) {
            Icon(
                imageVector = Icons.Outlined.CalendarMonth,
                contentDescription = t("تاريخ الشراء", "Purchase Date"),
                tint = Gray,
                modifier = Modifier.size(11.dp)
            )
            Text(dateText, color = Gray, fontSize = 9.sp, maxLines = 1)
        }
    }
}

@Composable
private fun ZakatItemRow(item: ZakatItem, zakatPercent: Double, exceedsNisab: Boolean) {
    val pricePerGram = GoldMarket.prices.first { it.karat == item.karat }.price
    val goldValue = pricePerGram * item.weightGrams
    val zakatAmount = goldValue * (zakatPercent / 100.0)
    val status = zakatStatusFor(item.purchaseDate, exceedsNisab)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            fmt(zakatAmount, 2, grouped = true),
            color = White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )

        Column(
            modifier = Modifier.weight(1.1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(status.color.copy(alpha = 0.15f))
                    .padding(horizontal = 7.dp, vertical = 4.dp)
            ) {
                Text(
                    status.label,
                    color = status.color,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(status.caption, color = Gray, fontSize = 7.5.sp, maxLines = 1)
        }

        Text(
            fmt(goldValue, 2, grouped = true),
            color = Gold,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )

        Text(
            fmt(item.weightGrams, 2),
            color = White,
            fontSize = 11.sp,
            modifier = Modifier.weight(0.8f),
            textAlign = TextAlign.Center
        )

        Text(
            item.karat.removeSuffix("K"),
            color = White,
            fontSize = 11.sp,
            modifier = Modifier.weight(0.6f),
            textAlign = TextAlign.Center
        )

        Row(
            modifier = Modifier.weight(1.2f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(GoldDark.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Text(item.emoji, fontSize = 10.sp)
            }
            Spacer(Modifier.width(4.dp))
            Text(
                item.name,
                color = White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ==================== صفحات مؤقتة ====================
@Composable
private fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(title, color = Gold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

// ==================== شاشة المزيد: الملف الشخصي والإعدادات ====================
@Composable
private fun MoreScreen(
    profile: UserProfile,
    signedInEmail: String?,
    onNavigateProfile: () -> Unit,
    onNavigatePrivacyPolicy: () -> Unit,
    onNavigateNotifications: () -> Unit,
    onBack: () -> Unit
) {
    var showLanguageDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = t("رجوع", "Back"),
                tint = Gold,
                modifier = Modifier
                    .size(20.dp)
                    .clickable { onBack() }
            )
            Text(
                t("المزيد", "More"),
                color = Gold,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(9.dp))
                .border(1.dp, Border, RoundedCornerShape(9.dp))
                .background(CardBlack)
                .clickable { onNavigateProfile() }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(23.dp))
                        .background(Gold),
                    contentAlignment = Alignment.Center
                ) {
                    Text(profile.avatar, fontSize = 20.sp)
                }
                Column {
                    Text(
                        profile.name.ifBlank { t("أضف اسمك", "Add your name") },
                        color = White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        signedInEmail ?: t("تعديل الملف الشخصي - سجّل دخولك", "Edit profile - sign in"),
                        color = Gray,
                        fontSize = 10.sp
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                contentDescription = null,
                tint = Gold,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(t("الإعدادات", "Settings"), color = Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(9.dp))
                .border(1.dp, Border, RoundedCornerShape(9.dp))
                .background(CardBlack)
        ) {
            SettingsRow(
                icon = Icons.Outlined.Notifications,
                label = t("الإشعارات", "Notifications"),
                onClick = onNavigateNotifications
            )
            SettingsDivider()
            SettingsRow(
                icon = Icons.Outlined.Language,
                label = t("اللغة", "Language"),
                value = if (AppLanguage.current == AppLang.EN) "English" else "العربية",
                onClick = { showLanguageDialog = true }
            )
            SettingsDivider()
            SettingsRow(icon = Icons.Outlined.AttachMoney, label = t("العملة", "Currency"))
            SettingsDivider()
            SettingsRow(icon = Icons.Outlined.Info, label = t("عن التطبيق", "About"))
            SettingsDivider()
            SettingsRow(
                icon = Icons.Outlined.Balance,
                label = t("سياسة الخصوصية", "Privacy Policy"),
                onClick = onNavigatePrivacyPolicy
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(t("الأمان", "Security"), color = Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))

        var appLockEnabled by remember { mutableStateOf(loadAppLockSettings().enabled) }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(9.dp))
                .border(1.dp, Border, RoundedCornerShape(9.dp))
                .background(CardBlack)
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(t("قفل التطبيق ببصمة/وجه", "App lock with fingerprint/face"), color = White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(
                    t(
                        "يطلب بصمتك أو وجهك أو رمز الجهاز عند كل فتح للتطبيق",
                        "Requires your fingerprint, face, or device code every time you open the app"
                    ),
                    color = Gray,
                    fontSize = 9.5.sp,
                    lineHeight = 14.sp
                )
            }
            Switch(
                checked = appLockEnabled,
                onCheckedChange = { enabled ->
                    appLockEnabled = enabled
                    persistAppLockSettings(AppLockSettings(enabled = enabled))
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Black,
                    checkedTrackColor = Gold,
                    uncheckedThumbColor = Gray,
                    uncheckedTrackColor = CardBlack
                )
            )
        }

        Spacer(Modifier.height(16.dp))
    }

    if (showLanguageDialog) {
        LanguagePickerDialog(onDismiss = { showLanguageDialog = false })
    }
}

@Composable
private fun SettingsRow(icon: ImageVector, label: String, tint: Color = Gold, value: String? = null, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
            Text(label, color = if (tint == Red) Red else White, fontSize = 12.sp)
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (value != null) {
                Text(value, color = Gray, fontSize = 11.sp)
            }
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                contentDescription = null,
                tint = Gray,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .height(1.dp)
            .background(Border)
    )
}

// ==================== شاشة الملف الشخصي (اسم/صورة محليان + حساب اختياري بالإيميل) ====================
@Composable
private fun ProfileScreen(
    profile: UserProfile,
    signedInEmail: String?,
    onNavigateAuth: () -> Unit,
    onSignOut: () -> Unit,
    onBack: () -> Unit,
    onSave: (UserProfile) -> Unit
) {
    var name by remember { mutableStateOf(profile.name) }
    var selectedAvatar by remember { mutableStateOf(profile.avatar) }
    var showSignOutConfirm by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = t("رجوع", "Back"),
                tint = Gold,
                modifier = Modifier
                    .size(22.dp)
                    .clickable { onBack() }
            )
            Text(
                t("الملف الشخصي", "Profile"),
                color = White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.size(22.dp))
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, Border, RoundedCornerShape(10.dp))
                .background(CardBlack)
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(t("الحساب", "Account"), color = White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(
                    signedInEmail ?: t("لم تسجّل الدخول بعد — تسجيل الدخول اختياري", "Not signed in yet — sign in is optional"),
                    color = Gray,
                    fontSize = 10.sp
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, if (signedInEmail != null) Red else Gold, RoundedCornerShape(8.dp))
                    .clickable { if (signedInEmail != null) showSignOutConfirm = true else onNavigateAuth() }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    if (signedInEmail != null) t("تسجيل الخروج", "Sign Out") else t("تسجيل الدخول", "Sign In"),
                    color = if (signedInEmail != null) Red else Gold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // الاسم والصورة الرمزية يظهران بعد تسجيل الدخول فقط — قبله ما فيه
        // حساب فعلي يُربط به الاسم، فيبقى الملف الشخصي بس بطاقة تسجيل الدخول
        if (signedInEmail != null) {
            Spacer(Modifier.height(16.dp))
            Text(
                t("الاسم والصورة الرمزية أدناه محليان على جهازك فقط", "The name and avatar below are local to your device only"),
                color = Gray,
                fontSize = 10.sp
            )

            Spacer(Modifier.height(20.dp))

            Text(t("الاسم", "Name"), color = Gray, fontSize = 10.sp)
            Spacer(Modifier.height(6.dp))
            SelectableTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = t("مثال: محمد العتيبي", "e.g. John Smith"),
                modifier = Modifier.fillMaxWidth().height(42.dp)
            )

            Spacer(Modifier.height(16.dp))

            Text(t("الصورة الرمزية", "Avatar"), color = Gray, fontSize = 10.sp)
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                profileAvatarOptions.forEach { avatar ->
                    val selected = avatar == selectedAvatar
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .border(
                                width = if (selected) 1.5.dp else 1.dp,
                                color = if (selected) Gold else Border,
                                shape = RoundedCornerShape(24.dp)
                            )
                            .background(if (selected) GoldDark.copy(alpha = 0.2f) else Color.Transparent)
                            .clickable { selectedAvatar = avatar },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(avatar, fontSize = 20.sp)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Gold)
                    .clickable { onSave(UserProfile(name = name.trim(), avatar = selectedAvatar)) },
                contentAlignment = Alignment.Center
            ) {
                Text(t("حفظ", "Save"), color = Black, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(16.dp))
    }

    if (showSignOutConfirm) {
        ConfirmDialog(
            title = t("تسجيل الخروج", "Sign Out"),
            message = t("هل أنت متأكد من تسجيل الخروج؟", "Are you sure you want to sign out?"),
            confirmLabel = t("تسجيل الخروج", "Sign Out"),
            confirmColor = Red,
            onDismiss = { showSignOutConfirm = false },
            onConfirm = {
                showSignOutConfirm = false
                onSignOut()
            }
        )
    }
    }
}

// نافذة اختيار لغة التطبيق — تُطبَّق فوراً وتُحفظ محلياً، تشمل اتجاه
// الواجهة كاملة (RTL/LTR) بجانب النصوص المُترجَمة عبر t()
@Composable
private fun LanguagePickerDialog(onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 28.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, Border, RoundedCornerShape(14.dp))
                .background(CardBlack)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { }
                .padding(18.dp)
        ) {
            Text(t("اللغة", "Language"), color = White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(14.dp))

            LanguageOptionRow(
                label = "العربية",
                selected = AppLanguage.current == AppLang.AR,
                onClick = {
                    AppLanguage.set(AppLang.AR)
                    onDismiss()
                }
            )
            Spacer(Modifier.height(8.dp))
            LanguageOptionRow(
                label = "English",
                selected = AppLanguage.current == AppLang.EN,
                onClick = {
                    AppLanguage.set(AppLang.EN)
                    onDismiss()
                }
            )
        }
    }
}

@Composable
private fun LanguageOptionRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(9.dp))
            .border(1.dp, if (selected) Gold else Border, RoundedCornerShape(9.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = if (selected) Gold else White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        if (selected) {
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = null,
                tint = Gold,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// نافذة تأكيد عامة (نعم/إلغاء) بنفس أسلوب نوافذ التطبيق الأخرى
@Composable
private fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    confirmColor: Color,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 28.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, Border, RoundedCornerShape(14.dp))
                .background(CardBlack)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { }
                .padding(18.dp)
        ) {
            Text(title, color = White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(message, color = Gray, fontSize = 11.sp)
            Spacer(Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .border(1.dp, Border, RoundedCornerShape(9.dp))
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("إلغاء", color = Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(confirmColor)
                        .clickable { onConfirm() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(confirmLabel, color = White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==================== شاشة تسجيل الدخول / إنشاء حساب بالإيميل ====================
@Composable
private fun AuthScreen(onBack: () -> Unit, onAuthSuccess: () -> Unit) {
    var isSignUpMode by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }
    var infoText by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val triggerGoogleSignIn = GoogleSignInLauncher.rememberLauncher { idToken, error ->
        when {
            idToken != null -> {
                isLoading = true
                scope.launch {
                    val err = AuthService.completeGoogleSignIn(idToken)
                    isLoading = false
                    if (err != null) errorText = err else onAuthSuccess()
                }
            }
            error != null -> errorText = error
            // else: المستخدم أغلق نافذة اختيار الحساب بنفسه، بلا أي إجراء
        }
    }

    fun submit() {
        errorText = null
        infoText = null
        val trimmedEmail = email.trim()
        if (!isValidEmail(trimmedEmail)) {
            errorText = t("الرجاء إدخال بريد إلكتروني صحيح", "Please enter a valid email address")
            return
        }
        if (password.length < 6) {
            errorText = t("كلمة المرور 6 أحرف على الأقل", "Password must be at least 6 characters")
            return
        }
        if (isSignUpMode && password != confirmPassword) {
            errorText = t("كلمتا المرور غير متطابقتين", "Passwords do not match")
            return
        }
        isLoading = true
        scope.launch {
            val error = if (isSignUpMode) {
                AuthService.signUp(trimmedEmail, password)
            } else {
                AuthService.signIn(trimmedEmail, password)
            }
            isLoading = false
            if (error != null) {
                errorText = error
            } else if (isSignUpMode) {
                // نُبقي المستخدم لحظة على الشاشة ليرى تنبيه إرسال رابط
                // تأكيد البريد قبل الانتقال لباقي التطبيق
                infoText = t("تم إنشاء حسابك بنجاح، وأُرسل رابط تأكيد إلى بريدك الإلكتروني", "Your account was created successfully, and a confirmation link was sent to your email")
                delay(1600)
                onAuthSuccess()
            } else {
                onAuthSuccess()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = t("رجوع", "Back"),
                tint = Gold,
                modifier = Modifier
                    .size(22.dp)
                    .clickable { onBack() }
            )
            Text(
                if (isSignUpMode) t("حساب جديد", "New Account") else t("تسجيل الدخول", "Sign In"),
                color = White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.size(22.dp))
        }

        Spacer(Modifier.height(16.dp))
        Text(
            t("اختياري تماماً — التطبيق يعمل بكامل ميزاته بلا تسجيل دخول", "Completely optional — the app works with all its features without signing in"),
            color = Gray,
            fontSize = 10.sp
        )

        Spacer(Modifier.height(20.dp))

        Text(t("البريد الإلكتروني", "Email"), color = Gray, fontSize = 10.sp)
        Spacer(Modifier.height(6.dp))
        SelectableTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = "example@email.com",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth().height(42.dp)
        )

        Spacer(Modifier.height(12.dp))

        Text(t("كلمة المرور", "Password"), color = Gray, fontSize = 10.sp)
        Spacer(Modifier.height(6.dp))
        SelectableTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = t("6 أحرف على الأقل", "At least 6 characters"),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth().height(42.dp)
        )

        if (isSignUpMode) {
            Spacer(Modifier.height(12.dp))
            Text(t("تأكيد كلمة المرور", "Confirm Password"), color = Gray, fontSize = 10.sp)
            Spacer(Modifier.height(6.dp))
            SelectableTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = "",
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth().height(42.dp)
            )
        } else {
            Spacer(Modifier.height(8.dp))
            Text(
                t("نسيت كلمة المرور؟", "Forgot password?"),
                color = Gold,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    val trimmedEmail = email.trim()
                    if (!isValidEmail(trimmedEmail)) {
                        errorText = t("أدخل بريدك الإلكتروني أول لاستعادة كلمة المرور", "Enter your email first to reset your password")
                        return@clickable
                    }
                    errorText = null
                    scope.launch {
                        val error = AuthService.sendPasswordReset(trimmedEmail)
                        infoText = if (error == null) t("أُرسل رابط استعادة كلمة المرور إلى بريدك", "A password reset link was sent to your email") else null
                        errorText = error
                    }
                }
            )
        }

        errorText?.let {
            Spacer(Modifier.height(10.dp))
            Text(it, color = Red, fontSize = 10.sp)
        }
        infoText?.let {
            Spacer(Modifier.height(10.dp))
            Text(it, color = Green, fontSize = 10.sp)
        }

        Spacer(Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Gold)
                .clickable(enabled = !isLoading) { submit() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (isLoading) t("جارٍ...", "Loading...") else if (isSignUpMode) t("إنشاء الحساب", "Create Account") else t("تسجيل الدخول", "Sign In"),
                color = Black,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(14.dp))

        Text(
            if (isSignUpMode) t("عندك حساب؟ سجّل الدخول", "Have an account? Sign in") else t("ماعندك حساب؟ أنشئ واحداً جديداً", "No account? Create a new one"),
            color = Gray,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    isSignUpMode = !isSignUpMode
                    errorText = null
                    infoText = null
                },
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(22.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.weight(1f).height(1.dp).background(Border))
            Text(t("أو", "or"), color = Gray, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 10.dp))
            Box(modifier = Modifier.weight(1f).height(1.dp).background(Border))
        }
        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, Border, RoundedCornerShape(10.dp))
                .clickable(enabled = !isLoading) { triggerGoogleSignIn() },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("G", color = Gold, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(8.dp))
            Text(t("الدخول بحساب جوجل", "Sign in with Google"), color = White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(16.dp))
    }
}

// ==================== شاشة سياسة الخصوصية (داخل التطبيق) ====================
// دالة بدل val ثابتة حتى تبقى النصوص متجاوبة مع اللغة الحالية عند كل استدعاء
private fun privacyPolicySections(): List<Pair<String, String>> = listOf(
    t("البيانات المحفوظة على جهازك فقط", "Data stored on your device only") to
        t(
            "قطع المحفظة، بيانات الزكاة، والملف الشخصي (الاسم والصورة الرمزية) تُحفظ كملفات محلية على جهازك فقط، بمساحة تخزين خاصة بالتطبيق لا يصل إليها أي تطبيق آخر — لا تُرفع لأي خادم، ولا تُشارك أو تُباع لأي طرف ثالث تحت أي ظرف.",
            "Portfolio items, Zakat data, and your profile (name and avatar) are stored as local files on your device only, in app-private storage that no other app can access — they are never uploaded to any server, and never shared or sold to any third party under any circumstance."
        ),
    t("تسجيل الدخول (اختياري)", "Sign-in (optional)") to
        t(
            "يمكنك استخدام التطبيق بكامل ميزاته بلا أي تسجيل دخول. إن اخترت إنشاء حساب بالبريد الإلكتروني، تُدار عملية الدخول عبر مزوّد خدمات مصادقة عالمي موثوق ومتخصص، وتُحفظ كلمة المرور لديه بشكل مشفَّر بالكامل — نحن أنفسنا لا نطّلع عليها ولا نحتفظ بنسخة منها. الملف الشخصي (الاسم والصورة الرمزية) منفصل تماماً ومحفوظ على جهازك فقط بغض النظر عن تسجيل الدخول.",
            "You can use the app with all its features without signing in at all. If you choose to create an account by email, the sign-in process is managed by a trusted, specialized global authentication provider, and your password is stored there fully encrypted — we ourselves never see it or keep a copy of it. Your profile (name and avatar) is entirely separate and stored on your device only, regardless of sign-in."
        ),
    t("أسعار الذهب والأخبار", "Gold prices and news") to
        t(
            "تُجلب الأسعار الحية والتاريخية، وكذلك الأخبار المتعلقة بالذهب، من مصادر بيانات عامة متخصصة، دون إرسال أي معلومة تعرّف بك أو ببياناتك المحفوظة إليها.",
            "Live and historical prices, as well as gold-related news, are fetched from specialized public data sources, without sending any information that identifies you or your saved data to them."
        ),
    t("الأمان وحماية البيانات", "Security and data protection") to
        t(
            "كل اتصال بين التطبيق وأي مصدر بيانات خارجي مشفَّر بالكامل. لا يجمع التطبيق أو يطّلع على أي بيانات بطاقة دفع أو حساب بنكي إطلاقاً. لأسباب أمنية، لا يُفصح هذا التطبيق عن التفاصيل التقنية الداخلية لكيفية عمله.",
            "Every connection between the app and any external data source is fully encrypted. The app never collects or accesses any payment card or bank account data whatsoever. For security reasons, this app does not disclose internal technical details of how it works."
        ),
    t("تتبع الأعطال التقنية", "Technical crash tracking") to
        t(
            "عند حدوث عطل تقني أو فشل في تحديث الأسعار، تُرسل رسالة تشخيصية تقنية مجهولة (بلا اسمك أو بياناتك) لمساعدتنا على اكتشاف المشكلة وإصلاحها بسرعة.",
            "When a technical fault or a price update failure occurs, an anonymous technical diagnostic message (without your name or data) is sent to help us detect the issue and fix it quickly."
        ),
    t("إحصاءات استخدام مجهولة", "Anonymous usage statistics") to
        t(
            "نجمع إحصاءات مجهولة تماماً عن استخدام الشاشات (بلا اسمك أو أي رقم يعرّفك) لفهم الميزات الأكثر استخداماً وتحسين التطبيق، بلا أي تسجيل لجلسات الشاشة.",
            "We collect completely anonymous statistics about screen usage (without your name or any identifying number) to understand the most-used features and improve the app, with no recording of screen sessions."
        ),
    t("التواصل", "Contact") to
        t(
            "لأي استفسار أو طلب حذف بيانات، يمكن التواصل عبر البريد الإلكتروني الموضّح في صفحة التطبيق على المتجر.",
            "For any inquiry or data deletion request, you can reach out via the email address shown on the app's store page."
        )
)

@Composable
private fun PrivacyPolicyScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = t("رجوع", "Back"),
                tint = Gold,
                modifier = Modifier
                    .size(22.dp)
                    .clickable { onBack() }
            )
            Text(
                t("سياسة الخصوصية", "Privacy Policy"),
                color = White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.size(22.dp))
        }

        Spacer(Modifier.height(16.dp))

        val sections = privacyPolicySections()
        sections.forEachIndexed { index, (title, body) ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, Border, RoundedCornerShape(10.dp))
                    .background(CardBlack)
                    .padding(12.dp)
            ) {
                Text(title, color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text(body, color = Gray, fontSize = 11.sp, lineHeight = 17.sp)
            }
            if (index != sections.lastIndex) {
                Spacer(Modifier.height(10.dp))
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

// ==================== شاشة إعدادات الإشعارات ====================
@Composable
private fun NotificationSettingsScreen(
    settings: NotificationSettings,
    onBack: () -> Unit,
    onToggleDailyPrice: (Boolean) -> Unit,
    onToggleFedMeetingAlerts: (Boolean) -> Unit
) {
    var priceAlerts by remember { mutableStateOf(loadPriceAlerts()) }
    var showAddAlertDialog by remember { mutableStateOf(false) }

    fun persistAlerts(updated: List<PriceAlert>) {
        priceAlerts = updated
        persistPriceAlerts(updated)
        PriceAlertScheduler.setActive(updated.isNotEmpty())
    }

    Box(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = t("رجوع", "Back"),
                tint = Gold,
                modifier = Modifier
                    .size(22.dp)
                    .clickable { onBack() }
            )
            Text(
                t("الإشعارات", "Notifications"),
                color = White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.size(22.dp))
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, Border, RoundedCornerShape(10.dp))
                .background(CardBlack)
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(t("سعر الذهب اليومي", "Daily Gold Price"), color = White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(
                    t("إشعار يومي بسعري الافتتاح والإغلاق الفعليين لعيار 24 وعيار 21", "A daily notification with the actual open and close prices for 24K and 21K"),
                    color = Gray,
                    fontSize = 10.sp,
                    lineHeight = 15.sp
                )
            }
            Switch(
                checked = settings.dailyPriceEnabled,
                onCheckedChange = onToggleDailyPrice,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Black,
                    checkedTrackColor = Gold,
                    uncheckedThumbColor = Gray,
                    uncheckedTrackColor = CardBlack
                )
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, Border, RoundedCornerShape(10.dp))
                .background(CardBlack)
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(t("مواعيد اجتماعات الفيدرالي", "Fed Meeting Dates"), color = White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(
                    t("تذكير يوم الاجتماع وقبله بيوم واحد، مع موعد إعلان القرار (9:00 مساءً بتوقيت مكة)", "A reminder on the meeting day and the day before, with the decision announcement time (9:00 PM Makkah time)"),
                    color = Gray,
                    fontSize = 10.sp,
                    lineHeight = 15.sp
                )
            }
            Switch(
                checked = settings.fedMeetingAlertsEnabled,
                onCheckedChange = onToggleFedMeetingAlerts,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Black,
                    checkedTrackColor = Gold,
                    uncheckedThumbColor = Gray,
                    uncheckedTrackColor = CardBlack
                )
            )
        }

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(t("تنبيهات الأسعار", "Price Alerts"), color = White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.clickable { showAddAlertDialog = true }
            ) {
                Text(t("إضافة", "Add"), color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("+", color = Gold, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            t("نبّهني عند وصول سعر عيار معيّن لسعر مستهدف — يعمل مرة واحدة لكل تنبيه", "Notify me when a karat's price reaches a target — fires once per alert"),
            color = Gray,
            fontSize = 10.sp,
            lineHeight = 15.sp
        )
        Spacer(Modifier.height(10.dp))

        if (priceAlerts.isEmpty()) {
            Text(t("لا توجد تنبيهات أسعار حالياً", "No price alerts right now"), color = Gray, fontSize = 11.sp)
        } else {
            priceAlerts.forEach { alert ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .border(1.dp, Border, RoundedCornerShape(9.dp))
                        .background(CardBlack)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ShowChart,
                            contentDescription = null,
                            tint = if (alert.isUpward) Green else Red,
                            modifier = Modifier.size(16.dp)
                        )
                        Column {
                            Text(karatLabel(alert.karat), color = White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "${if (alert.isUpward) t("عند الوصول لـ", "On reaching") else t("عند النزول لـ", "On dropping to")} " +
                                    "${fmt(alert.targetPrice, 2, grouped = true)} ${t("ريال", "SAR")}",
                                color = Gray,
                                fontSize = 9.5.sp
                            )
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Switch(
                            checked = alert.isEnabled,
                            onCheckedChange = { enabled ->
                                persistAlerts(priceAlerts.map { if (it.id == alert.id) it.copy(isEnabled = enabled) else it })
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Black,
                                checkedTrackColor = Gold,
                                uncheckedThumbColor = Gray,
                                uncheckedTrackColor = CardBlack
                            ),
                            modifier = Modifier.scale(0.7f)
                        )
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = t("حذف التنبيه", "Delete Alert"),
                            tint = Gray,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { persistAlerts(priceAlerts.filter { it.id != alert.id }) }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }

    if (showAddAlertDialog) {
        AddPriceAlertDialog(
            onDismiss = { showAddAlertDialog = false },
            onConfirm = { karat, targetPrice, isUpward ->
                val newAlert = PriceAlert(
                    id = Clock.System.now().toEpochMilliseconds().toString(),
                    karat = karat,
                    targetPrice = targetPrice,
                    isUpward = isUpward
                )
                persistAlerts(priceAlerts + newAlert)
                showAddAlertDialog = false
            }
        )
    }
    }
}

// نافذة إضافة تنبيه سعر جديد: يختار المستخدم العيار ويكتب السعر المستهدف
// فقط، والاتجاه (صعود/هبوط) يُستنتج تلقائياً بمقارنته بالسعر الحالي
@Composable
private fun AddPriceAlertDialog(
    onDismiss: () -> Unit,
    onConfirm: (karat: String, targetPrice: Double, isUpward: Boolean) -> Unit
) {
    var selectedKarat by remember { mutableStateOf("21K") }
    var targetPrice by remember { mutableDoubleStateOf(GoldMarket.prices.first { it.karat == "21K" }.price) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 28.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, Border, RoundedCornerShape(14.dp))
                .background(CardBlack)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { }
                .padding(18.dp)
        ) {
            Text(t("تنبيه سعر جديد", "New Price Alert"), color = White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(14.dp))

            Text(t("العيار", "Karat"), color = Gray, fontSize = 10.sp)
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("24K", "22K", "21K", "18K").forEach { k ->
                    ChoiceButton(
                        text = karatLabel(k),
                        selected = k == selectedKarat,
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                    ) {
                        selectedKarat = k
                        targetPrice = GoldMarket.prices.first { it.karat == k }.price
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            Text(t("السعر المستهدف (ريال للجرام)", "Target Price (SAR per gram)"), color = Gray, fontSize = 10.sp)
            Spacer(Modifier.height(6.dp))
            NumericInputField(
                value = targetPrice,
                onValueChanged = { targetPrice = it },
                fontSize = 16.sp,
                minValue = 0.0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .border(1.dp, Border, RoundedCornerShape(9.dp))
            )

            Spacer(Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .border(1.dp, Border, RoundedCornerShape(9.dp))
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(t("إلغاء", "Cancel"), color = Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(Gold)
                        .clickable {
                            val currentPrice = GoldMarket.prices.first { it.karat == selectedKarat }.price
                            onConfirm(selectedKarat, targetPrice, targetPrice >= currentPrice)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(t("إضافة", "Add"), color = Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==================== الشريط العلوي ====================
@Composable
private fun Header(
    showNotificationBadge: Boolean,
    onNotificationsClick: () -> Unit,
    onAccountClick: () -> Unit
) {
    val shareApp = AppShare.rememberShareTrigger()
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NotificationBell(showBadge = showNotificationBadge, onClick = onNotificationsClick)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    GoldLogo()
                    Text(
                        text = "GOLD VISION",
                        color = Gold,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                CircleButton(Icons.Outlined.Share, onClick = shareApp)
                SmallGoldButton("SAR")
                CircleButton(Icons.Outlined.AccountCircle, onClick = onAccountClick)
            }
        }
    }
}

// جرس الإشعارات يفتح شاشة إعدادات الإشعارات، مع نقطة حمراء تظهر فقط إذا
// لم يُفعِّل المستخدم إشعار السعر اليومي بعد — لا رقم وهمي ثابت
@Composable
private fun NotificationBell(showBadge: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
    ) {
        Icon(
            imageVector = Icons.Outlined.Notifications,
            contentDescription = t("الإشعارات", "Notifications"),
            tint = Gold,
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.Center)
        )
        if (showBadge) {
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 1.dp, y = 0.dp)
                    .clip(RoundedCornerShape(4.5.dp))
                    .background(Red)
            )
        }
    }
}

// شعار "GOLD VISION" الفعلي (صورة حقيقية بخلفية شفافة، composeResources/drawable) —
// نفس الصورة المستخدَمة في أيقونة التطبيق، تظهر في الشريط العلوي وبالتالي
// في كل صفحات التطبيق
@Composable
private fun GoldLogo(size: Dp = 30.dp) {
    Image(
        painter = painterResource(Res.drawable.logo_gold_vision),
        contentDescription = null,
        modifier = Modifier.size(size)
    )
}

@Composable
private fun CircleButton(icon: ImageVector, onClick: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(22.dp))
            .border(1.dp, Gold, RoundedCornerShape(22.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Gold,
            modifier = Modifier.size(19.dp)
        )
    }
}

@Composable
private fun SmallGoldButton(text: String) {
    Box(
        modifier = Modifier
            .height(42.dp)
            .width(92.dp)
            .border(1.dp, Gold, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Gold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

// ==================== شريط الحالة المباشرة ====================
@Composable
private fun LiveStatus(updateText: String) {
    val hasError = GoldMarket.lastError != null
    val statusColor = if (hasError) Red else Green
    val statusLabel = when {
        GoldMarket.isLoading -> t("يحدّث...", "Updating...")
        hasError -> t("غير محدث", "Not updated")
        else -> t("مباشر", "Live")
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = t("أسعار الذهب الآن  ⓘ", "Gold Prices Now  ⓘ"),
            color = White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = "$updateText ↻",
                color = Gray,
                fontSize = 10.sp,
                maxLines = 1
            )
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(statusColor)
            )
            Text(statusLabel, color = if (hasError) Red else White, fontSize = 11.sp)
        }
    }
}

// ==================== بطاقات الأسعار ====================
@Composable
private fun PriceCards(
    selected: String,
    onKaratSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        GoldMarket.prices.forEach { item ->
            val active = selected == item.karat
            val isMostUsed = item.karat == "21K"
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(96.dp)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(78.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .border(
                            width = if (active) 1.5.dp else 1.dp,
                            color = if (active) Gold else Border,
                            shape = RoundedCornerShape(9.dp)
                        )
                        .clickable { onKaratSelected(item.karat) }
                        .padding(7.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            item.karat,
                            color = Gold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            fmt(item.price, 2),
                            color = White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(t("ريال / جرام", "SAR / gram"), color = Gray, fontSize = 10.sp)
                        Text(
                            "▲ ${fmt(item.change, 2)} (${fmt(item.percent, 2)}%)",
                            color = Green,
                            fontSize = 10.sp
                        )
                    }
                }

                if (isMostUsed) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Gold)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            t("الأكثر استخداماً", "Most used"),
                            color = Black,
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

// ==================== حاسبة الذهب (قابلة لإعادة الاستخدام) ====================
@Composable
private fun GoldCalculator(
    selectedKarat: String,
    buyMode: Boolean,
    weight: Double,
    manufacturing: Double,
    beforeVat: Double,
    vat: Double,
    total: Double,
    onBuyModeChanged: (Boolean) -> Unit,
    onKaratChanged: (String) -> Unit,
    onWeightChanged: (Double) -> Unit,
    onManufacturingChanged: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    AppCard(title = t("حاسبة الذهب", "Gold Calculator"), modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(23.dp)
                .border(1.dp, Border, RoundedCornerShape(5.dp))
        ) {
            CalculatorMode(
                text = t("شراء", "Buy"),
                selected = buyMode,
                modifier = Modifier.weight(1f)
            ) { onBuyModeChanged(true) }

            CalculatorMode(
                text = t("بيع", "Sell"),
                selected = !buyMode,
                modifier = Modifier.weight(1f)
            ) { onBuyModeChanged(false) }
        }

        Spacer(Modifier.height(3.dp))
        Text(t("اختر العيار", "Choose karat"), color = White, fontSize = 10.sp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("24K", "22K", "21K", "18K").forEach { k ->
                ChoiceButton(
                    text = k,
                    selected = k == selectedKarat,
                    modifier = Modifier.weight(1f)
                ) { onKaratChanged(k) }
            }
        }

        Text(t("الوزن (جرام)", "Weight (grams)"), color = White, fontSize = 10.sp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(23.dp)
                .border(0.8.dp, Border, RoundedCornerShape(5.dp)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SmallActionButton("−") {
                onWeightChanged((weight - 1).coerceAtLeast(0.1))
            }
            NumericInputField(
                value = weight,
                onValueChanged = onWeightChanged,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            SmallActionButton("+") {
                onWeightChanged(weight + 1)
            }
        }

        Spacer(Modifier.height(3.dp))
        CalculatorRow(t("سعر الجرام", "Price per gram"), "${fmt(selectedPrice(selectedKarat), 2)} ${t("ريال", "SAR")}")
        CalculatorRow(
            t("السعر قبل الضريبة ⓘ", "Price before tax ⓘ"),
            "${fmt(beforeVat, 2, grouped = true)} ${t("ريال", "SAR")}",
            valueColor = Gold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(t("المصنعية (للجرام) ✎", "Workmanship (per gram) ✎"), color = White, fontSize = 10.sp)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(t("ريال", "SAR"), color = Gray, fontSize = 9.sp)
                NumericInputField(
                    value = manufacturing,
                    onValueChanged = { onManufacturingChanged(it.coerceAtMost(500.0)) },
                    fontSize = 10.sp,
                    minValue = 0.0,
                    modifier = Modifier
                        .width(46.dp)
                        .height(16.dp)
                )
            }
        }

        CalculatorRow(
            t("الضريبة (15%)", "Tax (15%)"),
            "${fmt(vat, 2, grouped = true)} ${t("ريال", "SAR")}"
        )

        Spacer(Modifier.height(2.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Border)
        )
        Spacer(Modifier.height(3.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(t("الإجمالي", "Total"), color = Gold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(
                "${t("ريال", "SAR")} ${fmt(total, 2, grouped = true)}",
                color = Gold,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun selectedPrice(karat: String): Double =
    GoldMarket.prices.first { it.karat == karat }.price

@Composable
private fun CalculatorMode(
    text: String,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(7.dp))
            .background(if (selected) Gold else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = if (selected) Black else White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ChoiceButton(
    text: String,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(34.dp)
            .clip(RoundedCornerShape(7.dp))
            .border(
                1.dp,
                if (selected) Gold else Border,
                RoundedCornerShape(7.dp)
            )
            .background(if (selected) Gold else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = if (selected) Black else White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// ==================== الدولة ونسبة الضريبة (حاسبة الذهب / المحل أعطاك سعراً) ====================
private data class CountryTaxOption(val flag: String, val name: String, val vatPercent: Double)

private val countryTaxOptions = listOf(
    CountryTaxOption("🇸🇦", "السعودية", 15.0),
    CountryTaxOption("🇦🇪", "الإمارات", 5.0),
    CountryTaxOption("🇧🇭", "البحرين", 10.0),
    CountryTaxOption("🇴🇲", "عُمان", 5.0),
    CountryTaxOption("🇶🇦", "قطر", 0.0),
    CountryTaxOption("🇰🇼", "الكويت", 0.0),
    CountryTaxOption("🇪🇬", "مصر", 14.0),
    CountryTaxOption("🌍", "دولة أخرى", 0.0)
)

// اسم country.name يبقى بالعربي كمعرّف ثابت للخيار (مقارنات/تخزين)؛
// هذه الدالة فقط تُترجم الاسم المعروض في الواجهة
private fun countryDisplayName(country: CountryTaxOption): String = when (country.name) {
    "السعودية" -> t("السعودية", "Saudi Arabia")
    "الإمارات" -> t("الإمارات", "UAE")
    "البحرين" -> t("البحرين", "Bahrain")
    "عُمان" -> t("عُمان", "Oman")
    "قطر" -> t("قطر", "Qatar")
    "الكويت" -> t("الكويت", "Kuwait")
    "مصر" -> t("مصر", "Egypt")
    else -> t("دولة أخرى", "Other country")
}

// صندوق اختيار الدولة (بعلمها) مع نسبة الضريبة — تُملأ تلقائياً حسب
// الدولة المختارة، ويمكن تعديلها يدوياً بعد ذلك بشكل مستقل. عند إعفاء
// عيار 24 من الضريبة (isTaxExempt) يظهر ذلك بدل حقل النسبة
@Composable
private fun CountryTaxSelector(
    selectedCountry: CountryTaxOption,
    onCountrySelected: (CountryTaxOption) -> Unit,
    taxPercent: Double,
    onTaxPercentChanged: (Double) -> Unit,
    isTaxExempt: Boolean
) {
    var showPicker by remember { mutableStateOf(false) }

    Text(t("الدولة ونسبة الضريبة", "Country & tax rate"), color = Gray, fontSize = 10.sp)
    Spacer(Modifier.height(6.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier
                .weight(1.4f)
                .height(42.dp)
                .clip(RoundedCornerShape(9.dp))
                .border(1.dp, Border, RoundedCornerShape(9.dp))
                .clickable { showPicker = true }
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(selectedCountry.flag, fontSize = 15.sp)
                Text(countryDisplayName(selectedCountry), color = White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Text("˅", color = Gold, fontSize = 11.sp)
        }

        if (isTaxExempt) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .border(1.dp, Border, RoundedCornerShape(9.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(t("معفى", "Exempt"), color = Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .border(1.dp, Border, RoundedCornerShape(9.dp)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NumericInputField(
                    value = taxPercent,
                    onValueChanged = onTaxPercentChanged,
                    fontSize = 12.sp,
                    minValue = 0.0,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
                Text("%", color = Gray, fontSize = 11.sp, modifier = Modifier.padding(end = 10.dp))
            }
        }
    }

    if (showPicker) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { showPicker = false },
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 28.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, Border, RoundedCornerShape(14.dp))
                    .background(CardBlack)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { }
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    t("اختر الدولة", "Choose country"),
                    color = White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    textAlign = TextAlign.End
                )
                countryTaxOptions.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onCountrySelected(option)
                                onTaxPercentChanged(option.vatPercent)
                                showPicker = false
                            }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(option.flag, fontSize = 15.sp)
                        Text(countryDisplayName(option), color = White, fontSize = 12.sp, modifier = Modifier.weight(1f))
                        Text(
                            if (option.vatPercent == 0.0) t("بدون ضريبة", "No tax") else "${fmt(option.vatPercent, 0)}%",
                            color = Gray,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

// صندوق "حفظ في المحفظة" في شاشة الحاسبة، يظهر قبل زر "المحل أعطاك
// سعراً؟". يفتح شاشة "إضافة قطعة" نفسها معبّأة بنتيجة الحساب الحالي
// (عيار/وزن/إجمالي)، حتى يستفيد المستخدم من خيارات التعديل والتاريخ
// والاسم الموجودة أصلاً هناك بدل قائمة حفظ منفصلة
@Composable
private fun SaveToPortfolioBox(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, Border, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.AccountBalanceWallet,
                contentDescription = null,
                tint = Gray,
                modifier = Modifier.size(15.dp)
            )
            Text(t("حفظ في المحفظة", "Save to Portfolio"), color = White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SmallActionButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(42.dp)
            .fillMaxSize()
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Gold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

// حقل رقمي عام قابل للكتابة المباشرة من الكيبورد (مستخدم للوزن والمصنعية)
@Composable
private fun NumericInputField(
    value: Double,
    onValueChanged: (Double) -> Unit,
    modifier: Modifier = Modifier,
    fontSize: androidx.compose.ui.unit.TextUnit = 12.sp,
    minValue: Double = 0.1
) {
    var fieldValue by remember { mutableStateOf(TextFieldValue(fmt(value, 2))) }

    LaunchedEffect(value) {
        val parsed = fieldValue.text.toDoubleOrNull()
        if (parsed == null || kotlin.math.abs(parsed - value) > 0.001) {
            val newText = fmt(value, 2)
            fieldValue = TextFieldValue(newText, selection = TextRange(newText.length))
        }
    }

    BasicTextField(
        value = fieldValue,
        onValueChange = { new ->
            if (new.text.isEmpty() || new.text.matches(Regex("^\\d*\\.?\\d*$"))) {
                fieldValue = new
                // يُبلَّغ بأي رقم صالح فوراً أثناء الكتابة، حتى لو كان أقل من
                // minValue (مثل 0) — حتى يبقى المجموع المعروض مطابقاً دائماً
                // لما يكتبه المستخدم فعلياً، بدل حساب صامت بقيمة قديمة مخفية.
                // الحد الأدنى يُفرض فقط عند مغادرة الحقل (onFocusChanged أدناه)
                new.text.toDoubleOrNull()?.let { parsedValue ->
                    onValueChanged(parsedValue)
                }
            }
        },
        singleLine = true,
        textStyle = TextStyle(
            color = White,
            fontSize = fontSize,
            textAlign = TextAlign.Center
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        cursorBrush = SolidColor(Gold),
        modifier = modifier.onFocusChanged { focusState ->
            // إذا ترك المستخدم الحقل فارغاً أو برقم أقل من الحد الأدنى عند
            // الخروج منه، يُصحَّح تلقائياً للحد الأدنى — في العرض وفي القيمة
            // الفعلية المستخدَمة بالحساب معاً، حتى لا يختلفا
            if (!focusState.isFocused) {
                val parsed = fieldValue.text.toDoubleOrNull()
                if (parsed == null || parsed < minValue) {
                    onValueChanged(minValue)
                    val newText = fmt(minValue, 2)
                    fieldValue = TextFieldValue(newText, selection = TextRange(newText.length))
                }
            }
        }
    ) { innerTextField ->
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            innerTextField()
        }
    }
}

// حقل نصي عام: الضغط بأي مكان بالحقل يفتح الكيبورد (حتى لو الحقل أعلى من
// النص)، وأي تحديد (نقر مزدوج على كلمة أو سحب) يتوسّع تلقائياً ليشمل
// النص كامل — يُستخدم لكل حقول النص الحر بالتطبيق (الاسم، الإيميل،
// كلمة المرور، الملاحظات، إلخ)
@Composable
private fun SelectableTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    fontSize: androidx.compose.ui.unit.TextUnit = 12.sp,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    val focusRequester = remember { FocusRequester() }
    var fieldValue by remember { mutableStateOf(TextFieldValue(value)) }

    LaunchedEffect(value) {
        if (fieldValue.text != value) {
            fieldValue = TextFieldValue(value, selection = TextRange(value.length))
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(9.dp))
            .border(1.dp, Border, RoundedCornerShape(9.dp))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { focusRequester.requestFocus() }
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (value.isEmpty()) {
            Text(placeholder, color = Gray, fontSize = fontSize)
        }
        BasicTextField(
            value = fieldValue,
            onValueChange = { new ->
                fieldValue = new
                if (new.text != value) onValueChange(new.text)
            },
            singleLine = true,
            textStyle = TextStyle(color = White, fontSize = fontSize, textDirection = TextDirection.Content),
            cursorBrush = SolidColor(Gold),
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
        )
    }
}

@Composable
private fun CalculatorRow(
    label: String,
    value: String,
    valueColor: Color = White
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = White, fontSize = 10.sp)
        Text(value, color = valueColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

// ==================== الرسم البياني ====================
@Composable
private fun PriceChart(
    selectedPeriod: String,
    onPeriodSelected: (String) -> Unit,
    selectedKarat: String,
    onChartClick: () -> Unit = {}
) {
    val basePrice = selectedPrice(selectedKarat)
    val karatPercent = GoldMarket.prices.first { it.karat == selectedKarat }.percent
    val minPrice = basePrice * 0.94
    val maxPrice = basePrice * 1.06
    var showAnalysis by remember { mutableStateOf(false) }

    AppCard(
        title = t("تتبع أسعار الذهب", "Gold Price Tracking"),
        titleIcon = Icons.AutoMirrored.Outlined.ShowChart,
        modifier = Modifier.fillMaxSize()
    ) {
        // مفتاح التبديل بين تتبع الأسعار (رسم بسيط) والتحليل الفني
        // (شموع يابانية) — نفس مفتاح شاشة الرسم البياني الكاملة بالضبط
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, Border, RoundedCornerShape(8.dp))
                .padding(2.dp)
        ) {
            listOf(false to t("تتبع الأسعار", "Price Tracking"), true to t("التحليل الفني", "Technical Analysis")).forEach { (analysisMode, label) ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (showAnalysis == analysisMode) Gold else Color.Transparent)
                        .clickable { showAnalysis = analysisMode }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        label,
                        color = if (showAnalysis == analysisMode) Black else Gray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        val periods = chartPeriods

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            periods.forEach { period ->
                Box(
                    modifier = Modifier
                        .height(31.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(if (selectedPeriod == period) Gold else Color.Transparent)
                        .border(1.dp, Border, RoundedCornerShape(5.dp))
                        .clickable { onPeriodSelected(period) }
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        periodLabel(period),
                        color = if (selectedPeriod == period) Black else White,
                        fontSize = 10.sp,
                        fontWeight = if (selectedPeriod == period) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(Modifier.height(5.dp))
        Text(
            "▲ ${fmt(karatPercent, 2)}%",
            color = Green,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                // بوضع التحليل الفني، رسم الشموع يتعامل مع اللمس بنفسه
                // (تكبير/سحب/تلميح) — تفعيل النقر للتنقل هنا كان يتعارض
                // مع تلك الإيماءات، فنعطّله في هذا الوضع تحديداً فقط
                .then(if (!showAnalysis) Modifier.clickable { onChartClick() } else Modifier)
        ) {
            KaratChartCanvas(
                modifier = Modifier.fillMaxSize(),
                basePrice = basePrice,
                seed = karatChartSeeds[selectedKarat] ?: 1,
                period = selectedPeriod,
                realPoints = realChartPointsFor(selectedKarat, selectedPeriod),
                realBars = if (showAnalysis) realBarsFor(selectedPeriod) else null,
                karat = selectedKarat
            )

            // تُخفى بوضع التحليل الفني (الشموع) لأنها تتشارك نفس الركن
            // (أعلى يمين بالـ RTL) مع زر "إعادة ضبط" التكبير داخل رسم
            // الشموع، والشموع أصلاً توفّر أعلى/أدنى سعر بتفصيل أدق عبر
            // بطاقة التلميح عند اللمس
            if (!showAnalysis) {
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 2.dp, end = 2.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(t("أعلى سعر", "High"), color = Gray, fontSize = 8.5.sp)
                    Text(
                        fmt(maxPrice, 2),
                        color = White,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(t("ريال", "SAR"), color = Gray, fontSize = 8.sp)

                    Spacer(Modifier.height(6.dp))

                    Text(t("أدنى سعر", "Low"), color = Gray, fontSize = 8.5.sp)
                    Text(
                        fmt(minPrice, 2),
                        color = White,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(t("ريال", "SAR"), color = Gray, fontSize = 8.sp)
                }
            }
        }
    }
}

// ==================== أهم الأخبار ====================
@Composable
private fun ImportantNews() {
    val topThree = GoldNews.articles.take(3)
    AppCard(title = t("أهم الأخبار المؤثرة", "Top News"), modifier = Modifier.fillMaxSize()) {
        if (topThree.isEmpty()) {
            Text(
                if (GoldNews.isLoading) t("جارٍ تحميل الأخبار...", "Loading news...") else t("لا توجد أخبار متوفرة حالياً", "No news available right now"),
                color = Gray,
                fontSize = 10.sp
            )
        } else {
            topThree.forEach { article ->
                NewsRow(dot = Gold, text = article.title, time = article.publishedAt)
            }
        }
        Spacer(Modifier.weight(1f))
        Text(
            t("عرض المزيد", "View more"),
            color = White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun NewsRow(dot: Color, text: String, time: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 3.dp)
                .size(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(dot)
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text(
                text,
                color = White,
                fontSize = 11.sp,
                lineHeight = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(time, color = Gray, fontSize = 9.5.sp)
        }
    }
}

// ==================== جدول الفيدرالي ====================
@Composable
private fun FedSchedule(rows: List<FedMeetingRow>) {
    AppCard(
        title = t("مواعيد اجتماعات الفيدرالي", "Fed Meeting Dates"),
        titleIcon = Icons.Outlined.CalendarMonth,
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = t("اليوم", "Day"),
                color = Gray,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(0.8f),
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            Text(
                text = t("التاريخ", "Date"),
                color = Gray,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            Text(
                text = t("الوقت", "Time"),
                color = Gray,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(0.9f),
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            Text(
                text = t("العدّ التنازلي", "Countdown"),
                color = Gray,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .height(1.dp)
                .background(Border)
        )

        rows.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = row.day,
                    color = White,
                    fontSize = 9.sp,
                    modifier = Modifier.weight(0.8f),
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )

                Text(
                    text = row.date,
                    color = White,
                    fontSize = 9.sp,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )

                Text(
                    text = timeDisplayLabel(row.time),
                    color = White,
                    fontSize = 9.sp,
                    modifier = Modifier.weight(0.9f),
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background((if (row.daysLeft <= 3) Red else Gold).copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (row.daysLeft == 0) t("اليوم", "Today") else t("بعد ${row.daysLeft} يوم", "In ${row.daysLeft}d"),
                            color = if (row.daysLeft <= 3) Red else Gold,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = t("عرض الجدول الكامل", "View full schedule"),
            color = White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 2.dp),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

// ==================== شاشة مواعيد الفيدرالي الكاملة ====================
@Composable
private fun FedMeetingsScreen(onBack: () -> Unit) {
    val rows = remember { upcomingFedMeetings() }
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = t("رجوع", "Back"),
                tint = Gold,
                modifier = Modifier
                    .size(20.dp)
                    .clickable { onBack() }
            )
            Text(
                t("مواعيد اجتماعات الفيدرالي", "Fed Meeting Dates"),
                color = Gold,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, Border, RoundedCornerShape(10.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = Gold,
                modifier = Modifier
                    .size(15.dp)
                    .padding(top = 1.dp)
            )
            Text(
                t(
                    "قرار الفائدة الأمريكية يُعلن عادة الساعة 9:00 مساءً بتوقيت مكة المكرمة " +
                        "(2:00 ظهراً بتوقيت واشنطن)",
                    "The US interest rate decision is usually announced at 9:00 PM Makkah time " +
                        "(2:00 PM Washington time)"
                ),
                color = Gray,
                fontSize = 10.sp,
                lineHeight = 15.sp
            )
        }

        Spacer(Modifier.height(8.dp))

        if (rows.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(t("لا توجد اجتماعات مجدولة قريباً", "No meetings scheduled soon"), color = Gray, fontSize = 12.sp)
            }
            return
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(rows) { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(9.dp))
                        .border(1.dp, Border, RoundedCornerShape(9.dp))
                        .background(CardBlack)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "${row.day} ${row.date}",
                            color = White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            t("الساعة ${row.time} بتوقيت مكة المكرمة", "${timeDisplayLabel(row.time)} Makkah time"),
                            color = Gray,
                            fontSize = 9.5.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background((if (row.daysLeft <= 3) Red else Gold).copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = if (row.daysLeft == 0) t("اليوم", "Today") else t("بعد ${row.daysLeft} يوم", "In ${row.daysLeft}d"),
                            color = if (row.daysLeft <= 3) Red else Gold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

// ==================== شريط ملخص المحفظة ====================
@Composable
private fun PortfolioSummary(totalValue: Double, itemCount: Int, totalWeight: Double, totalCost: Double) {
    val profitLoss = totalValue - totalCost
    val profitLossPercent = if (totalCost > 0) (profitLoss / totalCost) * 100.0 else 0.0
    val profitLossText = if (totalCost > 0) {
        "${if (profitLoss >= 0) "+" else ""}${fmt(profitLoss, 2, grouped = true)} ${t("ريال", "SAR")} (${fmt(profitLossPercent, 2)}%)"
    } else ""

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(55.dp)
            .border(1.dp, Border, RoundedCornerShape(9.dp))
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        SummaryItem(
            title = t("قيمة المحفظة", "Portfolio Value"),
            value = "${fmt(totalValue, 2, grouped = true)} ${t("ريال", "SAR")}",
            extra = profitLossText,
            extraColor = if (profitLoss >= 0) Green else Red,
            valueColor = Gold
        )
        DividerVertical()
        SummaryItem(
            title = t("عدد المنتجات", "Item Count"),
            value = t("$itemCount منتجات", "$itemCount items"),
            extra = "",
            valueColor = White
        )
        DividerVertical()
        SummaryItem(
            title = t("إجمالي الوزن", "Total Weight"),
            value = "${fmt(totalWeight, 2)} ${t("جرام", "g")}",
            extra = "",
            valueColor = White
        )
        Box(
            modifier = Modifier
                .height(38.dp)
                .clip(RoundedCornerShape(7.dp))
                .border(1.dp, Gold, RoundedCornerShape(7.dp))
                .padding(horizontal = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(t("عرض المحفظة", "View Portfolio"), color = Gold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Icon(
                    imageVector = Icons.Outlined.AccountBalanceWallet,
                    contentDescription = null,
                    tint = Gold,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}

@Composable
private fun SummaryItem(
    title: String,
    value: String,
    extra: String,
    valueColor: Color,
    extraColor: Color = Green
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 5.dp)
    ) {
        Text(title, color = Gold, fontSize = 10.sp)
        Text(value, color = valueColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        if (extra.isNotEmpty()) {
            Text(extra, color = extraColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun DividerVertical() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(44.dp)
            .background(GoldDark)
    )
}

// ==================== شريط التنقل السفلي ====================
@Composable
private fun BottomNav(
    selected: Int,
    onSelected: (Int) -> Unit
) {
    val tabs: List<Pair<String, ImageVector>> = listOf(
        t("الرئيسية", "Home") to Icons.Outlined.Home,
        t("حاسبة الذهب", "Calculator") to Icons.Outlined.Calculate,
        t("الأخبار", "News") to Icons.AutoMirrored.Outlined.Article,
        t("المحفظة", "Portfolio") to Icons.Outlined.AccountBalanceWallet,
        t("الزكاة", "Zakat") to Icons.Outlined.Balance,
        t("المزيد", "More") to Icons.Outlined.MoreHoriz
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        tabs.forEachIndexed { index, tab ->
            val active = index == selected
            Column(
                modifier = Modifier
                    .width(65.dp)
                    .height(60.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (active) Color(0xFF211900) else Color.Transparent)
                    .clickable { onSelected(index) }
                    .padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = tab.second,
                    contentDescription = tab.first,
                    tint = if (active) Gold else Gray,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    tab.first,
                    color = if (active) Gold else White,
                    fontSize = 11.sp,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1
                )
            }
        }
    }
}

// ==================== بطاقة عامة قابلة لإعادة الاستخدام ====================
@Composable
private fun AppCard(
    title: String,
    modifier: Modifier = Modifier,
    titleIcon: ImageVector? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(9.dp))
            .border(1.dp, Border, RoundedCornerShape(9.dp))
            .background(CardBlack)
            .padding(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    title,
                    color = White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                if (titleIcon != null) {
                    Icon(
                        imageVector = titleIcon,
                        contentDescription = null,
                        tint = Gold,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Green)
            )
        }

        Spacer(Modifier.height(5.dp))
        content()
    }
}
