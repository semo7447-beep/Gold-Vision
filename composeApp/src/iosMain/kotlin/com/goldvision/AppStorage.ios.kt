package com.goldvision

import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDomainMask
import platform.Foundation.stringWithContentsOfFile
import platform.Foundation.writeToFile

// ⚠️ لم يُتَح اختبار هذا الملف فعلياً (يحتاج Xcode على جهاز Mac، غير
// متوفر في هذه البيئة) — يُصرَّف منطقياً بنفس أسلوب iosMain الحالي، لكن
// يحتاج تأكيداً حقيقياً عند إضافة هدف iOS للمشروع
actual object AppStorage {
    private fun documentsPath(fileName: String): String {
        val paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true)
        val documentsDirectory = paths.firstOrNull() as? String ?: ""
        return "$documentsDirectory/$fileName"
    }

    actual fun readText(fileName: String): String? {
        return try {
            NSString.stringWithContentsOfFile(
                documentsPath(fileName),
                encoding = NSUTF8StringEncoding,
                error = null
            ) as String?
        } catch (e: Exception) {
            null
        }
    }

    actual fun writeText(fileName: String, content: String) {
        (content as NSString).writeToFile(
            documentsPath(fileName),
            atomically = true,
            encoding = NSUTF8StringEncoding,
            error = null
        )
    }
}
