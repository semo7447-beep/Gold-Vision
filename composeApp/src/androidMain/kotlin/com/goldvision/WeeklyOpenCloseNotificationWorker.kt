package com.goldvision

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

private const val WEEKLY_OPEN_CLOSE_CHANNEL_ID = "gold_vision_weekly_open_close"
private const val WEEKLY_OPEN_CLOSE_NOTIFICATION_ID = 1003

// يعمل حتى لو كان التطبيق مغلقاً تماماً (مُشغَّل من WorkManager): يجلب
// آخر شمعة يومية حقيقية من مزوّد الأسعار، ويُصدر إشعاراً أسبوعياً بسعري
// الافتتاح والإغلاق الفعليين لعيار 24 وعيار 21
internal class WeeklyOpenCloseNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            GoldHistory.refresh(todayLocalDate())
            val (title, body) = buildWeeklyOpenCloseNotificationText(GoldHistory.dailyBarsUsdPerOunce)
                ?: return Result.success()
            postNotification(title, body)
            Result.success()
        } catch (e: Exception) {
            reportSilentError("WeeklyOpenCloseNotificationWorker failed: ${e.message}")
            Result.retry()
        }
    }

    private fun postNotification(title: String, body: String) {
        val context = applicationContext
        val notificationManager = NotificationManagerCompat.from(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    WEEKLY_OPEN_CLOSE_CHANNEL_ID,
                    "أسعار الذهب الأسبوعية (افتتاح وإغلاق)",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context, WEEKLY_OPEN_CLOSE_NOTIFICATION_ID, openAppIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, WEEKLY_OPEN_CLOSE_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(WEEKLY_OPEN_CLOSE_NOTIFICATION_ID, notification)
    }
}
