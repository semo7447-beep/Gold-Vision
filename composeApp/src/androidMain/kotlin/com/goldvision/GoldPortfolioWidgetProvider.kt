package com.goldvision

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit

private const val PORTFOLIO_WIDGET_UPDATE_WORK_NAME = "gold_vision_portfolio_widget_update"
private const val PortfolioGreen = 0xFF35D12F.toInt()
private const val PortfolioRed = 0xFFFF3B30.toInt()

// ويدجت ثانٍ منفصل يعرض قيمة المحفظة الحية بدل الأسعار — نفس نمط
// GoldPriceWidgetProvider.kt بالضبط (AppWidgetProvider/RemoteViews قياسي)
internal class GoldPortfolioWidgetProvider : AppWidgetProvider() {
    companion object {
        const val ACTION_REFRESH = "com.goldvision.widget.ACTION_REFRESH_PORTFOLIO"
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            WorkManager.getInstance(context).enqueue(OneTimeWorkRequestBuilder<GoldPortfolioWidgetWorker>().build())
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { appWidgetId ->
            appWidgetManager.updateAppWidget(appWidgetId, buildPortfolioWidgetRemoteViews(context))
        }
        WorkManager.getInstance(context).enqueue(OneTimeWorkRequestBuilder<GoldPortfolioWidgetWorker>().build())
    }

    override fun onEnabled(context: Context) {
        val request = PeriodicWorkRequestBuilder<GoldPortfolioWidgetWorker>(30, TimeUnit.MINUTES).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PORTFOLIO_WIDGET_UPDATE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    override fun onDisabled(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(PORTFOLIO_WIDGET_UPDATE_WORK_NAME)
    }
}

// نفس معادلة GoldItem.currentValue() في App.kt (لا يمكن استدعاؤها مباشرة
// لأنها private)، مكرَّرة هنا محلياً لهذا الويدجت فقط
private fun goldItemCurrentValue(item: GoldItem): Double {
    val pricePerGram = GoldMarket.prices.firstOrNull { it.karat == item.karat }?.price ?: return 0.0
    val beforeVat = pricePerGram * item.weightGrams
    val manufacturingValue = item.manufacturingPerGram * item.weightGrams
    val subtotal = beforeVat + manufacturingValue
    val vat = if (item.karat == "24K") 0.0 else subtotal * 0.15
    return subtotal + vat
}

internal fun buildPortfolioWidgetRemoteViews(context: Context): RemoteViews {
    val views = RemoteViews(context.packageName, R.layout.gold_portfolio_widget)

    val items = try {
        AppStorage.readText(goldItemsStorageFile)?.let { Json.decodeFromString<List<GoldItem>>(it) }
    } catch (e: Exception) {
        null
    } ?: emptyList()

    val ownedItems = items.filter { !it.isSold }
    val totalValue = ownedItems.sumOf { goldItemCurrentValue(it) }
    val totalCost = ownedItems.sumOf { it.purchasePriceWithTax }
    val changePercent = if (totalCost > 0.0) (totalValue - totalCost) / totalCost * 100.0 else 0.0

    views.setTextViewText(R.id.portfolio_widget_label, t("قيمة المحفظة", "Portfolio Value"))
    views.setTextViewText(R.id.portfolio_widget_value, "${fmt(totalValue, 2, grouped = true)} ${t("ريال", "SAR")}")
    if (ownedItems.isEmpty()) {
        views.setTextViewText(R.id.portfolio_widget_percent, t("لا توجد قطع بالمحفظة بعد", "No items in portfolio yet"))
        views.setTextColor(R.id.portfolio_widget_percent, Color.parseColor("#B8B8B8"))
    } else {
        val arrow = if (changePercent >= 0) "▲" else "▼"
        views.setTextViewText(R.id.portfolio_widget_percent, "$arrow ${fmt(changePercent, 2)}%")
        views.setTextColor(R.id.portfolio_widget_percent, if (changePercent >= 0) PortfolioGreen else PortfolioRed)
    }
    views.setTextViewText(R.id.portfolio_widget_updated_at, widgetUpdatedAtText())

    val openAppIntent = Intent(context, MainActivity::class.java)
    val pendingIntent = PendingIntent.getActivity(
        context, 1, openAppIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    views.setOnClickPendingIntent(R.id.portfolio_widget_root, pendingIntent)

    val refreshIntent = Intent(context, GoldPortfolioWidgetProvider::class.java).apply {
        action = GoldPortfolioWidgetProvider.ACTION_REFRESH
    }
    val refreshPendingIntent = PendingIntent.getBroadcast(
        context, 1, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    views.setOnClickPendingIntent(R.id.portfolio_widget_refresh, refreshPendingIntent)

    return views
}
