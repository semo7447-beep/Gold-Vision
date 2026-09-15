package com.goldvision

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.concurrent.TimeUnit

private const val WIDGET_UPDATE_WORK_NAME = "gold_vision_widget_update"

// ويدجت الشاشة الرئيسية لعرض السعر الحي — يستخدم AppWidgetProvider/RemoteViews
// القياسيين في أندرويد (بلا مكتبة Compose إضافية للويدجتات)، حتى تبقى
// موثوقية الواجهة عالية بغض النظر عن إصدار أي مكتبة خارجية
internal class GoldPriceWidgetProvider : AppWidgetProvider() {
    companion object {
        // إجراء مخصّص لزر "تحديث يدوي" داخل الويدجت نفسه، بالإضافة للتحديث
        // التلقائي الدوري — يُرسَل كـ broadcast صريح لهذا المكوّن نفسه
        const val ACTION_REFRESH = "com.goldvision.widget.ACTION_REFRESH"
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            // ضغط يدوي صريح من المستخدم على زر التحديث — يتجاوز الحد الأدنى
            // بين طلبات التحديث التلقائية (حماية حصة GoldAPI.io الشهرية)
            val request = OneTimeWorkRequestBuilder<GoldPriceWidgetWorker>()
                .setInputData(workDataOf("force" to true))
                .build()
            WorkManager.getInstance(context).enqueue(request)
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // عرض فوري بآخر سعر موجود بالذاكرة (حتى لو قديم) بدل شاشة فارغة،
        // ثم يُستبدل بالسعر الفعلي الجديد بعد اكتمال عامل التحديث أدناه
        appWidgetIds.forEach { appWidgetId ->
            appWidgetManager.updateAppWidget(appWidgetId, buildWidgetRemoteViews(context))
        }
        WorkManager.getInstance(context).enqueue(OneTimeWorkRequestBuilder<GoldPriceWidgetWorker>().build())
    }

    // أول ويدجت يُضاف للشاشة الرئيسية: يبدأ تحديثاً دورياً كل 30 دقيقة
    override fun onEnabled(context: Context) {
        val request = PeriodicWorkRequestBuilder<GoldPriceWidgetWorker>(30, TimeUnit.MINUTES).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WIDGET_UPDATE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    // آخر ويدجت يُحذف من الشاشة الرئيسية: يوقف التحديث الدوري توفيراً للبطارية
    override fun onDisabled(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WIDGET_UPDATE_WORK_NAME)
    }
}

// يبني محتوى الويدجت من آخر أسعار متوفرة في الذاكرة (GoldMarket.prices) —
// مشتركة بين onUpdate (عرض فوري) وGoldPriceWidgetWorker (بعد تحديث حقيقي)
internal fun buildWidgetRemoteViews(context: Context): RemoteViews {
    val views = RemoteViews(context.packageName, R.layout.gold_price_widget)

    views.setTextViewText(R.id.widget_date, widgetDateText())

    val prices = GoldMarket.prices
    val price24 = prices.firstOrNull { it.karat == "24K" }
    views.setTextViewText(R.id.widget_big_karat_label, t("24 عيار", "24K"))
    views.setTextViewText(R.id.widget_big_price, price24?.let { fmt(it.price, 2) } ?: "--")
    views.setTextViewText(R.id.widget_big_unit, t("ريال/جرام", "SAR/gram"))

    views.setTextViewText(R.id.widget_small_label_18, t("18 عيار", "18K"))
    views.setTextViewText(R.id.widget_small_label_21, t("21 عيار", "21K"))
    views.setTextViewText(R.id.widget_small_label_22, t("22 عيار", "22K"))
    prices.forEach { price ->
        val priceId = when (price.karat) {
            "18K" -> R.id.widget_small_price_18
            "21K" -> R.id.widget_small_price_21
            "22K" -> R.id.widget_small_price_22
            else -> return@forEach
        }
        views.setTextViewText(priceId, fmt(price.price, 2))
    }

    // نسبة تغيّر اليوم لعيار 24: تقارن آخر سعر حي بسعر افتتاح شمعة اليوم
    // نفسها (من GoldHistory)، لا "منذ آخر تحديث" كسعر بطاقات الأعيرة
    val todayBar = GoldHistory.dailyBarsUsdPerOunce.maxByOrNull { it.date }
    val todayOpen24 = todayBar?.let { usdPerOunceToSarPerGram(it.open, "24K") }
    if (price24 != null && todayOpen24 != null && todayOpen24 > 0.0) {
        val changePercent = (price24.price - todayOpen24) / todayOpen24 * 100.0
        setChangePill(
            context, views,
            bgId = R.id.widget_big_change_bg,
            textId = R.id.widget_big_change,
            text = "${fmt(kotlin.math.abs(changePercent), 2)}% ${t("اليوم", "today")} ${if (changePercent >= 0) "▲" else "▼"}",
            positive = changePercent >= 0
        )
    } else {
        views.setTextViewText(R.id.widget_big_change, "")
    }

    // بوكسا الافتتاح/الإغلاق لعياري 21 و24 من نفس شمعة اليوم
    views.setTextViewText(R.id.widget_oc21_title, t("سعر الذهب عيار 21", "21K Gold Price"))
    views.setTextViewText(R.id.widget_oc24_title, t("سعر الذهب عيار 24", "24K Gold Price"))
    val labelOpen = t("افتتاح", "Open")
    val labelClose = t("إغلاق", "Close")
    views.setTextViewText(R.id.widget_oc21_open_label, labelOpen)
    views.setTextViewText(R.id.widget_oc21_close_label, labelClose)
    views.setTextViewText(R.id.widget_oc24_open_label, labelOpen)
    views.setTextViewText(R.id.widget_oc24_close_label, labelClose)
    if (todayBar != null) {
        val open21 = usdPerOunceToSarPerGram(todayBar.open, "21K")
        val close21 = usdPerOunceToSarPerGram(todayBar.close, "21K")
        val open24 = usdPerOunceToSarPerGram(todayBar.open, "24K")
        val close24 = usdPerOunceToSarPerGram(todayBar.close, "24K")
        views.setTextViewText(R.id.widget_oc21_open, "${fmt(open21, 2)} ${t("ريال", "SAR")}")
        views.setTextViewText(R.id.widget_oc21_close, "${fmt(close21, 2)} ${t("ريال", "SAR")}")
        views.setTextViewText(R.id.widget_oc24_open, "${fmt(open24, 2)} ${t("ريال", "SAR")}")
        views.setTextViewText(R.id.widget_oc24_close, "${fmt(close24, 2)} ${t("ريال", "SAR")}")
        val change21 = if (open21 > 0.0) (close21 - open21) / open21 * 100.0 else 0.0
        val change24 = if (open24 > 0.0) (close24 - open24) / open24 * 100.0 else 0.0
        setChangePill(
            context, views, R.id.widget_oc21_change_bg, R.id.widget_oc21_change,
            "${if (change21 >= 0) "+" else ""}${fmt(change21, 2)}%", change21 >= 0
        )
        setChangePill(
            context, views, R.id.widget_oc24_change_bg, R.id.widget_oc24_change,
            "${if (change24 >= 0) "+" else ""}${fmt(change24, 2)}%", change24 >= 0
        )
    } else {
        views.setTextViewText(R.id.widget_oc21_open, "--")
        views.setTextViewText(R.id.widget_oc21_close, "--")
        views.setTextViewText(R.id.widget_oc24_open, "--")
        views.setTextViewText(R.id.widget_oc24_close, "--")
        views.setTextViewText(R.id.widget_oc21_change, "")
        views.setTextViewText(R.id.widget_oc24_change, "")
    }

    // نقطة حالة الاتصال + تسمية "مباشر"/"غير محدث": تتبع نفس حالة
    // GoldMarket.lastError الحقيقية المستخدَمة في شريط "أسعار الذهب الآن"
    // داخل التطبيق نفسه
    val isLive = GoldMarket.lastError == null
    views.setTextViewText(R.id.widget_updated_at, if (isLive) t("مباشر", "Live") else t("غير محدث", "Outdated"))
    views.setInt(
        R.id.widget_status_dot,
        "setColorFilter",
        if (isLive) context.getColor(R.color.widget_green) else context.getColor(R.color.widget_red)
    )

    // FLAG_ACTIVITY_NEW_TASK إلزامي لإطلاق Activity من سياق غير Activity
    // (الويدجت هنا)، وبالتزامن مع singleTask في AndroidManifest.xml يضمن
    // إحضار نفس نسخة التطبيق الحالية للمقدمة بدل نسخة جديدة تفقد حالتها
    val openAppIntent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        openAppIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

    // زر تحديث يدوي فوري — منطقة ضغط منفصلة عن باقي الويدجت (فتح التطبيق)،
    // يرسل broadcast صريح لهذا المزوّد نفسه فيشغّل تحديث سعر فوري بالخلفية
    val refreshIntent = Intent(context, GoldPriceWidgetProvider::class.java).apply {
        action = GoldPriceWidgetProvider.ACTION_REFRESH
    }
    val refreshPendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        refreshIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    views.setOnClickPendingIntent(R.id.widget_refresh, refreshPendingIntent)

    return views
}

// يلوّن خلفية ونص شارة نسبة التغيّر أخضر/أحمر حسب الإشارة — drawable
// منفصل لكل لون بدل تلوين وقت التشغيل (أبسط وأضمن عبر RemoteViews)
private fun setChangePill(context: Context, views: RemoteViews, bgId: Int, textId: Int, text: String, positive: Boolean) {
    views.setTextViewText(textId, text)
    views.setInt(bgId, "setBackgroundResource", if (positive) R.drawable.pill_bg_green else R.drawable.pill_bg_red)
    views.setTextColor(textId, if (positive) context.getColor(R.color.widget_green) else context.getColor(R.color.widget_red))
}

// تاريخ اليوم بصيغة "اسم اليوم DD/MM/YYYY"، بلغة التطبيق الداخلية
// (AppLanguage) لا لغة نظام الجهاز — بلا مكتبة SimpleDateFormat (تعتمد
// على java.util غير متاحة بنفس الشكل على iOS مستقبلاً)
private val widgetArabicDayNames = mapOf(
    kotlinx.datetime.DayOfWeek.SATURDAY to "السبت",
    kotlinx.datetime.DayOfWeek.SUNDAY to "الأحد",
    kotlinx.datetime.DayOfWeek.MONDAY to "الاثنين",
    kotlinx.datetime.DayOfWeek.TUESDAY to "الثلاثاء",
    kotlinx.datetime.DayOfWeek.WEDNESDAY to "الأربعاء",
    kotlinx.datetime.DayOfWeek.THURSDAY to "الخميس",
    kotlinx.datetime.DayOfWeek.FRIDAY to "الجمعة"
)

private val widgetEnglishDayNames = mapOf(
    kotlinx.datetime.DayOfWeek.SATURDAY to "Saturday",
    kotlinx.datetime.DayOfWeek.SUNDAY to "Sunday",
    kotlinx.datetime.DayOfWeek.MONDAY to "Monday",
    kotlinx.datetime.DayOfWeek.TUESDAY to "Tuesday",
    kotlinx.datetime.DayOfWeek.WEDNESDAY to "Wednesday",
    kotlinx.datetime.DayOfWeek.THURSDAY to "Thursday",
    kotlinx.datetime.DayOfWeek.FRIDAY to "Friday"
)

private fun widgetDateText(): String {
    val today = todayLocalDate()
    val dayName = if (AppLanguage.current == AppLang.EN) {
        widgetEnglishDayNames[today.dayOfWeek] ?: ""
    } else {
        widgetArabicDayNames[today.dayOfWeek] ?: ""
    }
    val day = today.dayOfMonth.toString().padStart(2, '0')
    val month = today.monthNumber.toString().padStart(2, '0')
    return "$dayName $day/$month/${today.year}"
}

internal fun widgetUpdatedAtText(): String {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val hour = now.hour.toString().padStart(2, '0')
    val minute = now.minute.toString().padStart(2, '0')
    return t("آخر تحديث: $hour:$minute", "Updated: $hour:$minute")
}

private var widgetLanguageNotifierContext: Context? = null

internal fun initWidgetLanguageNotifier(context: Context) {
    widgetLanguageNotifierContext = context.applicationContext
}

// يعيد بناء نصوص كل ويدجت مضاف فعلاً (السعر والمحفظة) فوراً بلغة
// التطبيق الجديدة، بدل انتظار دورة التحديث الدورية القادمة
internal actual fun notifyWidgetsLanguageChanged() {
    val context = widgetLanguageNotifierContext ?: return
    val appWidgetManager = AppWidgetManager.getInstance(context)

    val priceIds = appWidgetManager.getAppWidgetIds(ComponentName(context, GoldPriceWidgetProvider::class.java))
    priceIds.forEach { id -> appWidgetManager.updateAppWidget(id, buildWidgetRemoteViews(context)) }

    val portfolioIds = appWidgetManager.getAppWidgetIds(ComponentName(context, GoldPortfolioWidgetProvider::class.java))
    portfolioIds.forEach { id -> appWidgetManager.updateAppWidget(id, buildPortfolioWidgetRemoteViews(context)) }
}
