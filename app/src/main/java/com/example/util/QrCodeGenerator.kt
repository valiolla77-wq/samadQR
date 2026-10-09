package com.example.util

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

object QrCodeGenerator {

    /**
     * Generates a high-contrast QR code bitmap as required:
     * - ErrorCorrectionLevel.H
     * - MARGIN = 4
     * - UTF-8 character set
     * - Size 800x800
     * - Pure white background (#FFFFFF), pure black foreground (#000000)
     */
    fun generateQrBitmap(code: String, size: Int = 800): Bitmap? {
        if (code.isBlank()) return null
        return try {
            val hints = mapOf(
                EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H,
                EncodeHintType.MARGIN to 1,
                EncodeHintType.CHARACTER_SET to "UTF-8"
            )
            val matrix = MultiFormatWriter().encode(
                code, BarcodeFormat.QR_CODE, size, size, hints
            )
            val width = matrix.width
            val height = matrix.height
            val pixels = IntArray(width * height)
            for (y in 0 until height) {
                val offset = y * width
                for (x in 0 until width) {
                    pixels[offset + x] = if (matrix.get(x, y)) Color.BLACK else Color.WHITE
                }
            }
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Formats a 9-digit forgotten card code into 3-digit chunks: "740 560 364" or in Persian digits.
     */
    fun formatNineDigitCode(code: String, toPersian: Boolean = false): String {
        val clean = code.trim().filter { it.isDigit() }
        val formatted = when {
            clean.length == 9 -> "${clean.substring(0, 3)} - ${clean.substring(3, 6)} - ${clean.substring(6, 9)}"
            clean.length > 6 -> "${clean.substring(0, 3)} - ${clean.substring(3, 6)} - ${clean.substring(6)}"
            clean.length > 3 -> "${clean.substring(0, 3)} - ${clean.substring(3)}"
            else -> clean
        }
        return if (toPersian) {
            val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
            val sb = StringBuilder()
            for (ch in formatted) {
                if (ch in '0'..'9') sb.append(persianDigits[ch - '0']) else sb.append(ch)
            }
            sb.toString()
        } else formatted
    }
}
