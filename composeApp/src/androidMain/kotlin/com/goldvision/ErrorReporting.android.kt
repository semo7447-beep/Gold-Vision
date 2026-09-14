package com.goldvision

import android.util.Log
import io.sentry.Sentry

actual fun reportSilentError(message: String) {
    // يُطبع أيضاً في Logcat (بجانب Sentry) — يتيح تشخيص فوري أثناء التشغيل
    // من أندرويد ستوديو مباشرة بدل الاعتماد على لوحة Sentry فقط
    Log.e("GoldVisionSilentError", message)
    Sentry.captureMessage(message)
}
