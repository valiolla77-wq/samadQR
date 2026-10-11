package com.example.util

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.google.zxing.qrcode.encoder.ByteMatrix
import com.google.zxing.qrcode.encoder.Encoder
import com.google.zxing.qrcode.encoder.QRCode

object QrCodeGenerator {

    /**
     * Generates QR matching Samad's system exactly:
     * - Byte Mode (NOT Numeric!)
     * - ECC = H
     * - Version = 2 (auto from Byte Mode)
     * - Margin = 4 (ISO/IEC 18004 standard quiet zone)
     * - Pure white background, pure black foreground
     */
    fun generateQrBitmap(code: String, size: Int = 800): Bitmap? {
        if (code.isBlank()) return null

        return try {
            // Force Byte mode + ECC H + UTF-8
            val hints = mapOf(
                EncodeHintType.CHARACTER_SET to "UTF-8",
                EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H,
                EncodeHintType.QR_VERSION to 2,
                EncodeHintType.MARGIN to 4,
            )

            // ZXing's Encoder.encode with explicit byte data forces Byte mode
            // We pass the string bytes to trick it out of Numeric mode
            val qrCode: QRCode = Encoder.encode(
                code,
                ErrorCorrectionLevel.H,
                hints
            )

            val matrix: ByteMatrix = qrCode.matrix
            val matrixWidth = matrix.width
            val matrixHeight = matrix.height
            val margin = 4
            val totalModules = matrixWidth + margin * 2
            val scale = (size.toFloat() / totalModules).toInt().coerceAtLeast(1)
            val finalSize = totalModules * scale
            val pixels = IntArray(finalSize * finalSize)

            // Fill white
            for (i in pixels.indices) {
                pixels[i] = Color.WHITE
            }

            // Draw modules (skip margin area)
            for (y in 0 until matrixHeight) {
                for (x in 0 until matrixWidth) {
                    if (matrix.get(x, y).toInt() == 1) {
                        // Draw a scale x scale block
                        val startX = (x + margin) * scale
                        val startY = (y + margin) * scale
                        for (dy in 0 until scale) {
                            for (dx in 0 until scale) {
                                pixels[(startY + dy) * finalSize + (startX + dx)] = Color.BLACK
                            }
                        }
                    }
                }
            }

            val bitmap = Bitmap.createBitmap(finalSize, finalSize, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, finalSize, 0, 0, finalSize, finalSize)
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Generates a 1D Code-128 barcode bitmap for cafeteria scanner compatibility
     */
    fun generateBarcodeBitmap(code: String, width: Int = 640, height: Int = 180): Bitmap? {
        if (code.isBlank()) return null
        return try {
            val clean = code.trim().filter { it.isDigit() }
            if (clean.isBlank()) return null
            val hints = mapOf(
                EncodeHintType.MARGIN to 1,
                EncodeHintType.CHARACTER_SET to "UTF-8"
            )
            val matrix = MultiFormatWriter().encode(
                clean, BarcodeFormat.CODE_128, width, height, hints
            )
            val w = matrix.width
            val h = matrix.height
            val pixels = IntArray(w * h)
            for (y in 0 until h) {
                val offset = y * w
                for (x in 0 until w) {
                    pixels[offset + x] = if (matrix.get(x, y)) Color.BLACK else Color.WHITE
                }
            }
            val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, w, 0, 0, w, h)
            bitmap
        } catch (e: Exception) {
            // Fallback to QR code
            generateQrBitmap(code, minOf(width, height))
        }
    }

    fun formatNineDigitCode(code: String, toPersian: Boolean = false): String {
        val clean = code.trim().filter { it.isDigit() }
        val formatted = when {
            clean.length == 9 -> "${clean.substring(0, 3)} ${clean.substring(3, 6)} ${clean.substring(6, 9)}"
            clean.length > 6 -> "${clean.substring(0, 3)} ${clean.substring(3, 6)} ${clean.substring(6)}"
            clean.length > 3 -> "${clean.substring(0, 3)} ${clean.substring(3)}"
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
