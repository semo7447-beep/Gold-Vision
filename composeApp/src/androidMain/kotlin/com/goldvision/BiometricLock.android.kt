package com.goldvision

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.goldvision.resources.Res
import com.goldvision.resources.logo_gold_vision
import org.jetbrains.compose.resources.painterResource

private val LockBlack = Color(0xFF050505)
private val LockGold = Color(0xFFFFC21A)
private val LockWhite = Color(0xFFF4F4F4)
private val LockGray = Color(0xFFB8B8B8)

@Composable
internal actual fun BiometricAuthGate(enabled: Boolean, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    // إذا كان الجهاز بلا بصمة/وجه ولا حتى رمز قفل شاشة، لا يوجد أي طريقة
    // فعلية يفتح بها المستخدم قفله بنفسه — نتركه يدخل مباشرة بدل حبسه
    // خارج تطبيقه هو نفسه
    val canUseLock = remember(enabled, activity) {
        if (!enabled || activity == null) {
            false
        } else {
            val manager = BiometricManager.from(activity)
            val result = manager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_WEAK or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            result == BiometricManager.BIOMETRIC_SUCCESS
        }
    }

    if (!canUseLock) {
        content()
        return
    }

    var unlocked by remember { mutableStateOf(false) }
    if (unlocked) {
        content()
        return
    }

    fun promptUnlock() {
        val fragmentActivity = activity ?: return
        val executor = ContextCompat.getMainExecutor(fragmentActivity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                unlocked = true
            }
        }
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("فتح Gold Vision")
            .setSubtitle("استخدم بصمتك أو وجهك أو رمز الجهاز لفتح التطبيق")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_WEAK or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()
        BiometricPrompt(fragmentActivity, executor, callback).authenticate(promptInfo)
    }

    // يطلب فتح القفل تلقائياً فور ظهور الشاشة، بدل انتظار ضغطة أولى —
    // نفس سلوك التطبيقات البنكية المعتادة
    LaunchedEffect(Unit) {
        promptUnlock()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LockBlack),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Image(
                painter = painterResource(Res.drawable.logo_gold_vision),
                contentDescription = null,
                modifier = Modifier.size(72.dp)
            )
            Spacer(Modifier.height(24.dp))
            Icon(
                imageVector = Icons.Outlined.Fingerprint,
                contentDescription = null,
                tint = LockGold,
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text("التطبيق مقفل", color = LockWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("افتحه ببصمتك أو وجهك أو رمز الجهاز", color = LockGray, fontSize = 11.sp)
            Spacer(Modifier.height(28.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .background(LockGold, RoundedCornerShape(10.dp))
                    .clickable { promptUnlock() },
                contentAlignment = Alignment.Center
            ) {
                Text("فتح القفل", color = LockBlack, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
