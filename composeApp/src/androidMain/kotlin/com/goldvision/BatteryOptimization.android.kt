package com.goldvision

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings

internal actual object BatteryOptimization {
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    actual val isExempted: Boolean
        get() {
            val context = appContext ?: return true
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return true
            return powerManager.isIgnoringBatteryOptimizations(context.packageName)
        }

    actual fun openExemptionSettings() {
        val context = appContext ?: return
        try {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // بعض الأجهزة (خصوصاً MIUI ببعض إصداراتها) قد ترفض هذا الـIntent
            // القياسي — نفتح شاشة تفاصيل التطبيق العامة كبديل احتياطي، حتى
            // يقدر المستخدم الوصول لإعدادات البطارية يدوياً من هناك
            try {
                val fallback = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallback)
            } catch (e2: Exception) {
                reportSilentError("BatteryOptimization.openExemptionSettings failed: ${e2.message}")
            }
        }
    }
}
