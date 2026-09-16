package com.goldvision

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

// يبني "بطاقة السعر" كصورة Bitmap برسم يدوي مباشر (بلا Compose — تُستدعى
// من BroadcastReceiver الويدجت، سياق بلا شجرة واجهة قابلة للالتقاط أصلاً)،
// بنفس تصميم PriceShareCard المستخدم داخل التطبيق (App.kt)، حتى تبقى
// البطاقة المُشارَكة من الويدجت مطابقة بصرياً لتلك المشارَكة من داخل التطبيق
private fun buildWidgetPriceCardBitmap(context: Context): Bitmap {
    val width = 720
    val padding = 40f
    val cornerRadius = 36f

    val background = Color.parseColor("#090909")
    val border = Color.parseColor("#FFC21A")
    val gold = Color.parseColor("#FFC21A")
    val white = Color.parseColor("#F4F4F4")
    val gray = Color.parseColor("#B8B8B8")
    val green = Color.parseColor("#35D12F")
    val red = Color.parseColor("#FF3B30")
    val boxBorder = Color.parseColor("#9B7300")
    val boxBg = Color.parseColor("#050505")

    val boldTypeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    val regularTypeface = Typeface.DEFAULT

    fun paint(color: Int, size: Float, bold: Boolean = false, align: Paint.Align = Paint.Align.LEFT) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        textSize = size
        typeface = if (bold) boldTypeface else regularTypeface
        textAlign = align
    }

    val prices = GoldMarket.prices
    val featured = prices.firstOrNull { it.karat == "24K" } ?: prices.firstOrNull()
    val others = prices.filterNot { it.karat == featured?.karat }
    val dateText = widgetDateText()

    // الارتفاع يُحسب تراكمياً حسب المحتوى الفعلي بدل رقم ثابت مُقدَّر، حتى
    // يطابق تماماً بغض النظر عن عدد صفوف الأعيرة الأخرى المتاحة فعلياً
    val boxRowHeight = if (others.isNotEmpty()) 170f else 0f
    val height = (padding * 2 + 90 + 40 + 230 + 40 + boxRowHeight + 40 + 70).toInt()

    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(Color.TRANSPARENT)

    val cardRect = RectF(0f, 0f, width.toFloat(), height.toFloat())
    canvas.drawRoundRect(cardRect, cornerRadius, cornerRadius, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = background })
    canvas.drawRoundRect(
        cardRect.apply { inset(2f, 2f) },
        cornerRadius, cornerRadius,
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = border
            alpha = 140
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
    )

    fun divider(y: Float) {
        canvas.drawRect(
            padding, y, width - padding, y + 2f,
            Paint().apply { color = boxBorder }
        )
    }

    var cursorY = padding + 50f

    // رأس البطاقة: "GOLD VISION" باللون الذهبي عند الحافة اليمينية
    // (بداية السطر بالعربي)، والتاريخ عند الحافة اليسارية — بنفس ترتيب
    // Row(SpaceBetween) بالنسخة داخل التطبيق تحت اتجاه RTL
    canvas.drawText("GOLD VISION", width - padding, cursorY, paint(gold, 34f, bold = true, align = Paint.Align.RIGHT))
    canvas.drawText(dateText, padding, cursorY, paint(gray, 22f, align = Paint.Align.LEFT))

    cursorY += 40f
    divider(cursorY)
    cursorY += 90f

    if (featured != null) {
        canvas.drawText(
            t("عيار ${featured.karat.removeSuffix("K")}", "${featured.karat} Gold"),
            width / 2f, cursorY, paint(gold, 26f, bold = true, align = Paint.Align.CENTER)
        )
        cursorY += 60f

        val priceText = fmt(featured.price, 2)
        val unitText = t("ريال/جرام", "SAR/g")
        val pricePaint = paint(white, 68f, bold = true, align = Paint.Align.LEFT)
        val unitPaint = paint(gray, 26f, align = Paint.Align.LEFT)
        val gap = 14f
        val totalWidth = unitPaint.measureText(unitText) + gap + pricePaint.measureText(priceText)
        val startX = width / 2f - totalWidth / 2f
        canvas.drawText(unitText, startX, cursorY, unitPaint)
        canvas.drawText(priceText, startX + unitPaint.measureText(unitText) + gap, cursorY, pricePaint)
        cursorY += 60f

        val isUp = featured.change >= 0
        val pillColor = if (isUp) green else red
        val pillText = t(
            "${if (isUp) "▲" else "▼"} ${fmt(featured.percent, 2)}٪ اليوم",
            "${if (isUp) "▲" else "▼"} ${fmt(featured.percent, 2)}% today"
        )
        val pillPaint = paint(pillColor, 24f, bold = true, align = Paint.Align.CENTER)
        val pillTextWidth = pillPaint.measureText(pillText)
        val pillRect = RectF(
            width / 2f - pillTextWidth / 2f - 24f, cursorY - 34f,
            width / 2f + pillTextWidth / 2f + 24f, cursorY + 14f
        )
        canvas.drawRoundRect(pillRect, 30f, 30f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = pillColor; alpha = 36 })
        canvas.drawText(pillText, width / 2f, cursorY, pillPaint)
    }

    cursorY += 50f
    divider(cursorY)
    cursorY += 50f

    if (others.isNotEmpty()) {
        val boxGap = 16f
        val boxWidth = (width - padding * 2 - boxGap * (others.size - 1)) / others.size
        others.forEachIndexed { index, item ->
            val left = padding + index * (boxWidth + boxGap)
            val boxRect = RectF(left, cursorY, left + boxWidth, cursorY + 120f)
            canvas.drawRoundRect(boxRect, 20f, 20f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = boxBg })
            canvas.drawRoundRect(
                boxRect, 20f, 20f,
                Paint(Paint.ANTI_ALIAS_FLAG).apply { color = boxBorder; style = Paint.Style.STROKE; strokeWidth = 2f }
            )
            val centerX = left + boxWidth / 2f
            canvas.drawText(
                t("عيار ${item.karat.removeSuffix("K")}", item.karat),
                centerX, boxRect.top + 46f, paint(gold, 22f, bold = true, align = Paint.Align.CENTER)
            )
            canvas.drawText(
                fmt(item.price, 2),
                centerX, boxRect.top + 84f, paint(white, 28f, bold = true, align = Paint.Align.CENTER)
            )
        }
        cursorY += boxRowHeight
    }

    cursorY += 20f
    divider(cursorY)
    cursorY += 46f

    canvas.drawText(
        t("GOLD VISION · حمّل التطبيق الآن", "GOLD VISION · Download the app now"),
        width / 2f, cursorY, paint(gray, 20f, bold = true, align = Paint.Align.CENTER)
    )

    return bitmap
}

// يُستدعى من GoldPriceWidgetProvider عند ضغط زر واتساب بالويدجت: يبني
// البطاقة، يحفظها مؤقتاً بـ cacheDir (نفس آلية FileProvider المستخدمة
// فعلياً لمشاركة تقارير PDF وبطاقة السعر داخل التطبيق)، ثم يفتح واتساب
// مباشرة برسالة تحتوي الصورة — أو نافذة مشاركة عامة إن لم يكن واتساب
// مثبَّتاً على الجهاز
internal fun shareWidgetPriceCardToWhatsApp(context: Context) {
    try {
        val bitmap = buildWidgetPriceCardBitmap(context)
        val dir = File(context.cacheDir, "images").apply { mkdirs() }
        val file = File(dir, "gold_vision_widget_card_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, out) }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

        val whatsappPackage = listOf("com.whatsapp", "com.whatsapp.w4b").firstOrNull { pkg ->
            try {
                context.packageManager.getApplicationInfo(pkg, 0)
                true
            } catch (e: Exception) {
                false
            }
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (whatsappPackage != null) setPackage(whatsappPackage)
        }

        if (whatsappPackage != null) {
            context.startActivity(shareIntent)
        } else {
            Toast.makeText(context, t("واتساب غير مثبت على الجهاز", "WhatsApp isn't installed"), Toast.LENGTH_SHORT).show()
            context.startActivity(
                Intent.createChooser(shareIntent, null).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            )
        }
    } catch (e: Exception) {
        reportSilentError("shareWidgetPriceCardToWhatsApp failed: ${e.message}")
    }
}
