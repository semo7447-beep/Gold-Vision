package com.goldvision

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File

// يكتب نص JSON لملف مؤقت بذاكرة التخزين المؤقت للتطبيق، ثم يفتح نافذة
// المشاركة القياسية في أندرويد (نفس أسلوب PdfExport بالضبط) حتى يحفظه
// المستخدم بجوجل درايف أو يرسله لنفسه بواتساب/إيميل كنسخة احتياطية
internal actual object PortfolioBackupExport {
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    actual fun exportBackup(jsonContent: String) {
        val context = appContext ?: return
        try {
            val fileName = "gold_vision_backup_${System.currentTimeMillis()}.json"
            val outFile = File(context.cacheDir, fileName)
            outFile.writeText(jsonContent)

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", outFile)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(
                Intent.createChooser(shareIntent, t("حفظ النسخة الاحتياطية", "Save backup")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
        } catch (e: Exception) {
            reportSilentError("PortfolioBackupExport.exportBackup failed: ${e.message}")
        }
    }
}

// ActivityResultContracts.OpenDocument بنفس أسلوب GoogleSignInLauncher —
// يعمل مباشرة من أي Composable عبر LocalContext.current بلا حاجة لأي
// تعديل بـMainActivity
internal actual object PortfolioBackupImport {
    @Composable
    actual fun rememberLauncher(onResult: (jsonContent: String?, error: String?) -> Unit): () -> Unit {
        val context = LocalContext.current
        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri ->
            if (uri == null) {
                // المستخدم أغلق منتقي الملفات بنفسه — ليس خطأ فعلياً
                onResult(null, null)
                return@rememberLauncherForActivityResult
            }
            try {
                val text = context.contentResolver.openInputStream(uri)
                    ?.use { it.readBytes().decodeToString() }
                if (text.isNullOrBlank()) {
                    onResult(null, t("تعذرت قراءة الملف", "Could not read the file"))
                } else {
                    onResult(text, null)
                }
            } catch (e: Exception) {
                onResult(null, t("تعذرت قراءة الملف", "Could not read the file"))
            }
        }
        return {
            launcher.launch(arrayOf("application/json", "text/*", "*/*"))
        }
    }
}
