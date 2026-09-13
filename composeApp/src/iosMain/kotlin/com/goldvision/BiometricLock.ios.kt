package com.goldvision

import androidx.compose.runtime.Composable

// قفل التطبيق ببصمة/وجه غير مفعّل بعد على iOS — يعرض المحتوى مباشرة
@Composable
internal actual fun BiometricAuthGate(enabled: Boolean, content: @Composable () -> Unit) {
    content()
}
