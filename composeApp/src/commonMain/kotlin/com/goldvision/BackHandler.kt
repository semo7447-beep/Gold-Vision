package com.goldvision

import androidx.compose.runtime.Composable

// يربط زر/إيماءة الرجوع في نظام التشغيل (زر فعلي، زر على الشاشة، أو
// إيماءة السحب من الحافة) بالتنقل داخل التطبيق، حتى تعمل تلقائياً بلا
// حاجة لضغط زر الرجوع داخل التطبيق نفسه — التطبيق الفعلي مختلف لكل
// منصة (نظام أندرويد الموحَّد للزر/الإيماءة؛ بلا تأثير على iOS بعد)
@Composable
expect fun BackHandler(enabled: Boolean, onBack: () -> Unit)
