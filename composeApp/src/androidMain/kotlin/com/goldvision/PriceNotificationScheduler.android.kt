package com.goldvision

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

private const val DAILY_PRICE_WORK_NAME = "gold_vision_daily_price_notification"
private const val WEEKLY_OPEN_CLOSE_WORK_NAME = "gold_vision_weekly_open_close_notification"

// يجدول عملين دوريين عبر WorkManager يستمران حتى لو أُغلق التطبيق تماماً
// (بعكس أي مؤقّت داخل التطبيق نفسه فقط، يتوقف بمجرد إغلاقه): إشعار كل
// ساعة بسعر الجرام الحالي، وإشعار كل أسبوع بسعري الافتتاح والإغلاق —
// الاثنان يتبعان نفس تفضيل "سعر الذهب اليومي"
internal actual object PriceNotificationScheduler {
    private var appContext: Context? = null

    // يُستدعى من GoldVisionApplication.onCreate، بنفس نمط AppStorage.init
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    actual fun setEnabled(enabled: Boolean) {
        val context = appContext ?: return
        val workManager = WorkManager.getInstance(context)
        if (enabled) {
            val hourlyRequest = PeriodicWorkRequestBuilder<DailyPriceNotificationWorker>(1, TimeUnit.HOURS).build()
            workManager.enqueueUniquePeriodicWork(
                DAILY_PRICE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                hourlyRequest
            )
            val weeklyRequest = PeriodicWorkRequestBuilder<WeeklyOpenCloseNotificationWorker>(7, TimeUnit.DAYS).build()
            workManager.enqueueUniquePeriodicWork(
                WEEKLY_OPEN_CLOSE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                weeklyRequest
            )
        } else {
            workManager.cancelUniqueWork(DAILY_PRICE_WORK_NAME)
            workManager.cancelUniqueWork(WEEKLY_OPEN_CLOSE_WORK_NAME)
        }
    }
}
