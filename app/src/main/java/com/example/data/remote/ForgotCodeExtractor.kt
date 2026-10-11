package com.example.data.remote

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

data class ExtractedForgotCode(
    val code: String?,
    val isValid: Boolean = true,
    val message: String? = null
)

object ForgotCodeExtractor {
    private const val TAG = "ForgotCodeExtractor"

    fun extract(raw: String?): ExtractedForgotCode {
        if (raw.isNullOrBlank()) {
            return ExtractedForgotCode(code = null, message = "پاسخی از سرور دریافت نشد")
        }

        val trimmed = raw.trim()
        Log.d(TAG, "Raw forgot code response: $trimmed")

        var foundCode: String? = null
        var isValid = true
        var message: String? = null

        // 1. Try parsing as JSON object
        try {
            if (trimmed.startsWith("{")) {
                val json = JSONObject(trimmed)

                if (json.has("message")) {
                    val msg = json.optString("message", "").trim()
                    if (msg.isNotBlank() && msg != "null") {
                        message = msg
                    }
                }

                // Check payload
                val payload = json.opt("payload")
                if (payload is JSONObject) {
                    val codeKeys = listOf(
                        "forgetCardCode", "forgotCardCode", "code", "printCode",
                        "cardCode", "barCode", "barcode", "value", "token", "serial"
                    )
                    for (k in codeKeys) {
                        val v = payload.opt(k)?.toString()?.trim()
                        if (!v.isNullOrBlank() && v != "null") {
                            foundCode = v
                            break
                        }
                    }
                    if (payload.has("valid")) {
                        isValid = payload.optBoolean("valid", true)
                    }
                } else if (payload is Number) {
                    foundCode = payload.toString()
                } else if (payload is String && payload.isNotBlank() && payload != "null") {
                    foundCode = payload.trim()
                } else if (payload is JSONArray && payload.length() > 0) {
                    val firstItem = payload.opt(0)
                    if (firstItem is JSONObject) {
                        val codeKeys = listOf("forgetCardCode", "forgotCardCode", "code", "printCode")
                        for (k in codeKeys) {
                            val v = firstItem.opt(k)?.toString()?.trim()
                            if (!v.isNullOrBlank() && v != "null") {
                                foundCode = v
                                break
                            }
                        }
                    } else if (firstItem != null) {
                        foundCode = firstItem.toString().trim()
                    }
                }

                // If not found in payload, check root JSON keys
                if (foundCode.isNullOrBlank()) {
                    val rootKeys = listOf("forgetCardCode", "forgotCardCode", "cardCode", "printCode")
                    for (k in rootKeys) {
                        val v = json.opt(k)?.toString()?.trim()
                        if (!v.isNullOrBlank() && v != "null") {
                            foundCode = v
                            break
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "JSON parsing warning: ${e.message}")
        }

        // 2. Regex fallback for known keys
        if (foundCode.isNullOrBlank()) {
            val keyRegex = Regex("""(?:"forgetCardCode"|"forgotCardCode"|"printCode"|"cardCode"|"barcode")\s*:\s*"?([0-9A-Za-z]+)"?""")
            val keyMatch = keyRegex.find(trimmed)
            if (keyMatch != null) {
                foundCode = keyMatch.groupValues[1]
            }
        }

        // 3. Fallback: Search for 9-digit code (standard Samad forget code length)
        if (foundCode.isNullOrBlank()) {
            val nineDigitMatch = Regex("""\b\d{9}\b""").find(trimmed)
            if (nineDigitMatch != null) {
                foundCode = nineDigitMatch.value
            }
        }

        // 4. Fallback: If trimmed itself is purely digits between 6 and 14 digits
        if (foundCode.isNullOrBlank() && trimmed.matches(Regex("""\d{6,14}"""))) {
            foundCode = trimmed
        }

        Log.d(TAG, "Extracted code: $foundCode, valid: $isValid, msg: $message")
        return ExtractedForgotCode(code = foundCode, isValid = isValid, message = message)
    }
}
