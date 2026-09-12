package com.goldvision

import androidx.compose.runtime.Composable

// لا يوجد مفهوم زر/إيماءة رجوع موحَّد بهذا الشكل على iOS — بلا تأثير حالياً
@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
}
