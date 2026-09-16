package com.goldvision

// مراقبة الاتصال الفعلية غير مفعّلة بعد على iOS — تبقى دائماً "متصل"
// (بلا تأثير)، بنفس نمط PdfExport.ios.kt وPortfolioBackup.ios.kt
internal actual object NetworkMonitor {
    actual val isOnline: Boolean = true
}
