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

private const val PRICE_ALERT_CHANNEL_ID = "gold_vision_price_alert"
private const val PRICE_ALERT_NOTIFICATION_ID_BASE = 2000

// يعمل حتى لو كان التطبيق مغلقاً تماماً: يجلب السعر الحي الفعلي، يقارنه
// بكل تنبيهات المستخدم المحفوظة، ويُصدر إشعاراً لكل تنبيه تحقق شرطه ثم
// يحذفه من القائمة (يعمل مرة واحدة فقط لكل تنبيه)
internal class PriceAlertWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val alerts = loadPriceAlerts()
            if (alerts.isEmpty()) {
                PriceAlertScheduler.setActive(false)
                return Result.success()
            }
            GoldMarket.refresh()
            val (triggered, remaining) = checkPriceAlerts(alerts)
            if (remaining.size != alerts.size) {
                persistPriceAlerts(remaining)
            }
            triggered.forEachIndexed { index, (title, body) ->
                postNotification(title, body, PRICE_ALERT_NOTIFICATION_ID_BASE + index)
            }
            if (remaining.isEmpty()) {
                PriceAlertScheduler.setActive(false)
            }
            Result.success()
        } catch (e: Exception) {
            reportSilentError("PriceAlertWorker failed: ${e.message}")
            Result.retry()
        }
    }

    private fun postNotification(title: String, body: String, notificationId: Int) {
        val context = applicationContext
        val notificationManager = NotificationManagerCompat.from(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    PRICE_ALERT_CHANNEL_ID,
                    "تنبيهات الأسعار المستهدفة",
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        // FLAG_ACTIVITY_NEW_TASK إلزامي لإطلاق Activity من سياق غير Activity
        // (عامل خلفية هنا)، وبالتزامن مع singleTask في AndroidManifest.xml
        // يضمن إحضار نفس نسخة التطبيق الحالية للمقدمة بدل نسخة جديدة تفقد حالتها
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context, notificationId, openAppIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, PRICE_ALERT_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }
}
