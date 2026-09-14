package com.goldvision

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

// يُنفَّذ دورياً (كل 30 دقيقة) ومرة فورية عند أول ظهور لويدجت المحفظة —
// يحدّث السعر الحي ثم يعيد حساب قيمة المحفظة ويدفعها لكل نسخ الويدجت
internal class GoldPortfolioWidgetWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return try {
            GoldMarket.refresh(force = inputData.getBoolean("force", false))
            val appWidgetManager = AppWidgetManager.getInstance(applicationContext)
            val componentName = ComponentName(applicationContext, GoldPortfolioWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            val remoteViews = buildPortfolioWidgetRemoteViews(applicationContext)
            appWidgetIds.forEach { appWidgetId ->
                appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
