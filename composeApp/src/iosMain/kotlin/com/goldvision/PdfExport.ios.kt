package com.goldvision

// تصدير PDF غير مفعّل بعد على iOS — بلا تأثير حالياً
internal actual object PdfExport {
    actual fun exportReport(title: String, generatedAt: String, summary: List<PdfReportRow>, rows: List<PdfReportRow>) {
    }
}
