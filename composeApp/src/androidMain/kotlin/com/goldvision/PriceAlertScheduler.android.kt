package com.goldvision

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

private const val PRICE_ALERT_WORK_NAME = "gold_vision_price_alert_check"

// يجدول فحصاً دورياً كل 15 دقيقة (أقل مدة تسمح بها WorkManager) طالما فيه
// تنبيه سعر واحد نشط على الأقل — نفس نمط PriceNotificationScheduler
internal actual object PriceAlertScheduler {
    private var appContext: Context? = null

    // يُستدعى من GoldVisionApplication.onCreate، بنفس نمط AppStorage.init
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    actual fun setActive(active: Boolean) {
        val context = appContext ?: return
        val workManager = WorkManager.getInstance(context)
        if (active) {
            val request = PeriodicWorkRequestBuilder<PriceAlertWorker>(15, TimeUnit.MINUTES).build()
            workManager.enqueueUniquePeriodicWork(
                PRICE_ALERT_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        } else {
            workManager.cancelUniqueWork(PRICE_ALERT_WORK_NAME)
        }
    }
}
