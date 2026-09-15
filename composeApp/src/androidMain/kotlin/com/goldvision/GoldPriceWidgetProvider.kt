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
    // اتجاه صفوف السعر/العيار يتبع تلقائياً لغة *نظام* الجهاز (RTL/LTR)،
    // لا لغة التطبيق الداخلية المستقلة (AppLanguage) — فلو كانا مختلفين
    // (مثلاً نظام الجهاز عربي والتطبيق إنجليزي)، يصير ترتيب العناصر
    // بالصف معكوساً بشكل خاطئ ويلتصق السعر بالعيار بلا مسافة. نفرض هنا
    // اتجاهاً صريحاً يطابق لغة التطبيق نفسها دائماً، بغض النظر عن لغة النظام
    views.setInt(
        R.id.widget_root,
        "setLayoutDirection",
        if (AppLanguage.current == AppLang.EN) android.view.View.LAYOUT_DIRECTION_LTR else android.view.View.LAYOUT_DIRECTION_RTL
    )
    // تسميات "24 عيار"...إلخ ثابتة داخل XML كقيمة افتراضية عربية؛ تُستبدل
    // هنا فعلياً عند كل بناء حتى تتبع اللغة الحالية بدل البقاء عربية دائماً
    views.setTextViewText(R.id.widget_label_24, t("24 عيار", "24K"))
    views.setTextViewText(R.id.widget_label_22, t("22 عيار", "22K"))
    views.setTextViewText(R.id.widget_label_21, t("21 عيار", "21K"))
    views.setTextViewText(R.id.widget_label_18, t("18 عيار", "18K"))
    GoldMarket.prices.forEach { price ->
        val priceId = when (price.karat) {
            "24K" -> R.id.widget_price_24
            "22K" -> R.id.widget_price_22
            "21K" -> R.id.widget_price_21
            "18K" -> R.id.widget_price_18
            else -> return@forEach
        }
        views.setTextViewText(priceId, "${fmt(price.price, 2)} ${t("ريال", "SAR")}")
    }
    views.setTextViewText(R.id.widget_updated_at, widgetUpdatedAtText())

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
