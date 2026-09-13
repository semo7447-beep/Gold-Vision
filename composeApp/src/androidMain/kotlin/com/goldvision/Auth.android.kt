package com.goldvision

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
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
        // رابط تأكيد البريد يُرسل تلقائياً عند إنشاء الحساب — لا يمنع استخدام
        // التطبيق إن فشل الإرسال لأي سبب (لا يوجد اتصال مثلاً)
        try {
            auth.currentUser?.sendEmailVerification()?.awaitResult()
        } catch (e: Exception) {
            // تجاهل: الحساب أُنشئ بنجاح بغض النظر عن نجاح إرسال رابط التأكيد
        }
        null
    } catch (e: Exception) {
        e.message ?: t("تعذر إنشاء الحساب", "Couldn't create account")
    }

    actual suspend fun signIn(email: String, password: String): String? = try {
        auth.signInWithEmailAndPassword(email, password).awaitResult()
        null
    } catch (e: Exception) {
        e.message ?: t("تعذر تسجيل الدخول", "Couldn't sign in")
    }

    actual suspend fun sendPasswordReset(email: String): String? = try {
        auth.sendPasswordResetEmail(email).awaitResult()
        null
    } catch (e: Exception) {
        e.message ?: t("تعذر إرسال رابط استعادة كلمة المرور", "Couldn't send the password reset link")
    }

    actual suspend fun completeGoogleSignIn(idToken: String): String? = try {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).awaitResult()
        null
    } catch (e: Exception) {
        e.message ?: t("تعذر تسجيل الدخول بحساب جوجل", "Couldn't sign in with Google")
    }

    actual fun signOut() {
        auth.signOut()
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
