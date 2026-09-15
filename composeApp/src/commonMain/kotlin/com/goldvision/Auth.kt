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
    // تُرسل لمستخدم مسجَّل دخوله حديثاً (بعد signUp) — منفصلة عن signUp حتى
    // تُعرَض نتيجتها الحقيقية للمستخدم بدل افتراض نجاحها دائماً
    suspend fun sendEmailVerification(): String?
    // تُستدعى بعد نجاح شاشة اختيار حساب جوجل نفسها (يديرها GoogleSignInLauncher)،
    // بالرمز (idToken) الناتج، لإكمال تسجيل الدخول فعلياً عبر Firebase
    suspend fun completeGoogleSignIn(idToken: String): String?
    fun signOut()
    // يحذف الحساب نهائياً (بيانات المحفظة السحابية + حساب Firebase Auth
    // نفسه) — إلزامي وجوده حسب سياسة جوجل بلاي لأي تطبيق فيه تسجيل حساب.
    // لا يمسّ البيانات المحلية على الجهاز (تبقى كما هي، التطبيق يعمل
    // بدونها). قد يفشل برسالة تطلب تسجيل دخول حديث إن مضى وقت طويل على
    // آخر تسجيل دخول (قيد أمان من Firebase نفسه)
    suspend fun deleteAccount(): String?
}

// يبني ويطلق واجهة "الدخول بحساب جوجل" الأصلية للمنصة (نافذة اختيار
// حساب جوجل)، ويُعيد رمز الدخول (idToken) عند النجاح لتمريره لاحقاً إلى
// AuthService.completeGoogleSignIn — منفصل عن AuthService لأنه يحتاج
// سياق واجهة (Activity/Composable)، بعكس بقية دوال المصادقة النصية البحتة
internal expect object GoogleSignInLauncher {
    @androidx.compose.runtime.Composable
    fun rememberLauncher(onResult: (idToken: String?, error: String?) -> Unit): () -> Unit
}

private val emailRegex = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

internal fun isValidEmail(text: String): Boolean = emailRegex.matches(text.trim())
