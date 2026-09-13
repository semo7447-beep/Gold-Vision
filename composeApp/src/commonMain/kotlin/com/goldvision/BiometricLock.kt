package com.goldvision

import androidx.compose.runtime.Composable

// يعرض شاشة قفل تطلب بصمة/وجه/رمز الجهاز قبل عرض محتوى التطبيق، إذا كان
// المستخدم فعّل هذا الخيار بنفسه — التطبيق الفعلي مختلف لكل منصة
// (BiometricPrompt على أندرويد؛ لا تأثير على iOS بعد، يعرض المحتوى مباشرة)
@Composable
internal expect fun BiometricAuthGate(enabled: Boolean, content: @Composable () -> Unit)
