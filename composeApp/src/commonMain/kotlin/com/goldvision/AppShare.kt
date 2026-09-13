package com.goldvision

// يفتح نافذة المشاركة الأصلية للمنصة (واتساب/رسائل/إيميل...) بنص جاهز
// يعرّف بالتطبيق — يحتاج سياق واجهة (Activity/Composable) فبقي منفصلاً
// عن الدوال النصية البحتة، بنفس أسلوب GoogleSignInLauncher
internal expect object AppShare {
    @androidx.compose.runtime.Composable
    fun rememberShareTrigger(): () -> Unit
}

// نص المشاركة موحَّد بالكود المشترك حتى يبقى نفسه على كل منصة
internal const val appShareText =
    "جرّب تطبيق Gold Vision 💎 لتتبع أسعار الذهب لحظياً، حساب الزكاة، وتقييم صفقات الذهب بسهولة."
