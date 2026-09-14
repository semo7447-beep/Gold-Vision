package com.goldvision

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.EmailAuthProvider
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

    actual suspend fun sendEmailVerification(): String? = try {
        val user = auth.currentUser
            ?: return t("لا يوجد مستخدم مسجَّل دخوله حالياً", "No signed-in user")
        user.sendEmailVerification().awaitResult()
        null
    } catch (e: Exception) {
        e.message ?: t("تعذر إرسال رابط التأكيد", "Couldn't send the confirmation link")
    }

    actual suspend fun completeGoogleSignIn(idToken: String): String? = try {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).awaitResult()
        null
    } catch (e: Exception) {
        e.message ?: t("تعذر تسجيل الدخول بحساب جوجل", "Couldn't sign in with Google")
    }

    actual suspend fun linkPasswordToCurrentUser(password: String): String? = try {
        val user = auth.currentUser
            ?: return t("لا يوجد مستخدم مسجَّل دخوله حالياً", "No signed-in user")
        val email = user.email
            ?: return t("تعذر العثور على بريد الحساب الحالي", "Couldn't find the current account's email")
        val credential = EmailAuthProvider.getCredential(email, password)
        user.linkWithCredential(credential).awaitResult()
        null
    } catch (e: Exception) {
        e.message ?: t("تعذر حفظ كلمة المرور", "Couldn't save the password")
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
