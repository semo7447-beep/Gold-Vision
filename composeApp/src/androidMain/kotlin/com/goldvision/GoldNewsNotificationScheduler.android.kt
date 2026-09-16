package com.goldvision

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

private const val GOLD_NEWS_WORK_NAME = "gold_vision_news_notification"

// يجدول عملاً دورياً كل 30 دقيقة عبر WorkManager (نفس نمط
// PriceNotificationScheduler)، يستمر حتى لو أُغلق التطبيق تماماً — 30
// دقيقة أدنى فاصل زمني ممكن أصلاً لعمل دوري بـWorkManager
internal actual object GoldNewsNotificationScheduler {
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    actual fun setEnabled(enabled: Boolean) {
        val context = appContext ?: return
        val workManager = WorkManager.getInstance(context)
        if (enabled) {
            val request = PeriodicWorkRequestBuilder<GoldNewsNotificationWorker>(30, TimeUnit.MINUTES).build()
            workManager.enqueueUniquePeriodicWork(
                GOLD_NEWS_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        } else {
            workManager.cancelUniqueWork(GOLD_NEWS_WORK_NAME)
        }
    }
}
