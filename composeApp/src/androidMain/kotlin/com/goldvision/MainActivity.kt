package com.goldvision

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color as AndroidColor
import android.os.Build
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

private const val ACTION_SHARE_APP = "com.goldvision.action.SHARE_APP"

// FragmentActivity (بدل ComponentActivity العادية) مطلوبة لعمل BiometricPrompt
// (قفل التطبيق ببصمة/وجه) — هي نفسها ComponentActivity مع دعم إضافي للـ Fragments
class MainActivity : FragmentActivity() {
    // مطلوبة من أندرويد 13 فأعلى فقط حتى تظهر إشعارات سعر الذهب اليومي
    // (إن فعّلها المستخدم من "المزيد ← الإشعارات")؛ تُطلب مرة واحدة عند
    // بدء التطبيق، ولا تمنع استخدامه لو رُفضت
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    // ⏳ تذكير: بعد الانتهاء من تصميم التطبيق (حسب طلب صاحب الحساب)، فعّل
    // FLAG_SECURE هنا (window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, ...))
    // لمنع لقطات الشاشة وتسجيلها نهائياً — إجراء أمان بنكي معياري، لكنه
    // يمنع أيضاً أخذ لقطات شاشة للتطبيق نفسه لأي غرض (حتى الإبلاغ عن مشاكل)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.BLACK),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.BLACK)
        )
        requestNotificationPermissionIfNeeded()
        handleShareAppShortcut(intent)
        setContent {
            App()
        }
    }

    // singleTask يعني إعادة استخدام نفس النسخة عند فتح الاختصار والتطبيق
    // مفتوح أصلاً بالخلفية — onCreate لا يُستدعى وقتها، فقط onNewIntent
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleShareAppShortcut(intent)
    }

    // يُشغَّل عند فتح اختصار "مشاركة التطبيق" من الضغط المطول على أيقونة
    // التطبيق (res/xml/shortcuts.xml) — يفتح نافذة المشاركة القياسية فوراً
    private fun handleShareAppShortcut(intent: Intent?) {
        if (intent?.action == ACTION_SHARE_APP) {
            shareAppText(this)
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
