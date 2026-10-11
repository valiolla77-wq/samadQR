package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.local.entity.CachedReserveEntity
import java.io.File
import java.io.FileOutputStream

object ExportHelper {

    /**
     * Shares a single reservation barcode image and summary text
     */
    fun shareSingleReserve(
        context: Context,
        reserve: CachedReserveEntity,
        qrBitmap: Bitmap?,
        studentName: String?,
        studentNumber: String?
    ) {
        try {
            val code = reserve.forgotCardCode ?: ""
            val digits = code.filter { it.isDigit() }
            val c1 = if (digits.length >= 3) digits.substring(0, 3) else digits
            val c2 = if (digits.length >= 6) digits.substring(3, 6) else (if (digits.length > 3) digits.substring(3) else "")
            val c3 = if (digits.length >= 9) digits.substring(6, 9) else (if (digits.length > 6) digits.substring(6) else "")
            val formattedCode = if (digits.length == 9) "$c1 - $c2 - $c3" else digits

            val shareText = buildString {
                appendLine("🍽️ ژتون غذای دانشگاه (سامانه سماد)")
                appendLine("━━━━━━━━━━━━━━━━━━━")
                if (!studentName.isNullOrBlank()) {
                    appendLine("👤 دانشجو: $studentName")
                }
                if (!studentNumber.isNullOrBlank()) {
                    appendLine("🎓 شماره دانشجویی: $studentNumber")
                }
                appendLine("🍛 وعده: ${reserve.mealName}")
                appendLine("🍲 غذا: ${reserve.foodName}")
                if (reserve.besideFoodNames.isNotBlank()) {
                    appendLine("🥗 مخلفات: ${reserve.besideFoodNames}")
                }
                appendLine("📅 تاریخ: ${reserve.dayName} ${reserve.dateJStr.ifBlank { reserve.date }}")
                if (reserve.selfName.isNotBlank()) {
                    appendLine("🏢 سلف: ${reserve.selfName}")
                }
                if (digits.isNotBlank()) {
                    // Prepend Left-to-Right mark (\u200E) to prevent RTL reverse display
                    appendLine("🔢 کد فراموشی کارت: \u200E$formattedCode")
                    appendLine("📋 کد بدون فاصله: \u200E$digits")
                }
                appendLine("━━━━━━━━━━━━━━━━━━━")
                appendLine("📱 ایجاد شده با اپلیکیشن سماد یار")
            }

            var imageUri: Uri? = null
            if (qrBitmap != null) {
                val cacheDir = File(context.cacheDir, "shared_qr")
                if (!cacheDir.exists()) cacheDir.mkdirs()
                val imageFile = File(cacheDir, "qr_${reserve.reserveId}.png")
                FileOutputStream(imageFile).use { out ->
                    qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                imageUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    imageFile
                )
            }

            val intent = Intent(Intent.ACTION_SEND).apply {
                if (imageUri != null) {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, imageUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } else {
                    type = "text/plain"
                }
                putExtra(Intent.EXTRA_TEXT, shareText)
                putExtra(Intent.EXTRA_SUBJECT, "ژتون غذای ${reserve.mealName} - ${reserve.dayName}")
            }

            val chooser = Intent.createChooser(intent, "اشتراک‌گذاری ژتون غذا")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "خطا در اشتراک‌گذاری: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Shares a group of reservations (e.g. all reserves of a day or entire week)
     */
    fun shareGroupReserves(
        context: Context,
        reserves: List<CachedReserveEntity>,
        title: String,
        studentName: String?,
        studentNumber: String?
    ) {
        if (reserves.isEmpty()) {
            Toast.makeText(context, "رزروی برای اشتراک‌گذاری وجود ندارد", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val shareText = buildString {
                appendLine("📋 لیست ژتون‌های غذای دانشگاه (سامانه سماد)")
                appendLine("━━━━━━━━━━━━━━━━━━━")
                if (!studentName.isNullOrBlank()) {
                    appendLine("👤 فرستنده: $studentName")
                }
                if (!studentNumber.isNullOrBlank()) {
                    appendLine("🎓 شماره دانشجویی: $studentNumber")
                }
                appendLine("📅 دوره: $title (${reserves.size} وعده رزرو شده)")
                appendLine("💡 راهنما: گیرنده می‌تواند کد فراموشی هر وعده را کپی کرده یا در اپلیکیشن سماد وارد کند تا بارکد اسکن سلف فوراً تولید شود.")
                appendLine("━━━━━━━━━━━━━━━━━━━")

                reserves.sortedBy { it.date }.forEachIndexed { index, r ->
                    val code = r.forgotCardCode ?: ""
                    val digits = code.filter { it.isDigit() }
                    val c1 = if (digits.length >= 3) digits.substring(0, 3) else digits
                    val c2 = if (digits.length >= 6) digits.substring(3, 6) else (if (digits.length > 3) digits.substring(3) else "")
                    val c3 = if (digits.length >= 9) digits.substring(6, 9) else (if (digits.length > 6) digits.substring(6) else "")
                    val formattedCode = if (digits.length == 9) "$c1 - $c2 - $c3" else digits

                    appendLine("${index + 1}. ${r.dayName} ${r.dateJStr.ifBlank { r.date }} | ${r.mealName}")
                    appendLine("   🍲 ${r.foodName}")
                    if (r.besideFoodNames.isNotBlank()) {
                        appendLine("   🥗 مخلفات: ${r.besideFoodNames}")
                    }
                    if (digits.isNotBlank()) {
                        appendLine("   🔢 کد فراموشی سلف: \u200E$formattedCode")
                        appendLine("   📋 کد مستقیم جهت اسکن: \u200E$digits")
                    } else {
                        appendLine("   ⚠️ کد فراموشی هنوز صادر نشده")
                    }
                    if (r.selfName.isNotBlank()) {
                        appendLine("   🏢 سلف: ${r.selfName}")
                    }
                    appendLine("-------------------")
                }
                appendLine("📱 ایجاد شده با اپلیکیشن سماد یار")
            }

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
                putExtra(Intent.EXTRA_SUBJECT, "ژتون‌های غذای دانشگاه - $title")
            }

            val chooser = Intent.createChooser(intent, "اشتراک‌گذاری در پیام‌رسان‌ها")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "خطا در اشتراک‌گذاری: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Exports week reservations to a beautifully designed Card-based PDF document with large QR codes
     */
    fun exportWeekReservesToPdf(
        context: Context,
        reserves: List<CachedReserveEntity>,
        weekTitle: String,
        studentName: String?,
        studentNumber: String?,
        universityName: String?
    ) {
        if (reserves.isEmpty()) {
            Toast.makeText(context, "رزروی برای صدور PDF وجود ندارد", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val sortedReserves = reserves.sortedBy { it.date }
            val pdfDoc = PdfDocument()

            // Standard A4: 595 x 842 points
            val pageWidth = 595
            val pageHeight = 842

            val cardsPerPage = 4
            val totalPages = (sortedReserves.size + cardsPerPage - 1) / cardsPerPage

            var currentReserveIndex = 0

            for (pageNumber in 1..totalPages) {
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                val page = pdfDoc.startPage(pageInfo)
                val canvas = page.canvas

                // Background
                val bgPaint = Paint().apply {
                    color = Color.rgb(248, 250, 252) // Slate 50
                    style = Paint.Style.FILL
                }
                canvas.drawRect(0f, 0f, pageWidth.toFloat(), pageHeight.toFloat(), bgPaint)

                // Header Card
                val headerPaint = Paint().apply {
                    color = Color.rgb(15, 23, 42) // Slate 900
                    style = Paint.Style.FILL
                }
                canvas.drawRoundRect(RectF(24f, 20f, pageWidth - 24f, 85f), 14f, 14f, headerPaint)

                val titlePaint = Paint().apply {
                    color = Color.rgb(245, 158, 11) // Amber 500
                    textSize = 15f
                    isFakeBoldText = true
                    textAlign = Paint.Align.RIGHT
                }
                canvas.drawText("ژتون‌های غذای هفتگی دانشگاه • سامانه سماد یار", pageWidth - 42f, 46f, titlePaint)

                val subTitlePaint = Paint().apply {
                    color = Color.rgb(226, 232, 240) // Slate 200
                    textSize = 10f
                    textAlign = Paint.Align.RIGHT
                }
                val infoStr = "دانشجو: ${studentName ?: "نامشخص"}  |  شماره دانشجویی: ${studentNumber ?: "نامشخص"}  |  $weekTitle"
                canvas.drawText(infoStr, pageWidth - 42f, 68f, subTitlePaint)

                var cardY = 98f
                val cardHeight = 160f
                val cardWidth = pageWidth - 48f // 547f
                val cardSpacing = 14f

                val cardBgPaint = Paint().apply {
                    color = Color.WHITE
                    style = Paint.Style.FILL
                }
                val cardBorderPaint = Paint().apply {
                    color = Color.rgb(203, 213, 225) // Slate 300
                    style = Paint.Style.STROKE
                    strokeWidth = 1.2f
                }
                val dividerPaint = Paint().apply {
                    color = Color.rgb(226, 232, 240)
                    strokeWidth = 1f
                }

                // Text Paints
                val badgeTextPaint = Paint().apply {
                    textSize = 10.5f
                    isFakeBoldText = true
                    textAlign = Paint.Align.CENTER
                }
                val datePaint = Paint().apply {
                    color = Color.rgb(30, 41, 59)
                    textSize = 12f
                    isFakeBoldText = true
                    textAlign = Paint.Align.RIGHT
                }
                val foodTitlePaint = Paint().apply {
                    color = Color.rgb(15, 23, 42)
                    textSize = 13.5f
                    isFakeBoldText = true
                    textAlign = Paint.Align.RIGHT
                }
                val detailPaint = Paint().apply {
                    color = Color.rgb(71, 85, 105)
                    textSize = 10f
                    textAlign = Paint.Align.RIGHT
                }
                val codeLabelPaint = Paint().apply {
                    color = Color.rgb(100, 116, 139)
                    textSize = 8.5f
                    textAlign = Paint.Align.CENTER
                }
                val codeTextPaint = Paint().apply {
                    color = Color.rgb(180, 83, 9) // Amber 700
                    textSize = 11.5f
                    isFakeBoldText = true
                    textAlign = Paint.Align.CENTER
                }
                val statusTagPaint = Paint().apply {
                    color = Color.rgb(5, 150, 105) // Emerald 600
                    textSize = 9f
                    isFakeBoldText = true
                    textAlign = Paint.Align.RIGHT
                }

                // Render up to 4 cards on this page
                for (i in 0 until cardsPerPage) {
                    if (currentReserveIndex >= sortedReserves.size) break
                    val reserve = sortedReserves[currentReserveIndex]

                    val cardRect = RectF(24f, cardY, 24f + cardWidth, cardY + cardHeight)
                    canvas.drawRoundRect(cardRect, 14f, 14f, cardBgPaint)
                    canvas.drawRoundRect(cardRect, 14f, 14f, cardBorderPaint)

                    // LEFT SECTION: Large QR Barcode and 9-digit code
                    val code = reserve.forgotCardCode ?: ""
                    val digits = code.filter { it.isDigit() }
                    val c1 = if (digits.length >= 3) digits.substring(0, 3) else digits
                    val c2 = if (digits.length >= 6) digits.substring(3, 6) else (if (digits.length > 3) digits.substring(3) else "")
                    val c3 = if (digits.length >= 9) digits.substring(6, 9) else (if (digits.length > 6) digits.substring(6) else "")
                    val formattedCode = if (digits.length == 9) "$c1 - $c2 - $c3" else digits

                    // Inner container for QR
                    val qrBoxRect = RectF(38f, cardY + 12f, 154f, cardY + 128f)
                    val qrBoxPaint = Paint().apply {
                        color = Color.rgb(248, 250, 252)
                        style = Paint.Style.FILL
                    }
                    canvas.drawRoundRect(qrBoxRect, 8f, 8f, qrBoxPaint)

                    val qrTargetRect = RectF(44f, cardY + 18f, 148f, cardY + 122f)
                    if (code.isNotBlank()) {
                        val qrBitmap = QrCodeGenerator.generateQrBitmap(code, 500)
                        if (qrBitmap != null) {
                            canvas.drawBitmap(qrBitmap, null, qrTargetRect, null)
                        }
                    } else {
                        // Placeholder
                        val placeholderPaint = Paint().apply {
                            color = Color.rgb(148, 163, 184)
                            textSize = 9.5f
                            textAlign = Paint.Align.CENTER
                        }
                        canvas.drawText("کد صادر نشده", 96f, cardY + 74f, placeholderPaint)
                    }

                    // 9-digit Code under QR
                    canvas.drawText("کد فراموشی سلف", 96f, cardY + 139f, codeLabelPaint)
                    val codeStr = if (formattedCode.isNotBlank()) "\u200E$formattedCode" else "—"
                    canvas.drawText(codeStr, 96f, cardY + 152f, codeTextPaint)

                    // Vertical Divider
                    canvas.drawLine(170f, cardY + 12f, 170f, cardY + cardHeight - 12f, dividerPaint)

                    // RIGHT SECTION: Details
                    val rightEdge = pageWidth - 42f

                    // Meal Type Pill Badge (e.g. ناهار / شام)
                    val isDinner = reserve.mealName.contains("شام")
                    val isBreakfast = reserve.mealName.contains("صبحانه")
                    val badgeBgColor = when {
                        isDinner -> Color.rgb(224, 231, 255)
                        isBreakfast -> Color.rgb(209, 250, 229)
                        else -> Color.rgb(254, 243, 199)
                    }
                    val badgeTextColor = when {
                        isDinner -> Color.rgb(67, 56, 202)
                        isBreakfast -> Color.rgb(4, 120, 87)
                        else -> Color.rgb(180, 83, 9)
                    }
                    val badgePaint = Paint().apply {
                        color = badgeBgColor
                        style = Paint.Style.FILL
                    }
                    badgeTextPaint.color = badgeTextColor

                    val badgeRect = RectF(rightEdge - 64f, cardY + 14f, rightEdge, cardY + 36f)
                    canvas.drawRoundRect(badgeRect, 6f, 6f, badgePaint)
                    canvas.drawText(reserve.mealName, rightEdge - 32f, cardY + 29f, badgeTextPaint)

                    // Day & Date (Left of badge)
                    val dayDateStr = "📅 ${reserve.dayName} ${reserve.dateJStr.ifBlank { reserve.date }}"
                    canvas.drawText(dayDateStr, rightEdge - 76f, cardY + 29f, datePaint)

                    // Food Name (Prominent)
                    val foodStr = "🍲 غذا: ${reserve.foodName}"
                    canvas.drawText(foodStr, rightEdge, cardY + 62f, foodTitlePaint)

                    // Beside foods (Side dishes)
                    val besideStr = "🥗 مخلفات: ${reserve.besideFoodNames.ifBlank { "مخلفات استاندارد سلف" }}"
                    canvas.drawText(besideStr, rightEdge, cardY + 86f, detailPaint)

                    // Dining Hall / Cafeteria
                    val selfStr = "🏢 سلف سرویس: ${reserve.selfName.ifBlank { "سلف مرکزی دانشگاه" }}"
                    canvas.drawText(selfStr, rightEdge, cardY + 108f, detailPaint)

                    // Price & Verification
                    val priceStr = if (reserve.price > 0) "مبلغ: ${reserve.price} ریال" else "ژتون معتبر دانشجویی"
                    val verifyStr = "✓ دارای مجوز سرو در سلف  •  $priceStr"
                    canvas.drawText(verifyStr, rightEdge, cardY + 134f, statusTagPaint)

                    cardY += cardHeight + cardSpacing
                    currentReserveIndex++
                }

                // Footer
                val footerPaint = Paint().apply {
                    color = Color.rgb(148, 163, 184)
                    textSize = 8.5f
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText(
                    "صفحه $pageNumber از $totalPages  •  بارکدها دارای رزولوشن بالا و آماده اسکن مستقیم در سلف دانشگاه می‌باشند",
                    (pageWidth / 2).toFloat(),
                    pageHeight - 16f,
                    footerPaint
                )

                pdfDoc.finishPage(page)
            }

            // Save PDF to cache
            val cacheDir = File(context.cacheDir, "shared_pdf")
            if (!cacheDir.exists()) cacheDir.mkdirs()
            val pdfFile = File(cacheDir, "samad_food_cards_${System.currentTimeMillis()}.pdf")
            FileOutputStream(pdfFile).use { out ->
                pdfDoc.writeTo(out)
            }
            pdfDoc.close()

            val pdfUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, pdfUri)
                putExtra(Intent.EXTRA_SUBJECT, "ژتون‌های کارتی غذای دانشگاه - $weekTitle")
                putExtra(Intent.EXTRA_TEXT, "فایل PDF ژتون‌های کارتی رزرو غذای هفته ($weekTitle)")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(intent, "اشتراک‌گذاری یا چاپ PDF ژتون‌ها")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            Toast.makeText(context, "فایل PDF کارتی با موفقیت آماده شد", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "خطا در ساخت PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }
}
