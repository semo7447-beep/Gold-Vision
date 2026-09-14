package com.goldvision

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal actual object AuthService {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    actual val currentUserEmail: String?
        get() = auth.currentUser?.email

    actual suspend fun signUp(email: String, password: String): String? = try {
        auth.createUserWithEmailAndPassword(email, password).awaitResult()
        null
    } catch (e: Exception) {
        localizedAuthError(e, "تعذر إنشاء الحساب", "Couldn't create account")
    }

    actual suspend fun signIn(email: String, password: String): String? = try {
        auth.signInWithEmailAndPassword(email, password).awaitResult()
        null
    } catch (e: Exception) {
        localizedAuthError(e, "تعذر تسجيل الدخول", "Couldn't sign in")
    }

    actual suspend fun sendPasswordReset(email: String): String? = try {
        auth.sendPasswordResetEmail(email).awaitResult()
        null
    } catch (e: Exception) {
        localizedAuthError(e, "تعذر إرسال رابط استعادة كلمة المرور", "Couldn't send the password reset link")
    }

    actual suspend fun sendEmailVerification(): String? = try {
        val user = auth.currentUser
            ?: return t("لا يوجد مستخدم مسجَّل دخوله حالياً", "No signed-in user")
        user.sendEmailVerification().awaitResult()
        null
    } catch (e: Exception) {
        localizedAuthError(e, "تعذر إرسال رابط التأكيد", "Couldn't send the confirmation link")
    }

    actual suspend fun completeGoogleSignIn(idToken: String): String? = try {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).awaitResult()
        null
    } catch (e: Exception) {
        localizedAuthError(e, "تعذر تسجيل الدخول بحساب جوجل", "Couldn't sign in with Google")
    }

    actual fun signOut() {
        auth.signOut()
    }
}

// رسائل Firebase الجاهزة تصل دائماً بالإنجليزية بغض النظر عن لغة التطبيق
// (e.message)، فنستبدلها برسالة مترجمة حسب رمز الخطأ الفعلي (errorCode) —
// بدل استخدامها مباشرة كما كان يحصل سابقاً (e.message ?: ...، وe.message
// كان دائماً غير null فعلياً فلا تُستخدم الترجمة أبداً)
private fun localizedAuthError(e: Exception, fallbackAr: String, fallbackEn: String): String {
    val code = (e as? FirebaseAuthException)?.errorCode
    return when (code) {
        "ERROR_EMAIL_ALREADY_IN_USE", "ERROR_CREDENTIAL_ALREADY_IN_USE" ->
            t("البريد الإلكتروني مستخدم بالفعل بحساب آخر", "This email is already in use by another account")
        "ERROR_INVALID_EMAIL" ->
            t("صيغة البريد الإلكتروني غير صحيحة", "Invalid email address format")
        "ERROR_WEAK_PASSWORD" ->
            t("كلمة المرور ضعيفة جداً، اختر كلمة أقوى", "Password is too weak — choose a stronger one")
        "ERROR_WRONG_PASSWORD", "ERROR_INVALID_CREDENTIAL" ->
            t("البريد الإلكتروني أو كلمة المرور غير صحيحة", "Incorrect email or password")
        "ERROR_USER_NOT_FOUND" ->
            t("لا يوجد حساب بهذا البريد الإلكتروني", "No account found with this email")
        "ERROR_USER_DISABLED" ->
            t("هذا الحساب معطَّل", "This account has been disabled")
        "ERROR_TOO_MANY_REQUESTS" ->
            t("محاولات كثيرة متتالية، حاول لاحقاً", "Too many attempts — try again later")
        "ERROR_NETWORK_REQUEST_FAILED" ->
            t("تحقق من اتصالك بالإنترنت", "Check your internet connection")
        else -> t(fallbackAr, fallbackEn)
    }
}

// يحوّل Task<T> الخاص بـ Google Play Services (ترجعها كل دوال Firebase)
// إلى دالة suspend عادية، بدل الاعتماد على مكتبة kotlinx-coroutines-play-services
// إضافية لمجرد هذا التحويل
private suspend fun <T> Task<T>.awaitResult(): T =
    suspendCancellableCoroutine { cont ->
        addOnCompleteListener { task ->
            if (task.isSuccessful) {
                cont.resume(task.result)
            } else {
                cont.resumeWithException(task.exception ?: Exception("Unknown Firebase error"))
            }
        }
    }
