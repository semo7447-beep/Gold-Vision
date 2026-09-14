package com.goldvision

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap

// حاوية بسيطة تربط بين Modifier يُعلَّق على العنصر المطلوب التقاطه، ودالة
// تُنفِّذ الالتقاط الفعلي إلى صورة — الالتقاط نفسه يحتاج تفاصيل خاصة
// بكل منصة (View على أندرويد)، فبقي منفصلاً عن الدوال المشتركة البحتة
internal data class PriceCardCapture(val modifier: Modifier, val capture: () -> ImageBitmap?)

// يلتقط بطاقة السعر اليومي (App.kt -> SharePriceCardScreen) كصورة،
// ثم يشاركها/يحفظها — يحتاج سياق واجهة (Activity/معرض الصور) فبقي
// منفصلاً عن الدوال البحتة، بنفس أسلوب AuthService
internal expect object PriceCardShare {
    @Composable
    fun rememberCapture(): PriceCardCapture

    @Composable
    fun rememberShareImage(): (ImageBitmap) -> Unit

    @Composable
    fun rememberSaveImage(): (ImageBitmap) -> Boolean
}
