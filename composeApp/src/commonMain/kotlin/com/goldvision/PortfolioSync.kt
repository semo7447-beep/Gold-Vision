package com.goldvision

// مزامنة سحابية اختيارية للمحفظة عبر الحساب المسجَّل بالإيميل (Firebase
// Firestore) — تعمل فقط لو كان المستخدم مسجّل دخول؛ من لم يسجّل دخول
// تبقى محفظته محلية بالكامل كما كانت دائماً. عند تعارض، تُفضَّل النسخة
// الأحدث تعديلاً (بالطابع الزمني) بغض النظر عن مصدرها — التطبيق الفعلي
// مختلف لكل منصة (Firestore على أندرويد؛ لا تأثير على iOS بعد)
internal expect object PortfolioSync {
    suspend fun upload(itemsJson: String, updatedAtMillis: Long)
    suspend fun download(): Pair<String, Long>?
}

private const val portfolioUpdatedAtFile = "portfolio_updated_at.txt"

internal fun loadPortfolioUpdatedAt(): Long =
    AppStorage.readText(portfolioUpdatedAtFile)?.toLongOrNull() ?: 0L

internal fun persistPortfolioUpdatedAt(millis: Long) {
    AppStorage.writeText(portfolioUpdatedAtFile, millis.toString())
}
