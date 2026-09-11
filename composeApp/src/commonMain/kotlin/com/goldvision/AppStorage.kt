package com.goldvision

// تخزين ملف نصي على جهاز المستخدم فقط — لا سحابة ولا خادم، بلا أي
// مزامنة أو مشاركة. يُستخدم لحفظ قطع المحفظة/الزكاة بين جلسات التطبيق.
// التطبيق الفعلي مختلف لكل منصة (يحتاج مساراً حقيقياً على القرص):
// androidMain يستخدم Context.filesDir، iosMain يستخدم مجلد Documents
expect object AppStorage {
    fun readText(fileName: String): String?
    fun writeText(fileName: String, content: String)
}
