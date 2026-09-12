package com.goldvision

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Balance
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

private val zakatItems = listOf(
    ZakatItem("خاتم", "💍", "21K", 5.00, "10 / 01 / 2023"),
    ZakatItem("سلسلة", "📿", "21K", 15.30, "10 / 01 / 2023"),
    ZakatItem("سوار", "⭕", "22K", 20.00, "05 / 08 / 2025"),
    ZakatItem("سبيكة", "🟨", "24K", 26.00, "20 / 08 / 2025")
)

// قطعة ذهب أضافها المستخدم بنفسه عبر شاشة "إضافة قطعة" — تظهر في المحفظة
// وفي الزكاة معاً (نفس الصنف بنفس البيانات)، بسعر يُحسب حياً من GoldMarket
@Serializable
private data class GoldItem(
    val name: String,
    val emoji: String,
    val karat: String,
    val weightGrams: Double,
    val purchasePriceWithTax: Double,
    val manufacturingPerGram: Double,
    val purchaseDate: String,
    val notes: String
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
            ZakatStatus("وجب عليه الزكاة", Green, "منذ ${(daysElapsed / 30).coerceAtLeast(1)} شهراً تقريباً")
        hawlCompleted ->
            ZakatStatus("وقت الزكاة", Green, "حال عليه الحول")
        else ->
            ZakatStatus("متبقي ${HAWL_DAYS - daysElapsed} يوماً", Gray, "لم يكتمل الحول بعد")
    }
}

// ==================== مواعيد الفيدرالي ====================
private data class FedMeetingRaw(val year: Int, val month: Int, val day: Int, val time: String)

private val fedMeetingsRaw = listOf(
    FedMeetingRaw(2026, 9, 16, "08:00 ص"),
    FedMeetingRaw(2026, 10, 28, "08:00 ص"),
    FedMeetingRaw(2026, 12, 9, "08:00 ص"),
    FedMeetingRaw(2027, 1, 27, "08:00 ص"),
    FedMeetingRaw(2027, 3, 17, "08:00 ص")
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

private fun todayLocalDate(): LocalDate =
    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

private fun upcomingFedMeetings(): List<Triple<String, String, String>> {
    val today = todayLocalDate()
    return fedMeetingsRaw
        .map { it to LocalDate(it.year, it.month, it.day) }
        .filter { (_, date) -> date >= today }
        .sortedBy { (_, date) -> date }
        .map { (raw, date) ->
            Triple(
                arabicDayNames[date.dayOfWeek] ?: "",
                "${raw.year}/${raw.month}/${raw.day}",
                raw.time
            )
        }
}

private fun currentDateTimeText(): String {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val hour24 = now.hour
    val period = if (hour24 < 12) "ص" else "م"
    val hour12 = when {
        hour24 == 0 -> 12
        hour24 > 12 -> hour24 - 12
        else -> hour24
    }
    val minute = now.minute.toString().padStart(2, '0')
    return "آخر تحديث: ${now.year}/${now.monthNumber}/${now.dayOfMonth} الساعة $hour12:$minute $period"
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
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                GoldVisionApp()
            }
        }
    }
}

// ==================== الشاشة الرئيسية للتطبيق (تحتوي على نظام التنقل) ====================
@Composable
private fun GoldVisionApp() {
    var selectedKarat by remember { mutableStateOf("21K") }
    var selectedPeriod by remember { mutableStateOf("أسبوع") }
    var buyMode by remember { mutableStateOf(true) }
    var weight by remember { mutableDoubleStateOf(10.0) }
    var manufacturing by remember { mutableDoubleStateOf(35.0) }
    var selectedBottom by remember { mutableIntStateOf(0) }
    var showChartFull by remember { mutableStateOf(false) }
    var showDealEvaluator by remember { mutableStateOf(false) }
    var showAddGoldItem by remember { mutableStateOf(false) }
    var editingGoldItemIndex by remember { mutableStateOf<Int?>(null) }
    val savedDeals = remember { mutableStateListOf<SavedDeal>() }
    val savedGoldItems = remember { mutableStateListOf<GoldItem>().apply { addAll(loadSavedGoldItems()) } }

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
    val marketScope = rememberCoroutineScope()

    val fedRows = remember { upcomingFedMeetings().take(4) }

    val selectedPrice = GoldMarket.prices.first { it.karat == selectedKarat }.price
    val beforeVat = selectedPrice * weight
    val vat = (beforeVat + manufacturing * weight) * 0.15
    val total = beforeVat + manufacturing * weight + vat

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .statusBarsPadding()
    ) {
        Header(onRefresh = { marketScope.launch { GoldMarket.refresh() } })

        Box(modifier = Modifier.weight(1f)) {
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
                    onBack = {
                        showAddGoldItem = false
                        editingGoldItemIndex = null
                    },
                    onSave = { item ->
                        if (editingIndex != null && editingIndex in savedGoldItems.indices) {
                            savedGoldItems[editingIndex] = item
                        } else {
                            savedGoldItems.add(0, item)
                        }
                        persistGoldItems(savedGoldItems)
                        showAddGoldItem = false
                        editingGoldItemIndex = null
                    },
                    onDelete = if (editingIndex != null) {
                        {
                            if (editingIndex in savedGoldItems.indices) {
                                savedGoldItems.removeAt(editingIndex)
                            }
                            persistGoldItems(savedGoldItems)
                            showAddGoldItem = false
                            editingGoldItemIndex = null
                        }
                    } else null
                )
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
                        onNavigatePortfolio = { selectedBottom = 3 }
                    )
                    1 -> CalculatorFullScreen(
                        selectedKarat = selectedKarat,
                        buyMode = buyMode,
                        weight = weight,
                        manufacturing = manufacturing,
                        beforeVat = beforeVat,
                        vat = vat,
                        total = total,
                        onBuyModeChanged = { buyMode = it },
                        onKaratChanged = { selectedKarat = it },
                        onWeightChanged = { weight = it },
                        onManufacturingChanged = { manufacturing = it },
                        onNavigateDealEvaluator = { showDealEvaluator = true },
                        savedDeals = savedDeals,
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
                    5 -> MoreScreen(onBack = { selectedBottom = 0 })
                }
            }
        }

        BottomNav(
            selected = selectedBottom,
            onSelected = { index ->
                showChartFull = false
                showDealEvaluator = false
                showAddGoldItem = false
                editingGoldItemIndex = null
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
    fedRows: List<Triple<String, String, String>>,
    savedGoldItems: List<GoldItem>,
    onNavigateCalculator: () -> Unit,
    onNavigateChart: () -> Unit,
    onNavigateNews: () -> Unit,
    onNavigatePortfolio: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
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
                    .clickable { onNavigateChart() }
            ) {
                PriceChart(
                    selectedPeriod = selectedPeriod,
                    onPeriodSelected = onPeriodSelected,
                    selectedKarat = selectedKarat
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
                totalWeight = homeTotals.totalWeight
            )
        }

        Spacer(Modifier.height(6.dp))
    }
}

// ==================== شاشة حاسبة الذهب الكاملة ====================
private fun karatLabel(karat: String): String = karat.removeSuffix("K") + " عيار"

@Composable
private fun CalculatorFullScreen(
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
    onNavigateDealEvaluator: () -> Unit,
    savedDeals: List<SavedDeal>,
    onBack: () -> Unit
) {
    val selectedPrice = GoldMarket.prices.first { it.karat == selectedKarat }
    val finalGramPrice = if (weight > 0) total / weight else 0.0
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
                contentDescription = "تحديث",
                tint = Gold,
                modifier = Modifier
                    .size(22.dp)
                    .align(Alignment.CenterStart)
                    .clickable { marketScope.launch { GoldMarket.refresh() } }
            )
            Text(
                "الحاسبة",
                color = White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "رجوع",
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
                text = "بيع",
                selected = !buyMode,
                modifier = Modifier.weight(1f)
            ) { onBuyModeChanged(false) }

            CalculatorMode(
                text = "شراء",
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
                Text("تحديث منذ دقائق", color = Gray, fontSize = 9.sp)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("سعر جرام الذهب", color = Gray, fontSize = 10.sp)
                Text(
                    fmt(selectedPrice.price, 2),
                    color = Gold,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = Gray,
                modifier = Modifier.size(16.dp)
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
                "الوزن (جرام)",
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

        if (manufacturing <= 0.0) {
            Spacer(Modifier.height(6.dp))
            Text(
                "ذهب خالص - بدون مصنعية أو ضريبة",
                color = Gray,
                fontSize = 10.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
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
                    Text("مباشر", color = White, fontSize = 11.sp)
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF123321))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("صفقة ممتازة", color = Green, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(10.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("الإجمالي (شامل الضريبة)", color = Gray, fontSize = 11.sp)
                Text(
                    "${fmt(total, 2, grouped = true)} ريال",
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
                    Text("معفى من الضريبة", color = Gray, fontSize = 9.sp)
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

            CalculatorRow("سعر الذهب", "${fmt(beforeVat, 2, grouped = true)} ريال")
            CalculatorRow("المصنعية", "${fmt(manufacturing * weight, 2, grouped = true)} ريال")
            CalculatorRow("ضريبة القيمة المضافة (15%)", "${fmt(vat, 2, grouped = true)} ريال")
            CalculatorRow("سعر الجرام النهائي", "${fmt(finalGramPrice, 2, grouped = true)} ريال")
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
                Text("المحل أعطاك سعراً؟", color = Gold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
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
            Text("الأسعار المحفوظة", color = Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                            "${fmt(deal.totalPrice, 2, grouped = true)} ريال",
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

    val karatPrice = GoldMarket.prices.first { it.karat == karat }.price
    val fairBeforeVat = karatPrice * weight
    val fairManufacturing = manufacturing * weight
    val fairSubtotal = fairBeforeVat + fairManufacturing
    val fairVat = fairSubtotal * 0.15
    val fairTotal = fairSubtotal + fairVat

    val shopPriceWithTax = if (includingTax) shopPrice else shopPrice * 1.15
    val savings = fairTotal - shopPriceWithTax
    val ratio = if (fairTotal > 0) (shopPriceWithTax / fairTotal).toFloat() else 1f

    val (tierLabel, tierColor) = when {
        ratio <= 1.0f -> "صفقة ممتازة" to Green
        ratio <= 1.05f -> "سعر عادل" to Yellow
        else -> "سعر مرتفع" to Red
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
                    contentDescription = "رجوع",
                    tint = Gold,
                    modifier = Modifier
                        .size(22.dp)
                        .clickable { onBack() }
                )
                Text(
                    "المحل أعطاك سعراً؟",
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

            Text("العيار", color = Gray, fontSize = 10.sp)
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .border(1.dp, Border, RoundedCornerShape(9.dp))
                    .clickable {
                        val order = listOf("24K", "22K", "21K", "18K")
                        karat = order[(order.indexOf(karat) + 1) % order.size]
                    }
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(karatLabel(karat), color = White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("˅", color = Gold, fontSize = 13.sp)
            }

            Spacer(Modifier.height(12.dp))

            Text("الوزن (جرام)", color = Gray, fontSize = 10.sp)
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

            Column(modifier = Modifier.clickable { showMore = !showMore }) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("السعودية", color = White, fontSize = 12.sp)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text("إظهار المزيد", color = Gray, fontSize = 10.sp)
                        Text(if (showMore) "^" else "˅", color = Gray, fontSize = 10.sp)
                    }
                }
                Text("بلد صنع الحلية", color = Gray, fontSize = 9.sp)
            }

            if (showMore) {
                Spacer(Modifier.height(6.dp))
                Text(
                    "بيانات إضافية عن الحلية ستظهر هنا قريباً",
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

            Text("عرض المحل (ريال)", color = Gray, fontSize = 10.sp)
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
                Text("شامل الضريبة؟", color = White, fontSize = 12.sp)
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

                Text("السعر العادل (شامل الضريبة)", color = Gray, fontSize = 11.sp)
                Text(
                    "${fmt(fairTotal, 2, grouped = true)} ريال",
                    color = Gold,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(4.dp))
                if (savings >= 0) {
                    Text("تدفع أقل من العادل", color = Green, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "وفرت ${fmt(savings, 2, grouped = true)} ريال",
                        color = Green,
                        fontSize = 10.sp
                    )
                } else {
                    Text("تدفع أكثر من العادل", color = Red, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "زيادة ${fmt(-savings, 2, grouped = true)} ريال",
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

                CalculatorRow("الإجمالي (بدون ضريبة)", "${fmt(fairSubtotal, 2, grouped = true)} ريال")
                CalculatorRow("الإجمالي (شامل الضريبة)", "${fmt(fairTotal, 2, grouped = true)} ريال")
                CalculatorRow("سعر الذهب", "${fmt(fairBeforeVat, 2, grouped = true)} ريال")
                CalculatorRow("المصنعية", "${fmt(fairManufacturing, 2, grouped = true)} ريال")
                val shopMargin = shopPriceWithTax - fairTotal
                val shopMarginPerGram = if (weight > 0) shopMargin / weight else 0.0
                CalculatorRow(
                    "ريع المحل",
                    "${fmt(shopMargin, 2, grouped = true)} ريال (${fmt(shopMarginPerGram, 2)} /جم)"
                )
                CalculatorRow("ضريبة القيمة المضافة (15%)", "${fmt(fairVat, 2, grouped = true)} ريال")
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
                Text("أسعار للتفاوض (شامل الضريبة)", color = White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))

                NegotiationRow("صفقة ممتازة", fairTotal, shopPriceWithTax, Green)
                NegotiationRow("سعر عادل", fairTotal * 1.05, shopPriceWithTax, Yellow)
                NegotiationRow("الحد الأقصى", fairTotal * 1.10, shopPriceWithTax, Red)
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
                Text("حفظ في المجموعة", color = Black, fontSize = 13.sp, fontWeight = FontWeight.Bold)
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
                Text("حفظ السعر", color = White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "إغلاق",
                    tint = Gray,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { onDismiss() }
                )
            }

            Spacer(Modifier.height(6.dp))
            Text(
                "${fmt(totalPrice, 2, grouped = true)} ريال • $tierLabel",
                color = tierColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(14.dp))
            Text("اسم المحل", color = Gray, fontSize = 10.sp)
            Spacer(Modifier.height(6.dp))

            val shopNameFocus = remember { FocusRequester() }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .border(1.dp, Border, RoundedCornerShape(9.dp))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { shopNameFocus.requestFocus() }
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (shopName.isEmpty()) {
                    Text("مثال: مجوهرات الأصيل", color = Gray, fontSize = 12.sp)
                }
                BasicTextField(
                    value = shopName,
                    onValueChange = { shopName = it },
                    singleLine = true,
                    textStyle = TextStyle(color = White, fontSize = 12.sp, textDirection = TextDirection.Content),
                    cursorBrush = SolidColor(Gold),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(shopNameFocus)
                )
            }

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
                    Text("إلغاء", color = Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(Gold)
                        .clickable {
                            val finalName = shopName.trim().ifEmpty { "محل بدون اسم" }
                            onConfirm(finalName)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("حفظ", color = Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
            Text("ممتاز", color = Green, fontSize = 9.sp)
            Text("عادل", color = Yellow, fontSize = 9.sp)
            Text("مرتفع", color = Red, fontSize = 9.sp)
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
                "${fmt(price, 2, grouped = true)} ريال",
                color = White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            if (savings > 0) {
                Text("وفر ${fmt(savings, 2, grouped = true)}", color = Green, fontSize = 9.sp)
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
    onDelete: (() -> Unit)? = null
) {
    val isEditing = editingItem != null
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf(editingItem?.name ?: "") }
    var selectedEmoji by remember { mutableStateOf(editingItem?.emoji ?: pieceEmojiOptions.first().first) }
    var karat by remember { mutableStateOf(editingItem?.karat ?: "21K") }
    var weight by remember { mutableDoubleStateOf(editingItem?.weightGrams ?: 5.0) }
    // عند التعديل، السعر المحفوظ (purchasePriceWithTax) شامل الضريبة أصلاً
    var purchasePrice by remember { mutableDoubleStateOf(editingItem?.purchasePriceWithTax ?: 3000.0) }
    var includingTax by remember { mutableStateOf(true) }
    var manufacturing by remember { mutableDoubleStateOf(editingItem?.manufacturingPerGram ?: 35.0) }
    var purchaseDate by remember { mutableStateOf(editingItem?.purchaseDate ?: todayDateText()) }
    var notes by remember { mutableStateOf(editingItem?.notes ?: "") }
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
                contentDescription = "رجوع",
                tint = Gold,
                modifier = Modifier
                    .size(22.dp)
                    .clickable { onBack() }
            )
            Text(
                if (isEditing) "تعديل القطعة" else "إضافة قطعة",
                color = White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            if (isEditing && onDelete != null) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "حذف القطعة",
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

        Text("اسم القطعة", color = Gray, fontSize = 10.sp)
        Spacer(Modifier.height(6.dp))
        val nameFocus = remember { FocusRequester() }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .clip(RoundedCornerShape(9.dp))
                .border(1.dp, Border, RoundedCornerShape(9.dp))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { nameFocus.requestFocus() }
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (name.isEmpty()) {
                Text("مثال: خاتم - سوار - سبيكة", color = Gray, fontSize = 12.sp)
            }
            BasicTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                textStyle = TextStyle(color = White, fontSize = 12.sp, textDirection = TextDirection.Content),
                cursorBrush = SolidColor(Gold),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(nameFocus)
            )
        }

        Spacer(Modifier.height(12.dp))

        Text("شكل القطعة", color = Gray, fontSize = 10.sp)
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
                    Text(label, color = if (selected) Gold else Gray, fontSize = 8.sp, maxLines = 1)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Text("العيار", color = Gray, fontSize = 10.sp)
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

        Text("الوزن (جرام)", color = Gray, fontSize = 10.sp)
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
            Text("بلد الصنع", color = Gray, fontSize = 10.sp)
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
                Text("السعودية", color = White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Icon(
                    imageVector = Icons.Outlined.Language,
                    contentDescription = null,
                    tint = Gray,
                    modifier = Modifier.size(15.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
        }

        Text("سعر الشراء (ريال)", color = Gray, fontSize = 10.sp)
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
                Text("السعر شامل الضريبة؟", color = White, fontSize = 12.sp)
            }
        } else {
            Spacer(Modifier.height(6.dp))
            Text("ذهب استثماري 24 عيار — معفى من الضريبة", color = Gray, fontSize = 9.sp)
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("المصنعية (للجرام) ريال", color = White, fontSize = 10.sp)
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

        Text("تاريخ الشراء", color = Gray, fontSize = 10.sp)
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

        Text("ملاحظات (اختياري)", color = Gray, fontSize = 10.sp)
        Spacer(Modifier.height(6.dp))
        val notesFocus = remember { FocusRequester() }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .clip(RoundedCornerShape(9.dp))
                .border(1.dp, Border, RoundedCornerShape(9.dp))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { notesFocus.requestFocus() }
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (notes.isEmpty()) {
                Text("اكتب ملاحظة...", color = Gray, fontSize = 12.sp)
            }
            BasicTextField(
                value = notes,
                onValueChange = { notes = it },
                singleLine = true,
                textStyle = TextStyle(color = White, fontSize = 12.sp, textDirection = TextDirection.Content),
                cursorBrush = SolidColor(Gold),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(notesFocus)
            )
        }

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
            Text("معاينة القيمة الحالية", color = White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))

            Column {
                Text("سعر جرام الذهب (${karatLabel(karat)})", color = Gray, fontSize = 9.sp)
                Text(
                    "${fmt(karatPrice, 2)} ريال",
                    color = Gold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(10.dp))
            CalculatorRow("قيمة الذهب (بدون مصنعية)", "${fmt(currentBeforeVat, 2, grouped = true)} ريال")
            CalculatorRow("قيمة المصنعية", "${fmt(currentManufacturing, 2, grouped = true)} ريال")
            if (isTaxExempt) {
                CalculatorRow("ضريبة القيمة المضافة", "معفى")
            } else {
                CalculatorRow("ضريبة القيمة المضافة (15%)", "${fmt(currentVat, 2, grouped = true)} ريال")
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
                Text("القيمة الحالية للقطعة", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(
                    "${fmt(currentTotal, 2, grouped = true)} ريال",
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
                Text("الربح / الخسارة الحالية", color = Gray, fontSize = 10.sp)
                Text(
                    "${if (profit >= 0) "+" else ""}${fmt(profit, 2, grouped = true)} ريال (${fmt(profitPercent, 2)}%)",
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
                Text("إلغاء", color = Gray, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Box(
                modifier = Modifier
                    .weight(2f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Gold)
                    .clickable {
                        val finalName = name.trim().ifEmpty { "قطعة ذهب" }
                        onSave(
                            GoldItem(
                                name = finalName,
                                emoji = selectedEmoji,
                                karat = karat,
                                weightGrams = weight,
                                purchasePriceWithTax = purchasePriceWithTax,
                                manufacturingPerGram = manufacturing,
                                purchaseDate = purchaseDate,
                                notes = notes
                            )
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (isEditing) "حفظ التعديلات" else "حفظ في المحفظة",
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
                    Text("موافق")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("إلغاء")
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
                Text("حذف القطعة؟", color = White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text("لا يمكن التراجع عن هذا الإجراء.", color = Gray, fontSize = 11.sp)
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
                        Text("إلغاء", color = Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                        Text("حذف نهائياً", color = White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==================== شاشة الرسم البياني الكاملة ====================
private val chartPeriods = listOf("24 ساعة", "أسبوع", "شهر", "3 شهور", "6 شهور", "سنة", "سنتان", "5 سنين")

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
                contentDescription = "رجوع",
                tint = Gold,
                modifier = Modifier
                    .size(22.dp)
                    .clickable { onBack() }
            )
            Text(
                if (showAnalysis) "التحليل الفني" else "تتبع الأسعار",
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
            listOf(false to "تتبع الأسعار", true to "التحليل الفني").forEach { (analysisMode, label) ->
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
                        period,
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

    val topNews = fullNewsList.first()
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
                    "سعر الذهب (عيار ${karat.removeSuffix("K")})",
                    color = White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        fmt(karatPrice.price, 2) + " ريال",
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
                    .height(130.dp)
            ) {
                KaratChartCanvas(
                    modifier = Modifier.fillMaxSize(),
                    basePrice = karatPrice.price,
                    seed = karatChartSeeds[karat] ?: 1,
                    period = period,
                    realPoints = realChartPointsFor(karat, period)
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
                    Text("الفترة", color = Gray, fontSize = 9.sp)
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
                    if (stats.isReal) "⚡ بيانات تاريخية حقيقية (goldprice.dev)" else "≈ تقدير مبني على زخم آخر 30 يوماً الحقيقية",
                    color = if (stats.isReal) Green else Yellow,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OpenCloseCard(title = "سعر الذهب عيار 24", stats = stats24, modifier = Modifier.weight(1f))
                OpenCloseCard(title = "سعر الذهب عيار 21", stats = stats21, modifier = Modifier.weight(1f))
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
                "تحليل فني بالذكاء الاصطناعي",
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
                        "الاتجاه العام: ${if (trendUp) "صاعد" else "هابط"}",
                        color = Gold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "استمرار الزخم ${if (trendUp) "الإيجابي" else "السلبي"} طالما بقي سعر الإغلاق " +
                            "${if (trendUp) "أعلى" else "أدنى"} من سعر الافتتاح لنفس الفترة.",
                    color = Gray,
                    fontSize = 10.sp,
                    lineHeight = 15.sp,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(12.dp))
            Text(
                "أهم النقاط (فترة: $period)",
                color = White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
            Spacer(Modifier.height(8.dp))

            AnalysisPoint(
                "افتتح عند ${fmt(stats.openPrice, 2)} وأغلق عند ${fmt(stats.closePrice, 2)} ريال — " +
                        "إغلاق ${if (trendUp) "أعلى" else "أدنى"} من الافتتاح بـ " +
                        "${fmt(kotlin.math.abs(changePercent), 2)}%."
            )
            AnalysisPoint("أهم خبر مؤثر الآن: ${topNews.text} (${topNews.time}).")
            AnalysisPoint(
                if (daysUntilFed != null)
                    "اجتماع الفيدرالي القادم بعد $daysUntilFed يوماً (${fedDate!!.toPeriodDisplayText()}) — " +
                            "قد يزيد التذبذب قرب الإعلان."
                else
                    "لا يوجد اجتماع فيدرالي مجدول قريباً ضمن التقويم الحالي."
            )
            AnalysisPoint(
                "أقرب دعم عند ${fmt(stats.periodLow, 2)} ريال، وأقرب مقاومة عند " +
                        "${fmt(stats.periodHigh, 2)} ريال (أدنى وأعلى سعر خلال الفترة)."
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
                    "توقعات الذكاء الاصطناعي",
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
                                "في حال استمر الإغلاق فوق الافتتاح ونبرة الأخبار إيجابية، يُتوقع اختبار " +
                                        "مستوى ${fmt(stats.periodHigh, 2)} ريال خلال الفترة القادمة. "
                            )
                        } else {
                            append(
                                "في حال استمر الإغلاق دون الافتتاح، فقد يتجه السعر لاختبار مستوى " +
                                        "${fmt(stats.periodLow, 2)} ريال خلال الفترة القادمة. "
                            )
                        }
                        if (daysUntilFed != null && daysUntilFed <= 14) {
                            append("مع اقتراب اجتماع الفيدرالي بعد $daysUntilFed يوماً، يُتوقع ارتفاع التذبذب حول الإعلان.")
                        } else {
                            append("لا يوجد حدث فيدرالي وشيك يُتوقع أن يزيد التذبذب حالياً.")
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
                    "ملاحظة: هذا التحليل يعتمد على بيانات الأسعار والأخبار ومواعيد الفيدرالي، وليس توصية استثمارية ملزمة.",
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
            Text(fmt(stats.openPrice, 2) + " ريال", color = White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            Text("افتتاح", color = Gray, fontSize = 8.5.sp)
        }
        Spacer(Modifier.height(3.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(fmt(stats.closePrice, 2) + " ريال", color = White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            Text("إغلاق", color = Gray, fontSize = 8.5.sp)
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
    val title = "عيار ${karatLabel(karat).removeSuffix(" عيار")} - ريال سعودي"
    AppCard(title = title, modifier = Modifier.fillMaxWidth().height(215.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                fmt(price, 2) + " ريال",
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

private fun usdPerOunceToSarPerGram(usdPerOunce: Double, karat: String): Double {
    val purity = karatPurity[karat] ?: 1.0
    return (usdPerOunce / TROY_OUNCE_GRAMS) * purity * HISTORY_USD_TO_SAR
}

// الشموع الحقيقية (GoldHistory) التي تقع ضمن الفترة المطلوبة، أو null إن
// لم تتوفر (الفترة أطول من سقف الخطة المجانية 30 يوماً، أو لم يجلب
// التطبيق بيانات بعد)
private fun realBarsFor(period: String): List<HistoryBar>? {
    if (periodDaysFor(period) > 30) return null
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
    // true = مبنية على بيانات تاريخية حقيقية من goldprice.dev، false = تقدير
    // ذكي مبني على زخم آخر 30 يوماً الحقيقية (للفترات الأطول من شهر، التي
    // تحتاج اشتراكاً مدفوعاً عند نفس المزوّد للحصول على بيانات حقيقية لها)
    val isReal: Boolean
)

private fun karatPeriodStats(basePrice: Double, karat: String, period: String): KaratPeriodStats {
    val realBars = realBarsFor(period)
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
// لحساب عدد الأيام المتبقية في شاشة التحليل الفني
private fun nextFedMeetingDate(): LocalDate? {
    val today = todayLocalDate()
    return fedMeetingsRaw
        .map { LocalDate(it.year, it.month, it.day) }
        .filter { it >= today }
        .minOrNull()
}

// تسميات محور الوقت أسفل الرسم، حسب الفترة المختارة (زي فيديو المرجع)
private fun xAxisLabelsFor(period: String): List<String> = when (period) {
    "24 ساعة" -> listOf("15:00", "18:00", "21:00", "00:00", "03:00", "06:00", "09:00", "12:00")
    "أسبوع" -> listOf("سبت", "أحد", "اثنين", "ثلاثاء", "أربعاء", "خميس", "جمعة")
    "شهر" -> listOf("1", "5", "10", "15", "20", "25", "30")
    "3 شهور" -> listOf("الشهر 1", "الشهر 2", "الشهر 3")
    "6 شهور" -> listOf("1", "2", "3", "4", "5", "6")
    "سنة" -> listOf("يناير", "مارس", "مايو", "يوليو", "سبتمبر", "نوفمبر")
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
            "${fmt(info.price, 2)} ريال",
            color = Gold,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
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
    realPoints: List<Pair<String, Double>>? = null
) {
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
private data class NewsItem(val dot: Color, val text: String, val time: String)

private val fullNewsList = listOf(
    NewsItem(Red, "مجلس الاحتياطي الفيدرالي يشير إلى تأجيل خفض أسعار الفائدة", "منذ 36 دقيقة"),
    NewsItem(Yellow, "ارتفاع مؤشر الدولار لأعلى مستوى في شهر", "منذ ساعتين"),
    NewsItem(Green, "ضعف بيانات التضخم في أمريكا", "منذ 3 ساعات"),
    NewsItem(Red, "ارتفاع الطلب على الذهب في الأسواق الآسيوية", "منذ 5 ساعات"),
    NewsItem(Yellow, "تراجع أسعار النفط يؤثر على معنويات المستثمرين", "منذ 8 ساعات"),
    NewsItem(Green, "البنوك المركزية تواصل شراء الذهب كاحتياطي", "أمس"),
    NewsItem(Red, "توترات جيوسياسية تدفع المستثمرين نحو الذهب", "أمس"),
    NewsItem(Yellow, "تحليل: أين تتجه أسعار الذهب خلال الربع القادم؟", "قبل يومين")
)

@Composable
private fun NewsScreen(onBack: () -> Unit) {
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
                contentDescription = "رجوع",
                tint = Gold,
                modifier = Modifier
                    .size(20.dp)
                    .clickable { onBack() }
            )
            Text(
                "الأخبار",
                color = Gold,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(fullNewsList) { news ->
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
private const val goldItemsStorageFile = "gold_items.json"

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

private fun persistGoldItems(items: List<GoldItem>) {
    AppStorage.writeText(goldItemsStorageFile, Json.encodeToString(items))
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

private data class PortfolioTotals(val totalValue: Double, val itemCount: Int, val totalWeight: Double)

private fun portfolioTotals(savedItems: List<GoldItem>): PortfolioTotals = PortfolioTotals(
    totalValue = savedItems.sumOf { it.currentValue() },
    itemCount = savedItems.size,
    totalWeight = savedItems.sumOf { it.weightGrams }
)

@Composable
private fun PortfolioScreen(
    savedItems: List<GoldItem>,
    onNavigateAddItem: () -> Unit,
    onEditItem: (Int) -> Unit,
    onBack: () -> Unit
) {
    val savedValues = savedItems.map { it to it.currentValue() }
    val totalValue = savedValues.sumOf { it.second }
    val totalWeight = savedItems.sumOf { it.weightGrams }
    val itemCount = savedItems.size

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
                contentDescription = "رجوع",
                tint = Gold,
                modifier = Modifier
                    .size(20.dp)
                    .clickable { onBack() }
            )
            Text(
                "المحفظة",
                color = Gold,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Box(modifier = Modifier.padding(horizontal = 12.dp)) {
            PortfolioSummary(totalValue = totalValue, itemCount = itemCount, totalWeight = totalWeight)
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
                    Text("إضافة قطعة", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                        .border(1.dp, Border, RoundedCornerShape(9.dp))
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
                                .background(GoldDark.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(item.emoji, fontSize = 12.sp)
                        }
                        Column {
                            Text(item.name, color = White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "${item.karat} • ${fmt(item.weightGrams, 2)} جرام",
                                color = Gray,
                                fontSize = 10.sp
                            )
                        }
                    }
                    Text(
                        "${fmt(value, 2, grouped = true)} ريال",
                        color = Gold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
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

    fun earliestPurchaseDate(karat: String): String =
        savedItems
            .filter { it.karat == karat }
            .mapNotNull { parseDisplayDate(it.purchaseDate) }
            .minOrNull()
            ?.toDisplayText()
            ?: todayDateText()

    fun fillWeightsFromPortfolio() {
        weight24 = savedItems.filter { it.karat == "24K" }.sumOf { it.weightGrams }
        weight22 = savedItems.filter { it.karat == "22K" }.sumOf { it.weightGrams }
        weight21 = savedItems.filter { it.karat == "21K" }.sumOf { it.weightGrams }
        weight18 = savedItems.filter { it.karat == "18K" }.sumOf { it.weightGrams }
        date24 = earliestPurchaseDate("24K")
        date22 = earliestPurchaseDate("22K")
        date21 = earliestPurchaseDate("21K")
        date18 = earliestPurchaseDate("18K")
    }

    val allZakatItems = zakatItems + savedItems.map { it.toZakatItem() }
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
                    contentDescription = "رجوع",
                    tint = Gold,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onBack() }
                )
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = "تحديث",
                    tint = Gold,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { marketScope.launch { GoldMarket.refresh() } }
                )
            }
            Text("الزكاة", color = White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = "معلومات عن زكاة الذهب",
                tint = Gold,
                modifier = Modifier
                    .size(20.dp)
                    .clickable { showZakatInfo = true }
            )
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
                    contentDescription = "معلومات عن نصاب الزكاة",
                    tint = Gold,
                    modifier = Modifier.size(12.dp)
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text("نصاب الزكاة (85 جم ذهب عيار 24)", color = Gray, fontSize = 10.sp)
                Text(
                    "${fmt(nisabValue, 2, grouped = true)} ريال",
                    color = Gold,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold
                )
                Text("يتحدّث مباشرة مع سعر الذهب العالمي", color = Gray, fontSize = 8.5.sp)
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
                    Text("إجمالي قيمة الذهب", color = Gray, fontSize = 10.sp)
                    Text(
                        fmt(totalGoldValue, 2, grouped = true),
                        color = Gold,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("ريال سعودي", color = Gray, fontSize = 9.sp)
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
                Text("مبلغ الزكاة المستحق", color = Gray, fontSize = 10.sp)
                Text(
                    fmt(totalZakat, 2, grouped = true),
                    color = Gold,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )
                Text("ريال سعودي", color = Gray, fontSize = 9.sp)
                Text(
                    "(${fmt(zakatPercent, 1)}%) من إجمالي قيمة الذهب",
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
                "تفاصيل الزكاة",
                color = White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
            Spacer(Modifier.height(12.dp))

            Text(
                "أوزان الذهب المملوكة (جرام)",
                color = Gray,
                fontSize = 9.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
            Spacer(Modifier.height(8.dp))
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
                    if (exceedsNisab) "الزكاة واجبة" else "الزكاة غير واجبة (أقل من النصاب)",
                    color = if (exceedsNisab) Green else Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                ZakatDetailItem(
                    title = "نصاب الزكاة (الذهب)",
                    value = fmt(nisabValue, 2, grouped = true),
                    unit = "ريال (85 جرام ع24)",
                    modifier = Modifier.weight(1f)
                )
                ZakatDetailItem(
                    title = "إجمالي الوزن",
                    value = fmt(totalWeight, 3),
                    unit = "جرام",
                    modifier = Modifier.weight(1f)
                )
                ZakatDetailItem(
                    title = "إجمالي قيمة الذهب",
                    value = fmt(totalGoldValue, 2, grouped = true),
                    unit = "ريال",
                    modifier = Modifier.weight(1f)
                )
                ZakatDetailItem(
                    title = "مبلغ الزكاة (${fmt(zakatPercent, 1)}%)",
                    value = fmt(totalZakat, 2, grouped = true),
                    unit = "ريال",
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
                "يجب الزكاة إذا بلغ الذهب المملوك النصاب الشرعي، وهو 85 جراماً من عيار 24 " +
                        "(أو ما يعادلها بالعيارات الأخرى)، أي ما قيمته الآن نحو " +
                        "${fmt(nisabValue, 2, grouped = true)} ريال.\n" +
                        "قيمة ذهبك الحالية (${fmt(totalGoldValue, 2, grouped = true)} ريال) " +
                        (if (exceedsNisab) "تتجاوز" else "لا تتجاوز") +
                        " النصاب.",
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
                        if (showAllItems) "عرض أقل" else "عرض الكل",
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
                Text("تفاصيل الأصناف المحسوبة", color = White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, Gold, RoundedCornerShape(8.dp))
                    .clickable { fillWeightsFromPortfolio() },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.AccountBalanceWallet,
                    contentDescription = null,
                    tint = Gold,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "استخدام الأوزان الموجودة في المحفظة",
                    color = Gold,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text("مبلغ الزكاة", color = Gray, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Text("الحالة", color = Gray, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.1f), textAlign = TextAlign.Center)
                Text("قيمة الذهب", color = Gray, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Text("الوزن (جم)", color = Gray, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.8f), textAlign = TextAlign.Center)
                Text("عيار", color = Gray, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.6f), textAlign = TextAlign.Center)
                Text("الصنف", color = Gray, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f), textAlign = TextAlign.Center)
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
                    Text("إضافة صنف جديد", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("+", color = Gold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // ---- بطاقة إعدادات حساب الزكاة ----
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, Border, RoundedCornerShape(12.dp))
                .background(CardBlack)
                .padding(14.dp)
        ) {
            Text(
                "إعدادات حساب الزكاة",
                color = White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("نسبة الزكاة", color = Gray, fontSize = 9.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
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
                            "(الذهب) ${fmt(zakatPercent, 2).trimEnd('0').trimEnd('.')}%",
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
                    Text("تاريخ حساب الزكاة", color = Gray, fontSize = 9.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
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
                    Text("تاريخ حساب الزكاة", color = Gray, fontSize = 9.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
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
                    "سيتم حساب حولان الحول 5 أيام وتحديده لك تلقائياً",
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
                    Text("شروط وجوب زكاة الذهب", color = White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "إغلاق",
                        tint = Gray,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { showZakatInfo = false }
                    )
                }

                Spacer(Modifier.height(6.dp))
                Text(
                    "النصاب الحالي: ${fmt(nisabValue, 2, grouped = true)} ريال (يعادل 85 جراماً من عيار 24)",
                    color = Gold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(14.dp))
                Text("شروط الوجوب", color = White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
                Spacer(Modifier.height(6.dp))
                Text(
                    "• بلوغ النصاب: 85 جراماً من الذهب الخالص (عيار 24)، وما يعادلها بالعيارات الأخرى (نحو 97 جراماً لعيار 21).\n" +
                            "• مرور الحول: أن يمضي عام هجري كامل على امتلاك النصاب.\n" +
                            "• الملك التام: أن يكون الذهب مملوكاً بالكامل وغير مرهون.",
                    color = Gray,
                    fontSize = 10.5.sp,
                    lineHeight = 17.sp,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(14.dp))
                Text("حكم ذهب الزينة", color = White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
                Spacer(Modifier.height(6.dp))
                Text(
                    "• ذهب الاستعمال الشخصي (الحلي المعتاد): لا زكاة فيه عند جمهور أهل العلم.\n" +
                            "• ذهب الادخار أو الاستثمار: تجب فيه الزكاة اتفاقاً إذا بلغ النصاب وحال عليه الحول.",
                    color = Gray,
                    fontSize = 10.5.sp,
                    lineHeight = 17.sp,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(14.dp))
                Text(
                    "مقدار الزكاة الواجب إخراجه: 2.5% (ربع العشر) من قيمة الذهب.",
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
                    Text("حسناً", color = Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                    Text("موافق")
                }
            },
            dismissButton = {
                TextButton(onClick = { activeDateKarat = null }) {
                    Text("إلغاء")
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
            fontSize = 8.5.sp,
            textAlign = TextAlign.Center,
            maxLines = 2,
            lineHeight = 10.sp
        )
        Spacer(Modifier.height(6.dp))
        Text(
            value,
            color = Gold,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(unit, color = Gray, fontSize = 8.sp, maxLines = 1)
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
        Text(karatLabel(karat).removeSuffix(" عيار"), color = Gray, fontSize = 9.sp)
        Spacer(Modifier.height(4.dp))
        NumericInputField(
            value = value,
            onValueChanged = onValueChanged,
            fontSize = 11.sp,
            minValue = 0.0,
            modifier = Modifier
                .width(58.dp)
                .height(30.dp)
                .clip(RoundedCornerShape(6.dp))
                .border(1.dp, Border, RoundedCornerShape(6.dp))
        )
        Text("جم", color = Gray, fontSize = 8.sp)
        Spacer(Modifier.height(3.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.clickable { onDateClick() }
        ) {
            Icon(
                imageVector = Icons.Outlined.CalendarMonth,
                contentDescription = "تاريخ الشراء",
                tint = Gray,
                modifier = Modifier.size(9.dp)
            )
            Text(dateText, color = Gray, fontSize = 6.8.sp, maxLines = 1)
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
            fontSize = 10.sp,
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
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Text(
                    status.label,
                    color = status.color,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
            Text(status.caption, color = Gray, fontSize = 6.2.sp, maxLines = 1)
        }

        Text(
            fmt(goldValue, 2, grouped = true),
            color = Gold,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )

        Text(
            fmt(item.weightGrams, 2),
            color = White,
            fontSize = 10.sp,
            modifier = Modifier.weight(0.8f),
            textAlign = TextAlign.Center
        )

        Text(
            karatLabel(item.karat).removeSuffix(" عيار"),
            color = White,
            fontSize = 10.sp,
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
                fontSize = 10.sp,
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
private fun MoreScreen(onBack: () -> Unit) {
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
                contentDescription = "رجوع",
                tint = Gold,
                modifier = Modifier
                    .size(20.dp)
                    .clickable { onBack() }
            )
            Text(
                "المزيد",
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
                    Text("م", color = Black, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("محمد العتيبي", color = White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("عرض الملف الشخصي", color = Gray, fontSize = 10.sp)
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

        Text("الإعدادات", color = Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(9.dp))
                .border(1.dp, Border, RoundedCornerShape(9.dp))
                .background(CardBlack)
        ) {
            SettingsRow(icon = Icons.Outlined.Notifications, label = "الإشعارات")
            SettingsDivider()
            SettingsRow(icon = Icons.Outlined.Language, label = "اللغة")
            SettingsDivider()
            SettingsRow(icon = Icons.Outlined.AttachMoney, label = "العملة")
            SettingsDivider()
            SettingsRow(icon = Icons.Outlined.Info, label = "عن التطبيق")
            SettingsDivider()
            SettingsRow(
                icon = Icons.AutoMirrored.Outlined.Logout,
                label = "تسجيل الخروج",
                tint = Red
            )
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun SettingsRow(icon: ImageVector, label: String, tint: Color = Gold) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
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
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
            contentDescription = null,
            tint = Gray,
            modifier = Modifier.size(16.dp)
        )
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

// ==================== الشريط العلوي ====================
@Composable
private fun Header(onRefresh: () -> Unit) {
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
                NotificationBell(count = 3)

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
                CircleButton(Icons.Outlined.Refresh, onClick = onRefresh)
                SmallGoldButton("SAR  ˅")
                CircleButton(Icons.Outlined.Bolt)
            }
        }
    }
}

@Composable
private fun NotificationBell(count: Int) {
    Box(modifier = Modifier.size(28.dp)) {
        Icon(
            imageVector = Icons.Outlined.Notifications,
            contentDescription = "الإشعارات",
            tint = Gold,
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.Center)
        )
        if (count > 0) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 3.dp, y = (-2).dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(Red),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = count.toString(),
                    color = White,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun GoldLogo() {
    Canvas(modifier = Modifier.size(30.dp)) {
        val w = size.width
        val h = size.height
        val cx = w / 2f

        val topY = h * 0.14f
        val bottomY = h * 0.97f
        val shoulderY = h * 0.34f
        val leftX = w * 0.04f
        val rightX = w * 0.96f
        val topLeftX = w * 0.22f
        val topRightX = w * 0.78f

        val diamond = Path().apply {
            moveTo(topLeftX, topY)
            lineTo(topRightX, topY)
            lineTo(rightX, shoulderY)
            lineTo(cx, bottomY)
            lineTo(leftX, shoulderY)
            close()
        }
        drawPath(path = diamond, color = Gold)

        val gColor = Color(0xFF171717)
        val gCy = shoulderY + h * 0.08f
        val s = h * 0.21f
        val t = h * 0.09f

        val bracket = Path().apply {
            moveTo(cx - s, gCy - s)
            lineTo(cx + s, gCy - s)
            lineTo(cx + s, gCy - s + t)
            lineTo(cx - s + t, gCy - s + t)
            lineTo(cx - s + t, gCy + s - t)
            lineTo(cx + s, gCy + s - t)
            lineTo(cx + s, gCy + s)
            lineTo(cx - s, gCy + s)
            close()
        }
        drawPath(path = bracket, color = gColor)

        drawRect(
            color = gColor,
            topLeft = Offset(cx, gCy - t / 2f),
            size = androidx.compose.ui.geometry.Size(s - t, t)
        )
    }
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
        GoldMarket.isLoading -> "يحدّث..."
        hasError -> "غير محدث"
        else -> "مباشر"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "أسعار الذهب الآن  ⓘ",
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
                        Text("ريال / جرام", color = Gray, fontSize = 10.sp)
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
                            "الأكثر استخداماً",
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
    AppCard(title = "حاسبة الذهب", modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(23.dp)
                .border(1.dp, Border, RoundedCornerShape(5.dp))
        ) {
            CalculatorMode(
                text = "شراء",
                selected = buyMode,
                modifier = Modifier.weight(1f)
            ) { onBuyModeChanged(true) }

            CalculatorMode(
                text = "بيع",
                selected = !buyMode,
                modifier = Modifier.weight(1f)
            ) { onBuyModeChanged(false) }
        }

        Spacer(Modifier.height(3.dp))
        Text("اختر العيار", color = White, fontSize = 10.sp)

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

        Text("الوزن (جرام)", color = White, fontSize = 10.sp)

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
        CalculatorRow("سعر الجرام", "${fmt(selectedPrice(selectedKarat), 2)} ريال")
        CalculatorRow(
            "السعر قبل الضريبة ⓘ",
            "${fmt(beforeVat, 2, grouped = true)} ريال",
            valueColor = Gold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("المصنعية (للجرام) ✎", color = White, fontSize = 10.sp)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text("ريال", color = Gray, fontSize = 9.sp)
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
            "الضريبة (15%)",
            "${fmt(vat, 2, grouped = true)} ريال"
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
            Text("الإجمالي", color = Gold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(
                "ريال ${fmt(total, 2, grouped = true)}",
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
    var text by remember { mutableStateOf(fmt(value, 2)) }

    LaunchedEffect(value) {
        val parsed = text.toDoubleOrNull()
        if (parsed == null || kotlin.math.abs(parsed - value) > 0.001) {
            text = fmt(value, 2)
        }
    }

    BasicTextField(
        value = text,
        onValueChange = { newValue ->
            if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                text = newValue
                newValue.toDoubleOrNull()?.let { parsedValue ->
                    if (parsedValue >= minValue) {
                        onValueChanged(parsedValue)
                    }
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
        modifier = modifier
    ) { innerTextField ->
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            innerTextField()
        }
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
    selectedKarat: String
) {
    val basePrice = selectedPrice(selectedKarat)
    val karatPercent = GoldMarket.prices.first { it.karat == selectedKarat }.percent
    val minPrice = basePrice * 0.94
    val maxPrice = basePrice * 1.06

    AppCard(
        title = "تتبع أسعار الذهب",
        titleIcon = Icons.AutoMirrored.Outlined.ShowChart,
        modifier = Modifier.fillMaxSize()
    ) {
        val periods = listOf("اليوم", "أسبوع", "شهر", "سنة", "10 سنوات", "20 سنة")

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
                        period,
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
        ) {
            PriceChartCanvas(modifier = Modifier.fillMaxSize(), basePrice = basePrice)

            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 2.dp, end = 2.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text("أعلى سعر", color = Gray, fontSize = 8.5.sp)
                Text(
                    fmt(maxPrice, 2),
                    color = White,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Text("ريال", color = Gray, fontSize = 8.sp)

                Spacer(Modifier.height(6.dp))

                Text("أدنى سعر", color = Gray, fontSize = 8.5.sp)
                Text(
                    fmt(minPrice, 2),
                    color = White,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Text("ريال", color = Gray, fontSize = 8.sp)
            }
        }
    }
}

@Composable
private fun PriceChartCanvas(modifier: Modifier, basePrice: Double) {
    val textMeasurer = rememberTextMeasurer()
    val minPrice = basePrice * 0.94
    val maxPrice = basePrice * 1.06
    val points = remember {
        val count = 60
        (0 until count).map { i ->
            val t = i / (count - 1).toFloat()
            val trend = 0.14f + t * 0.80f
            val ripple = (kotlin.math.sin(t * 14f) * 0.025f) + (kotlin.math.sin(t * 5f + 1f) * 0.035f)
            (trend + ripple).coerceIn(0.05f, 0.98f)
        }
    }
    val dateLabels = listOf("٣ يوليو", "٥ يوليو", "٧ يوليو", "٩ يوليو")

    Canvas(modifier = modifier.padding(top = 5.dp, bottom = 12.dp)) {
        val left = 40f
        val right = size.width - 8f
        val top = 8f
        val bottom = size.height - 30f
        val w = right - left
        val h = bottom - top

        for (i in 0..4) {
            val y = top + h * i / 4f
            drawLine(
                color = GoldDark.copy(alpha = 0.45f),
                start = Offset(left, y),
                end = Offset(right, y),
                strokeWidth = 1f
            )
        }

        for (i in 0..4) {
            val x = left + w * i / 4f
            drawLine(
                color = GoldDark.copy(alpha = 0.35f),
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
            lineTo(right, bottom)
            lineTo(left, bottom)
            close()
        }

        drawPath(
            path = fill,
            color = Green.copy(alpha = 0.28f)
        )

        drawPath(
            path = line,
            color = Green,
            style = Stroke(width = 2.4f, cap = StrokeCap.Round)
        )

        val priceLabels = (0..5).map { i -> fmt(maxPrice - (maxPrice - minPrice) * i / 5, 0) }
        priceLabels.forEachIndexed { index, label ->
            val y = bottom - h * index / 5f
            drawAxisLabel(textMeasurer, label, x = 0f, y = y, centered = false)
        }

        dateLabels.forEachIndexed { index, label ->
            val x = left + w * index / (dateLabels.size - 1)
            drawAxisLabel(textMeasurer, label, x = x, y = bottom + 22f, centered = true)
        }
    }
}

// ==================== أهم الأخبار ====================
@Composable
private fun ImportantNews() {
    AppCard(title = "أهم الأخبار المؤثرة", modifier = Modifier.fillMaxSize()) {
        NewsRow(
            dot = Red,
            text = "مجلس الاحتياطي الفيدرالي يشير إلى تأجيل خفض أسعار الفائدة",
            time = "منذ 36 دقيقة"
        )
        NewsRow(
            dot = Yellow,
            text = "ارتفاع مؤشر الدولار لأعلى مستوى في شهر",
            time = "منذ ساعتين"
        )
        NewsRow(
            dot = Green,
            text = "ضعف بيانات التضخم في أمريكا",
            time = "منذ 3 ساعات"
        )
        Spacer(Modifier.weight(1f))
        Text(
            "عرض المزيد",
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
private fun FedSchedule(rows: List<Triple<String, String, String>>) {
    AppCard(
        title = "مواعيد اجتماعات الفيدرالي",
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
                text = "اليوم",
                color = Gray,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(0.9f),
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            Text(
                text = "التاريخ",
                color = Gray,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1.1f),
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            Text(
                text = "الوقت",
                color = Gray,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1.1f),
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

        rows.forEach { (day, date, time) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = day,
                    color = White,
                    fontSize = 9.sp,
                    modifier = Modifier.weight(0.9f),
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )

                Text(
                    text = date,
                    color = White,
                    fontSize = 9.sp,
                    modifier = Modifier.weight(1.1f),
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )

                Text(
                    text = time,
                    color = White,
                    fontSize = 9.sp,
                    modifier = Modifier.weight(1.1f),
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "عرض الجدول الكامل",
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

// ==================== شريط ملخص المحفظة ====================
@Composable
private fun PortfolioSummary(totalValue: Double, itemCount: Int, totalWeight: Double) {
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
            title = "قيمة المحفظة",
            value = "${fmt(totalValue, 2, grouped = true)} ريال",
            extra = "",
            valueColor = Gold
        )
        DividerVertical()
        SummaryItem(
            title = "عدد المنتجات",
            value = "$itemCount منتجات",
            extra = "",
            valueColor = White
        )
        DividerVertical()
        SummaryItem(
            title = "إجمالي الوزن",
            value = "${fmt(totalWeight, 2)} جرام",
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
                Text("عرض المحفظة", color = Gold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
    valueColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 5.dp)
    ) {
        Text(title, color = Gold, fontSize = 10.sp)
        Text(value, color = valueColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        if (extra.isNotEmpty()) {
            Text(extra, color = Green, fontSize = 9.sp)
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
        "الرئيسية" to Icons.Outlined.Home,
        "حاسبة الذهب" to Icons.Outlined.Calculate,
        "الأخبار" to Icons.AutoMirrored.Outlined.Article,
        "المحفظة" to Icons.Outlined.AccountBalanceWallet,
        "الزكاة" to Icons.Outlined.Balance,
        "المزيد" to Icons.Outlined.MoreHoriz
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
