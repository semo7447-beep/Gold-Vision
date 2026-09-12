package com.goldvision

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
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
        e.message ?: "تعذر إنشاء الحساب"
    }

    actual suspend fun signIn(email: String, password: String): String? = try {
        auth.signInWithEmailAndPassword(email, password).awaitResult()
        null
    } catch (e: Exception) {
        e.message ?: "تعذر تسجيل الدخول"
    }

    actual suspend fun sendPasswordReset(email: String): String? = try {
        auth.sendPasswordResetEmail(email).awaitResult()
        null
    } catch (e: Exception) {
        e.message ?: "تعذر إرسال رابط استعادة كلمة المرور"
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
