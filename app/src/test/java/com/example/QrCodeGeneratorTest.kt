package com.example

import com.example.util.QrCodeGenerator
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.EncodeHintType
import com.google.zxing.LuminanceSource
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.google.zxing.qrcode.decoder.Mode
import com.google.zxing.qrcode.encoder.Encoder
import com.google.zxing.qrcode.encoder.QRCode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class QrCodeGeneratorTest {

    @Test
    fun testQrEncodingModeAndContent() {
        val testCode = "740560364"
        val hints = mapOf(
            EncodeHintType.CHARACTER_SET to "UTF-8",
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H,
            EncodeHintType.QR_VERSION to 2,
            EncodeHintType.MARGIN to 4,
        )

        val qrCode: QRCode = Encoder.encode(testCode, ErrorCorrectionLevel.H, hints)

        // Verify the QR code content
        assertEquals(2, qrCode.version.versionNumber)
        assertEquals(ErrorCorrectionLevel.H, qrCode.ecLevel)

        // Check what mode ZXing's Encoder actually picked
        val pickedMode = qrCode.mode
        println("ZXing Encoder picked mode for digit string: $pickedMode")

        // In ZXing, for all-digit input, Encoder.chooseMode picks Mode.NUMERIC even if UTF-8 hint is passed!
        assertEquals(Mode.NUMERIC, pickedMode)
    }

    @Test
    fun testQrInputTooLongDetection() {
        // Version 2 with ECC H has limited capacity
        // 50 numeric digits or 30 byte characters will exceed Version 2 ECC H capacity
        val tooLongCode = "123456789012345678901234567890123456789012345678901234567890"
        val result = QrCodeGenerator.generateQrResult(tooLongCode)
        assertTrue("Expected Error for oversized input", result is QrCodeGenerator.QrResult.Error)
        val error = result as QrCodeGenerator.QrResult.Error
        assertTrue("Expected isTooLong to be true", error.isTooLong)
    }

    @Test
    fun testFormatNineDigitCodePersian() {
        val testCode = "123456789"
        val formatted = QrCodeGenerator.formatNineDigitCode(testCode, toPersian = true)
        
        // Ensure string contains chunks in strictly left-to-right order:
        // first '۱۲۳', then '۴۵۶', then '۷۸۹'
        val indexChunk1 = formatted.indexOf("۱۲۳")
        val indexChunk2 = formatted.indexOf("۴۵۶")
        val indexChunk3 = formatted.indexOf("۷۸۹")

        assertTrue("Chunk 1 should be present", indexChunk1 >= 0)
        assertTrue("Chunk 2 should be present", indexChunk2 >= 0)
        assertTrue("Chunk 3 should be present", indexChunk3 >= 0)
        assertTrue("Chunk 1 must appear before Chunk 2", indexChunk1 < indexChunk2)
        assertTrue("Chunk 2 must appear before Chunk 3", indexChunk2 < indexChunk3)
        // Must contain LTR embedding to prevent RTL reordering in widgets
        assertTrue("Must contain LTR embedding marker", formatted.contains("\u202A"))
    }
}

