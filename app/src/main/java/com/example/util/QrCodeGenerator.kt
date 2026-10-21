package com.example.util

import android.graphics.Bitmap
import android.graphics.Color
import android.util.LruCache
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.google.zxing.qrcode.encoder.ByteMatrix
import com.google.zxing.qrcode.encoder.Encoder
import com.google.zxing.qrcode.encoder.QRCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object QrCodeGenerator {

    sealed class QrResult {
        data class Success(val bitmap: Bitmap) : QrResult()
        data class Error(val message: String, val isTooLong: Boolean = false) : QrResult()
    }

    // Cache recently generated QR bitmaps to avoid repeated matrix allocation
    private val qrBitmapCache = object : java.util.LinkedHashMap<String, Bitmap>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Bitmap>?): Boolean {
            return size > 16
        }
    }

    /**
     * Generates QR result matching Samad's system:
     * - ECC = H
     * - Version = 2
     * - Margin = 4 (ISO/IEC 18004 standard quiet zone)
     * - Pure white background, pure black foreground
     *
     * Returns a clear error state if the input exceeds Version 2 / ECC H capacity.
     */
    fun generateQrResult(code: String, size: Int = 800): QrResult {
        if (code.isBlank()) {
            return QrResult.Error("کد خالی است")
        }

        val cacheKey = "${code}_$size"
        synchronized(qrBitmapCache) {
            qrBitmapCache[cacheKey]?.let { cached ->
                return QrResult.Success(cached)
            }
        }

        return try {
            val hints = mapOf(
                EncodeHintType.CHARACTER_SET to "UTF-8",
                EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H,
                EncodeHintType.QR_VERSION to 2,
                EncodeHintType.MARGIN to 4,
            )

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

            synchronized(qrBitmapCache) {
                qrBitmapCache[cacheKey] = bitmap
            }
            QrResult.Success(bitmap)
        } catch (we: WriterException) {
            val msg = we.message ?: ""
            val isTooLong = msg.contains("too big", ignoreCase = true) || msg.contains("capacity", ignoreCase = true)
            QrResult.Error(
                message = if (isTooLong) "طول داده برای بارکد نسخه ۲ فراتر از حد مجاز است" else "خطا در تولید بارکد: $msg",
                isTooLong = isTooLong
            )
        } catch (e: Exception) {
            val msg = e.message ?: "خطای ناشناخته"
            QrResult.Error(message = "خطا در تولید بارکد: $msg", isTooLong = false)
        }
    }

    /**
     * Synchronous QR bitmap generator (cached)
     */
    fun generateQrBitmap(code: String, size: Int = 800): Bitmap? {
        return when (val result = generateQrResult(code, size)) {
            is QrResult.Success -> result.bitmap
            is QrResult.Error -> null
        }
    }

    /**
     * Generates QR bitmap asynchronously off the main thread
     */
    suspend fun generateQrBitmapAsync(code: String, size: Int = 800): Bitmap? = withContext(Dispatchers.Default) {
        generateQrBitmap(code, size)
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

    suspend fun generateBarcodeBitmapAsync(code: String, width: Int = 640, height: Int = 180): Bitmap? =
        withContext(Dispatchers.Default) {
            generateBarcodeBitmap(code, width, height)
        }

    fun formatNineDigitCode(code: String, toPersian: Boolean = false): String {
        val clean = code.trim().filter { it.isDigit() }
        if (clean.isEmpty()) return ""

        val chunk1 = if (clean.length >= 3) clean.substring(0, 3) else clean
        val chunk2 = if (clean.length >= 6) clean.substring(3, 6) else (if (clean.length > 3) clean.substring(3) else "")
        val chunk3 = if (clean.length >= 9) clean.substring(6, 9) else (if (clean.length > 6) clean.substring(6) else "")

        fun toFa(str: String): String {
            val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
            val sb = StringBuilder()
            for (ch in str) {
                if (ch in '0'..'9') sb.append(persianDigits[ch - '0']) else sb.append(ch)
            }
            return sb.toString()
        }

        val c1 = if (toPersian) toFa(chunk1) else chunk1
        val c2 = if (toPersian) toFa(chunk2) else chunk2
        val c3 = if (toPersian) toFa(chunk3) else chunk3

        // \u202A: Left-to-Right Embedding, \u200E: Left-to-Right Mark, \u202C: Pop Directional Formatting
        // Guarantees left-to-right display in RTL widgets and text views
        return if (c2.isNotEmpty() && c3.isNotEmpty()) {
            "\u202A\u200E$c1\u200E — \u200E$c2\u200E — \u200E$c3\u200E\u202C"
        } else if (c2.isNotEmpty()) {
            "\u202A\u200E$c1\u200E — \u200E$c2\u200E\u202C"
        } else {
            "\u202A\u200E$c1\u200E\u202C"
        }
    }
}
