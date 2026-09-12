package com.goldvision

// تسجيل دخول حقيقي بالإيميل/كلمة المرور عبر Firebase Authentication —
// مجاني بالكامل على خطة Spark بلا حد أقصى لعدد المستخدمين. اختياري
// تماماً: التطبيق يعمل بكامل ميزاته بدون تسجيل دخول (الملف الشخصي
// المحلي يبقى قائماً بذاته)، والحساب هنا إضافة اختيارية فقط. غير مفعّل
// بعد على iOS (يحتاج Firebase iOS SDK عبر CocoaPods/SPM، لم يُضَف بعد)
internal expect object AuthService {
    // بريد المستخدم المسجّل دخوله حالياً، أو null إن لم يسجّل دخول
    val currentUserEmail: String?

    // كل دالة ترجع null عند النجاح، أو رسالة خطأ نصية جاهزة للعرض عند الفشل
    suspend fun signUp(email: String, password: String): String?
    suspend fun signIn(email: String, password: String): String?
    suspend fun sendPasswordReset(email: String): String?
    fun signOut()
}

private val emailRegex = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

internal fun isValidEmail(text: String): Boolean = emailRegex.matches(text.trim())
