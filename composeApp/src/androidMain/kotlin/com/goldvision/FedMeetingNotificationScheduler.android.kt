package com.goldvision

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

private const val FED_MEETING_WORK_NAME = "gold_vision_fed_meeting_check"

// يجدول فحصاً دورياً كل 24 ساعة عبر WorkManager (نفس نمط
// PriceNotificationScheduler)، يستمر حتى لو أُغلق التطبيق تماماً
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
            val request = PeriodicWorkRequestBuilder<FedMeetingNotificationWorker>(24, TimeUnit.HOURS).build()
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
