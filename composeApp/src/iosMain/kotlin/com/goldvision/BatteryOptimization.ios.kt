package com.goldvision

// لا يوجد "تحسين بطارية" بمفهوم أندرويد على iOS — بلا تأثير حالياً
internal actual object BatteryOptimization {
    actual val isExempted: Boolean = true
    actual fun openExemptionSettings() {
    }
}
