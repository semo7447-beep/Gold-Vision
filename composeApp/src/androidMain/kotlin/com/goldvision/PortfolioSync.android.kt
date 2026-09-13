package com.goldvision

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

// مزامنة المحفظة عبر Firestore، تحت مستند خاص بكل مستخدم (uid) — لا يقدر
// أي مستخدم آخر الوصول لمستند غيره (يحتاج قاعدة أمان Firestore مطابقة،
// راجع تعليمات ملحقة). يُخزَّن نفس نص JSON المستخدم محلياً أصلاً بدل بنية
// مستندات منفصلة لكل قطعة، لتبسيط المزامنة (استبدال كامل بدل دمج جزئي)
internal actual object PortfolioSync {
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private fun portfolioDocRef() =
        FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
            firestore.collection("users").document(uid).collection("data").document("portfolio")
        }

    actual suspend fun upload(itemsJson: String, updatedAtMillis: Long) {
        val docRef = portfolioDocRef() ?: return
        val data = mapOf("itemsJson" to itemsJson, "updatedAt" to updatedAtMillis)
        docRef.set(data).awaitResult()
    }

    actual suspend fun download(): Pair<String, Long>? {
        val docRef = portfolioDocRef() ?: return null
        val snapshot = docRef.get().awaitResult()
        if (!snapshot.exists()) return null
        val itemsJson = snapshot.getString("itemsJson") ?: return null
        val updatedAt = snapshot.getLong("updatedAt") ?: return null
        return itemsJson to updatedAt
    }
}

private suspend fun <T> Task<T>.awaitResult(): T =
    suspendCancellableCoroutine { cont ->
        addOnCompleteListener { task ->
            if (task.isSuccessful) {
                cont.resume(task.result)
            } else {
                cont.resumeWithException(task.exception ?: Exception("Unknown Firestore error"))
            }
        }
    }
