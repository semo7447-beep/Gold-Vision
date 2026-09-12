package com.goldvision

// تسجيل الدخول بالإيميل غير مفعّل بعد على iOS (يحتاج Firebase iOS SDK
// عبر CocoaPods/SPM، لم يُضَف بعد) — بلا تأثير حالياً
internal actual object AuthService {
    actual val currentUserEmail: String? = null

    actual suspend fun signUp(email: String, password: String): String? =
        "تسجيل الدخول غير مفعّل على iOS بعد"

    actual suspend fun signIn(email: String, password: String): String? =
        "تسجيل الدخول غير مفعّل على iOS بعد"

    actual suspend fun sendPasswordReset(email: String): String? =
        "غير مفعّل على iOS بعد"

    actual fun signOut() {
    }
}
