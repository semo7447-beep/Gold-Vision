package com.goldvision

// مزامنة المحفظة السحابية غير مفعّلة بعد على iOS — بلا تأثير حالياً
internal actual object PortfolioSync {
    actual suspend fun upload(itemsJson: String, updatedAtMillis: Long) {
    }

    actual suspend fun download(): Pair<String, Long>? = null
}
