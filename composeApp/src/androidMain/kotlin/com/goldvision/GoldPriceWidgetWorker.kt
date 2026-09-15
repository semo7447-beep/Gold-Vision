package com.goldvision

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

// يُنفَّذ دورياً (كل 30 دقيقة عبر onEnabled أعلاه) ومرة فورية عند أول ظهور
// للويدجت (عبر onUpdate) — يجلب السعر الحقيقي الحالي ثم يدفعه لكل نسخ
// الويدجت الموجودة على الشاشة الرئيسية
internal class GoldPriceWidgetWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return try {
            GoldMarket.refresh(force = inputData.getBoolean("force", false))
            // يجلب شمعة اليوم الحقيقية (افتتاح/إغلاق) لعرضها في بوكسي
            // الافتتاح/الإغلاق بالويدجت الجديد — نفس المصدر المستخدَم أصلاً
            // لإشعار "أسعار الذهب كل ساعة"
            GoldHistory.refresh(todayLocalDate())
            val appWidgetManager = AppWidgetManager.getInstance(applicationContext)
            val componentName = ComponentName(applicationContext, GoldPriceWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            val remoteViews = buildWidgetRemoteViews(applicationContext)
            appWidgetIds.forEach { appWidgetId ->
                appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
