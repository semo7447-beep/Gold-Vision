package com.goldvision

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

private const val DAILY_PRICE_CHANNEL_ID = "gold_vision_daily_price"
private const val DAILY_PRICE_NOTIFICATION_ID = 1001

// يعمل حتى لو كان التطبيق مغلقاً تماماً (مُشغَّل من WorkManager): يجلب
// آخر شمعة يومية حقيقية من مزوّد الأسعار، ويُصدر إشعاراً بسعري
// الافتتاح والإغلاق الفعليين لعيار 24 وعيار 21
internal class DailyPriceNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            GoldHistory.refresh(todayLocalDate())
            val (title, body) = buildDailyPriceNotificationText(GoldHistory.dailyBarsUsdPerOunce)
                ?: return Result.success()
            postNotification(title, body)
            Result.success()
        } catch (e: Exception) {
            reportSilentError("DailyPriceNotificationWorker failed: ${e.message}")
            Result.retry()
        }
    }

    private fun postNotification(title: String, body: String) {
        val context = applicationContext
        val notificationManager = NotificationManagerCompat.from(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    DAILY_PRICE_CHANNEL_ID,
                    "سعر الذهب اليومي",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
        }

        // يتطلب أندرويد 13 فأعلى صلاحية صريحة، تُطلب مرة عند بدء التطبيق
        // من MainActivity؛ إن رُفضت نتجاهل الإشعار بصمت بدل تعطّل العمل الدوري
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val notification = NotificationCompat.Builder(context, DAILY_PRICE_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(DAILY_PRICE_NOTIFICATION_ID, notification)
    }
}
