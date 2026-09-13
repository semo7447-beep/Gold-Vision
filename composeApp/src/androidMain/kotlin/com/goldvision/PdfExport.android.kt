package com.goldvision

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

// ينشئ ملف PDF بجدول حقيقي (عنوان + شعار + ملخص + جدول أعمدة بحدود)
// بخط عربي واتجاه RTL صحيح عبر StaticLayout (بدل Canvas.drawText المباشر،
// الذي لا يشكّل الحروف العربية بشكل صحيح)، ثم يفتح نافذة المشاركة القياسية
// في أندرويد لحفظه أو إرساله أو طباعته مباشرة.
//
// ملاحظة مهمة: لا يجوز ضبط Paint.textAlign عند الرسم عبر StaticLayout —
// الأمران يتعارضان، فيحاول StaticLayout رسم كل جزء نصي (كل "شريحة" بلغة
// مختلفة داخل نفس السطر، كنص عربي ممزوج برقم إنجليزي) عند نفس نقطة
// الإحداثي بدل ترتيبها بجانب بعضها — وهذا بالضبط ما كان يسبب تراكب
// النصوص فوق بعضها في التقارير السابقة. المحاذاة لليمين تتم يدوياً هنا
// عبر إزاحة الـ canvas (translate) قبل رسم كل StaticLayout
internal actual object PdfExport {
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private const val PAGE_WIDTH = 595 // A4 تقريباً بوحدة نقطة (72 نقطة/إنش)
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 40f
    private val GoldColor = Color.rgb(160, 120, 20)
    private val HeaderBg = Color.rgb(32, 26, 10)
    private val ZebraBg = Color.rgb(246, 246, 246)
    private val BorderColor = Color.rgb(210, 210, 210)

    actual fun exportReport(
        title: String,
        generatedAt: String,
        summary: List<PdfReportRow>,
        rows: List<PdfReportRow>
    ) {
        val context = appContext ?: return
        try {
            val document = PdfDocument()
            var pageNumber = 1
            var page = document.startPage(
                PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            )
            var canvas = page.canvas
            var y = MARGIN

            val titlePaint = TextPaint().apply {
                color = Color.BLACK
                textSize = 16f
                isAntiAlias = true
                typeface = Typeface.DEFAULT_BOLD
            }
            val brandPaint = TextPaint(titlePaint).apply {
                textSize = 13f
                color = GoldColor
            }
            val metaPaint = TextPaint(titlePaint).apply {
                textSize = 9.5f
                color = Color.GRAY
                typeface = Typeface.DEFAULT
            }
            val labelPaint = TextPaint(titlePaint).apply {
                textSize = 10.5f
                color = Color.DKGRAY
                typeface = Typeface.DEFAULT
            }
            val valuePaint = TextPaint(labelPaint).apply {
                color = Color.BLACK
                typeface = Typeface.DEFAULT_BOLD
            }
            val headerCellPaint = TextPaint(labelPaint).apply {
                textSize = 10f
                color = Color.WHITE
                typeface = Typeface.DEFAULT_BOLD
            }
            val linePaint = Paint().apply {
                color = BorderColor
                strokeWidth = 0.75f
            }

            val contentWidth = (PAGE_WIDTH - MARGIN * 2)

            // يرسم نصاً عربياً/مختلطاً داخل صندوق بعرض ثابت، محاذى ليمين
            // الصندوق (boxRight)، عبر StaticLayout — يُعيد ارتفاع السطر
            // المرسوم فعلياً حتى يُستخدم لحساب الموضع التالي
            fun drawRtlText(text: String, paint: TextPaint, boxRight: Float, boxWidth: Int, topY: Float): Float {
                if (text.isEmpty() || boxWidth <= 0) return 0f
                val layout = StaticLayout.Builder
                    .obtain(text, 0, text.length, paint, boxWidth)
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setTextDirection(TextDirectionHeuristics.RTL)
                    .build()
                canvas.save()
                canvas.translate(boxRight - boxWidth, topY)
                layout.draw(canvas)
                canvas.restore()
                return layout.height.toFloat()
            }

            fun measureRtlHeight(text: String, paint: TextPaint, boxWidth: Int): Float {
                if (text.isEmpty() || boxWidth <= 0) return 0f
                return StaticLayout.Builder
                    .obtain(text, 0, text.length, paint, boxWidth)
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setTextDirection(TextDirectionHeuristics.RTL)
                    .build()
                    .height.toFloat()
            }

            fun ensureSpace(neededHeight: Float) {
                if (y + neededHeight > PAGE_HEIGHT - MARGIN) {
                    document.finishPage(page)
                    pageNumber += 1
                    page = document.startPage(
                        PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                    )
                    canvas = page.canvas
                    y = MARGIN
                }
            }

            // ==================== رأس التقرير: الشعار + اسم التطبيق ====================
            val logoSize = 34
            val logoBitmap = try {
                BitmapFactory.decodeResource(context.resources, R.drawable.logo_gold_vision)
                    ?.let { Bitmap.createScaledBitmap(it, logoSize, logoSize, true) }
            } catch (e: Exception) {
                null
            }
            if (logoBitmap != null) {
                canvas.drawBitmap(logoBitmap, PAGE_WIDTH - MARGIN - logoSize, y, null)
            }
            val brandBoxRight = PAGE_WIDTH - MARGIN - (if (logoBitmap != null) logoSize + 8f else 0f)
            drawRtlText("Gold Vision", brandPaint, brandBoxRight, contentWidth.toInt(), y + 9f)
            y += logoSize + 16f

            // ==================== عنوان التقرير وتاريخه ====================
            y += drawRtlText(title, titlePaint, PAGE_WIDTH - MARGIN, contentWidth.toInt(), y) + 4f
            y += drawRtlText(generatedAt, metaPaint, PAGE_WIDTH - MARGIN, contentWidth.toInt(), y) + 16f

            // ==================== قسم الملخص (عمودان: تسمية وقيمة) ====================
            if (summary.isNotEmpty()) {
                val summaryValueColWidth = 170f
                val summaryLabelColWidth = contentWidth - summaryValueColWidth
                summary.forEach { row ->
                    val rowHeight = maxOf(
                        measureRtlHeight(row.label, labelPaint, summaryLabelColWidth.toInt()),
                        measureRtlHeight(row.value, valuePaint, summaryValueColWidth.toInt())
                    )
                    ensureSpace(rowHeight + 6f)
                    drawRtlText(row.label, labelPaint, PAGE_WIDTH - MARGIN, summaryLabelColWidth.toInt(), y)
                    drawRtlText(row.value, valuePaint, MARGIN + summaryValueColWidth, summaryValueColWidth.toInt(), y)
                    y += rowHeight + 6f
                }
                y += 8f
                canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
                y += 16f
            }

            // ==================== جدول التفاصيل (بحدود وتظليل متبادل) ====================
            if (rows.isNotEmpty()) {
                val valueColWidth = 110f
                val labelColWidth = contentWidth - valueColWidth
                val tableLeft = MARGIN
                val tableRight = PAGE_WIDTH - MARGIN
                val valueColRight = tableLeft + valueColWidth
                val labelColRight = tableRight
                val cellPadH = 8f
                val cellPadV = 7f

                ensureSpace(24f + 26f)
                val headerHeight = 24f
                canvas.drawRect(tableLeft, y, tableRight, y + headerHeight, Paint().apply { color = HeaderBg })
                drawRtlText(
                    "التفاصيل", headerCellPaint, labelColRight - cellPadH,
                    (labelColWidth - cellPadH * 2).toInt(), y + 7f
                )
                drawRtlText(
                    "القيمة", headerCellPaint, valueColRight - cellPadH,
                    (valueColWidth - cellPadH * 2).toInt(), y + 7f
                )
                y += headerHeight

                rows.forEachIndexed { index, row ->
                    val labelHeight = measureRtlHeight(row.label, labelPaint, (labelColWidth - cellPadH * 2).toInt())
                    val rowHeight = maxOf(labelHeight, 12f) + cellPadV * 2

                    // لو الصف بيتجاوز الصفحة الحالية، ابدأ صفحة جديدة وأعد رسم رأس الجدول
                    if (y + rowHeight > PAGE_HEIGHT - MARGIN) {
                        document.finishPage(page)
                        pageNumber += 1
                        page = document.startPage(
                            PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                        )
                        canvas = page.canvas
                        y = MARGIN
                        canvas.drawRect(tableLeft, y, tableRight, y + headerHeight, Paint().apply { color = HeaderBg })
                        drawRtlText(
                            "التفاصيل", headerCellPaint, labelColRight - cellPadH,
                            (labelColWidth - cellPadH * 2).toInt(), y + 7f
                        )
                        drawRtlText(
                            "القيمة", headerCellPaint, valueColRight - cellPadH,
                            (valueColWidth - cellPadH * 2).toInt(), y + 7f
                        )
                        y += headerHeight
                    }

                    if (index % 2 == 1) {
                        canvas.drawRect(tableLeft, y, tableRight, y + rowHeight, Paint().apply { color = ZebraBg })
                    }
                    drawRtlText(
                        row.label, labelPaint, labelColRight - cellPadH,
                        (labelColWidth - cellPadH * 2).toInt(), y + cellPadV
                    )
                    if (row.value.isNotEmpty()) {
                        drawRtlText(
                            row.value, valuePaint, valueColRight - cellPadH,
                            (valueColWidth - cellPadH * 2).toInt(), y + cellPadV
                        )
                    }
                    // الفاصل الرأسي بين العمودين + الفاصل الأفقي أسفل الصف
                    canvas.drawLine(valueColRight, y, valueColRight, y + rowHeight, linePaint)
                    canvas.drawLine(tableLeft, y + rowHeight, tableRight, y + rowHeight, linePaint)
                    y += rowHeight
                }
            }

            document.finishPage(page)

            val fileName = "gold_vision_report_${System.currentTimeMillis()}.pdf"
            val outFile = File(context.cacheDir, fileName)
            FileOutputStream(outFile).use { document.writeTo(it) }
            document.close()

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", outFile)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(shareIntent, title).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            reportSilentError("PdfExport.exportReport failed: ${e.message}")
        }
    }
}
