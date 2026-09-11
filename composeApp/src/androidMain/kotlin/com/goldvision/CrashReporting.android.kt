package com.goldvision

import io.sentry.Sentry

actual fun sendTestCrashReport() {
    Sentry.captureException(RuntimeException("اختبار Sentry من تطبيق Gold Vision"))
}
