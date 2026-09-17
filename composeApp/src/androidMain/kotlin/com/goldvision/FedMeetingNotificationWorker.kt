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

private const val FED_MEETING_CHANNEL_ID = "gold_vision_fed_meeting"
private const val FED_MEETING_NOTIFICATION_ID = 1002

// يعمل حتى لو كان التطبيق مغلقاً تماماً (مُشغَّل من WorkManager): يفحص
// يومياً أقرب موعد اقتصادي مهم (اجتماع الفيدرالي، أو NFP/CPI/Core
// PCE/GDP)، ويُصدر إشعار تذكير فقط إن كان الموعد بعد أسبوع بالضبط أو
// غداً أو اليوم (بدل إشعار يومي دائم لا فائدة منه أغلب أيام السنة)
internal class FedMeetingNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val (title, body) = buildFedMeetingNotificationText() ?: return Result.success()
            postNotification(title, body)
            Result.success()
        } catch (e: Exception) {
            reportSilentError("FedMeetingNotificationWorker failed: ${e.message}")
            Result.retry()
        }
    }

    private fun postNotification(title: String, body: String) {
        val context = applicationContext
        val notificationManager = NotificationManagerCompat.from(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    FED_MEETING_CHANNEL_ID,
                    "مواعيد اجتماعات الفيدرالي",
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

        // FLAG_ACTIVITY_NEW_TASK إلزامي لإطلاق Activity من سياق غير Activity
        // (عامل خلفية هنا)، وبالتزامن مع singleTask في AndroidManifest.xml
        // يضمن إحضار نفس نسخة التطبيق الحالية للمقدمة بدل نسخة جديدة تفقد حالتها
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context, FED_MEETING_NOTIFICATION_ID, openAppIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, FED_MEETING_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(FED_MEETING_NOTIFICATION_ID, notification)
    }
}
