package com.goldvision

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

private const val DAILY_PRICE_WORK_NAME = "gold_vision_daily_price_notification"

// يجدول عملاً دورياً كل 24 ساعة عبر WorkManager يستمر حتى لو أُغلق
// التطبيق تماماً (بعكس أي مؤقّت داخل التطبيق نفسه فقط، يتوقف بمجرد إغلاقه)
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
            val request = PeriodicWorkRequestBuilder<DailyPriceNotificationWorker>(24, TimeUnit.HOURS).build()
            workManager.enqueueUniquePeriodicWork(
                DAILY_PRICE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        } else {
            workManager.cancelUniqueWork(DAILY_PRICE_WORK_NAME)
        }
    }
}
