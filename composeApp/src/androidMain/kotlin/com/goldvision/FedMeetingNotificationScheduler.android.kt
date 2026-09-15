package com.goldvision

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

private const val FED_MEETING_WORK_NAME = "gold_vision_fed_meeting_check"

// يجدول فحصاً دورياً كل 24 ساعة عبر WorkManager (نفس نمط
// PriceNotificationScheduler)، يستمر حتى لو أُغلق التطبيق تماماً — بتأخير
// بدء أولي (setInitialDelay) يضبط أول تشغيل على الساعة 9:00 مساءً بتوقيت
// مكة بالضبط (نفس موعد إعلان القرار)، فتبقى كل الدورات التالية (كل 24
// ساعة من هذه النقطة) عند نفس الساعة يومياً. هذا يضمن إشعارين فعليين
// بدل واحد فقط: تذكير عند الفحص اليوم السابق للاجتماع (daysLeft == 1)،
// ثم إشعار آخر عند 9:00 مساءً يوم الاجتماع نفسه (daysLeft == 0) — بدل
// الاعتماد على وقت تفعيل المستخدم للميزة (عشوائي وقد يكون في الصباح)
internal actual object FedMeetingNotificationScheduler {
    private var appContext: Context? = null

    // يُستدعى من GoldVisionApplication.onCreate، بنفس نمط AppStorage.init
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    actual fun setEnabled(enabled: Boolean) {
        val context = appContext ?: return
        val workManager = WorkManager.getInstance(context)
        if (enabled) {
            val request = PeriodicWorkRequestBuilder<FedMeetingNotificationWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(millisUntilNextMecca9PM(), TimeUnit.MILLISECONDS)
                .build()
            workManager.enqueueUniquePeriodicWork(
                FED_MEETING_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        } else {
            workManager.cancelUniqueWork(FED_MEETING_WORK_NAME)
        }
    }
}

private fun millisUntilNextMecca9PM(): Long {
    val zone = TimeZone.of("Asia/Riyadh")
    val now = Clock.System.now()
    val nowLocal = now.toLocalDateTime(zone)
    val targetDate = if (nowLocal.hour < 21) nowLocal.date else nowLocal.date.plus(1, DateTimeUnit.DAY)
    val targetInstant = LocalDateTime(targetDate.year, targetDate.monthNumber, targetDate.dayOfMonth, 21, 0, 0)
        .toInstant(zone)
    return (targetInstant - now).inWholeMilliseconds.coerceAtLeast(0L)
}
