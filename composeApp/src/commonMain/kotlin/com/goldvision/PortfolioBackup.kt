package com.goldvision

import androidx.compose.runtime.Composable

// يصدّر نسخة احتياطية من محفظة الذهب المحفوظة محلياً كملف JSON قابل
// للمشاركة (حفظ بجوجل درايف، إرسال لنفسه بواتساب/إيميل...) — حماية
// للبيانات من الضياع عند فقدان الجهاز أو حذف التطبيق، لمن لا يستخدم
// تسجيل الدخول (الذي يزامن سحابياً تلقائياً بدلاً عن هذا أصلاً)
internal expect object PortfolioBackupExport {
    fun exportBackup(jsonContent: String)
}

// يفتح منتقي الملفات القياسي للمنصة لاختيار ملف نسخة احتياطية JSON
// سابق وإعادة قراءته — النتيجة نص خام فقط؛ فك التشفير (JSON) إلى
// GoldItem والتحقق من صحته يتمّان بالكود المشترك، بنفس أسلوب
// GoogleSignInLauncher (لا منطق خاص بـGoldItem داخل كود المنصة)
internal expect object PortfolioBackupImport {
    @Composable
    fun rememberLauncher(onResult: (jsonContent: String?, error: String?) -> Unit): () -> Unit
}
