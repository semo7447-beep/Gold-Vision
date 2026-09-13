package com.goldvision

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

// يبحث عن معرّف عميل الويب (default_web_client_id) وقت التشغيل بدل
// الإشارة له مباشرة كمورد ثابت (R.string.default_web_client_id) — هذا
// المورد يُنشئه إضافة google-services تلقائياً من google-services.json
// فقط بعد تفعيل "تسجيل الدخول بجوجل" من Firebase Console وتحديث الملف؛
// البحث وقت التشغيل يمنع فشل بناء كامل التطبيق لو لم يُفعَّل هذا بعد
internal actual object GoogleSignInLauncher {
    @Composable
    actual fun rememberLauncher(onResult: (idToken: String?, error: String?) -> Unit): () -> Unit {
        val context = LocalContext.current

        val webClientId = remember {
            val resId = context.resources.getIdentifier(
                "default_web_client_id", "string", context.packageName
            )
            if (resId != 0) context.getString(resId) else null
        }

        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode != Activity.RESULT_OK) {
                // المستخدم أغلق نافذة اختيار الحساب بنفسه — ليس خطأ فعلياً
                onResult(null, null)
                return@rememberLauncherForActivityResult
            }
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                if (idToken == null) {
                    onResult(null, "تعذر الحصول على بيانات حساب جوجل")
                } else {
                    onResult(idToken, null)
                }
            } catch (e: ApiException) {
                onResult(null, "تعذر تسجيل الدخول بحساب جوجل")
            }
        }

        return {
            if (webClientId == null) {
                onResult(null, "الدخول بحساب جوجل غير مفعّل بعد لهذا التطبيق")
            } else {
                val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(webClientId)
                    .requestEmail()
                    .build()
                val client = GoogleSignIn.getClient(context, options)
                // يضمن ظهور نافذة اختيار الحساب دائماً (بدل دخول صامت بنفس
                // الحساب المستخدم آخر مرة) حتى يقدر المستخدم يبدّل حسابه
                client.signOut()
                launcher.launch(client.signInIntent)
            }
        }
    }
}
