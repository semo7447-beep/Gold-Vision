package com.goldvision

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

internal actual object PriceCardShare {
    // يلتقط البطاقة بالاعتماد على الـ View المضيفة لشجرة Compose كاملة
    // (رسم الشاشة الحالية إلى Bitmap ثم قصّ منطقة البطاقة فقط حسب موضعها
    // بالنافذة) — بديل يعمل مع أي إصدار Compose، بدل واجهة GraphicsLayer
    // الأحدث (rememberGraphicsLayer) غير المتوفرة بعد بإصدار Compose
    // Multiplatform المثبَّت بالمشروع (1.7.3)
    @Composable
    actual fun rememberCapture(): PriceCardCapture {
        val view = LocalView.current
        var bounds by remember { mutableStateOf(Rect()) }
        return remember(view) {
            PriceCardCapture(
                modifier = Modifier.onGloballyPositioned { coordinates ->
                    val position = coordinates.positionInWindow()
                    bounds = Rect(
                        position.x.toInt(),
                        position.y.toInt(),
                        (position.x + coordinates.size.width).toInt(),
                        (position.y + coordinates.size.height).toInt()
                    )
                },
                capture = capture@{
                    try {
                        val full = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
                        view.draw(Canvas(full))
                        val safe = Rect(bounds)
                        if (!safe.intersect(Rect(0, 0, full.width, full.height)) ||
                            safe.width() <= 0 || safe.height() <= 0
                        ) {
                            return@capture null
                        }
                        Bitmap.createBitmap(full, safe.left, safe.top, safe.width(), safe.height()).asImageBitmap()
                    } catch (e: Exception) {
                        reportSilentError("PriceCardCapture.capture failed: ${e.message}")
                        null
                    }
                }
            )
        }
    }

    @Composable
    actual fun rememberShareImage(): (ImageBitmap) -> Unit {
        val context = LocalContext.current
        return { imageBitmap ->
            try {
                val dir = File(context.cacheDir, "images").apply { mkdirs() }
                val file = File(dir, "gold_vision_price_${System.currentTimeMillis()}.png")
                FileOutputStream(file).use { out ->
                    imageBitmap.asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(
                    Intent.createChooser(shareIntent, null).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                )
            } catch (e: Exception) {
                reportSilentError("PriceCardShare.share failed: ${e.message}")
            }
        }
    }

    @Composable
    actual fun rememberSaveImage(): (ImageBitmap) -> Boolean {
        val context = LocalContext.current
        return { imageBitmap ->
            try {
                val fileName = "gold_vision_price_${System.currentTimeMillis()}.png"
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Gold Vision")
                    }
                }
                val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        imageBitmap.asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                // على أندرويد الأقدم من 10 (Q) قد يفشل هذا بلا صلاحية
                // WRITE_EXTERNAL_STORAGE — نُخفق بهدوء بدل تعقيد التطبيق
                // بتدفّق صلاحيات كامل لميزة اختيارية على شريحة أجهزة نادرة جداً اليوم
                reportSilentError("PriceCardShare.save failed: ${e.message}")
                false
            }
        }
    }
}
