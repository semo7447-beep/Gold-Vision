package com.goldvision

// تصدير المحفظة أو تقرير الزكاة كملف PDF ومشاركته مباشرة (حفظ/طباعة/إرسال)
// عبر نافذة المشاركة القياسية في النظام — التطبيق الفعلي مختلف لكل منصة
// (PdfDocument على أندرويد؛ لا تأثير على iOS بعد)
internal data class PdfReportRow(val label: String, val value: String)

internal expect object PdfExport {
    fun exportReport(title: String, generatedAt: String, summary: List<PdfReportRow>, rows: List<PdfReportRow>)
}
