package com.goldvision

import android.content.Context
import android.content.Intent
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

// ينشئ ملف PDF بسيطاً (عنوان + ملخص + جدول صفوف) بخط عربي واتجاه RTL
// صحيح عبر StaticLayout (بدل Canvas.drawText المباشر، الذي لا يشكّل
// الحروف العربية بشكل صحيح)، ثم يفتح نافذة المشاركة القياسية في أندرويد
// لحفظه أو إرساله أو طباعته مباشرة
internal actual object PdfExport {
    private var appContext: Context? = null

    // يُستدعى من GoldVisionApplication.onCreate، بنفس نمط AppStorage.init
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private const val PAGE_WIDTH = 595 // A4 تقريباً بوحدة نقطة (72 نقطة/إنش)
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 40f

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
                textSize = 20f
                isAntiAlias = true
                typeface = Typeface.DEFAULT_BOLD
                textAlign = Paint.Align.RIGHT
            }
            val metaPaint = TextPaint(titlePaint).apply {
                textSize = 10f
                color = Color.DKGRAY
                typeface = Typeface.DEFAULT
            }
            val labelPaint = TextPaint(metaPaint).apply {
                textSize = 12f
                color = Color.DKGRAY
            }
            val valuePaint = TextPaint(labelPaint).apply {
                color = Color.BLACK
                typeface = Typeface.DEFAULT_BOLD
            }
            val availableWidth = (PAGE_WIDTH - MARGIN * 2).toInt()

            fun drawRtlLine(text: String, paint: TextPaint, xRight: Float, topY: Float): Float {
                val layout = StaticLayout.Builder
                    .obtain(text, 0, text.length, paint, availableWidth)
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setTextDirection(TextDirectionHeuristics.RTL)
                    .build()
                canvas.save()
                canvas.translate(xRight - availableWidth, topY)
                layout.draw(canvas)
                canvas.restore()
                return topY + layout.height
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

            y = drawRtlLine(title, titlePaint, PAGE_WIDTH - MARGIN, y) + 6f
            y = drawRtlLine(generatedAt, metaPaint, PAGE_WIDTH - MARGIN, y) + 18f

            if (summary.isNotEmpty()) {
                summary.forEach { row ->
                    ensureSpace(22f)
                    val rowText = "${row.label}:  ${row.value}"
                    y = drawRtlLine(rowText, valuePaint, PAGE_WIDTH - MARGIN, y) + 4f
                }
                y += 14f
                canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, Paint().apply {
                    color = Color.LTGRAY
                    strokeWidth = 1f
                })
                y += 16f
            }

            rows.forEach { row ->
                ensureSpace(30f)
                val rowText = if (row.value.isNotEmpty()) "${row.label}  —  ${row.value}" else row.label
                y = drawRtlLine(rowText, labelPaint, PAGE_WIDTH - MARGIN, y) + 8f
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
