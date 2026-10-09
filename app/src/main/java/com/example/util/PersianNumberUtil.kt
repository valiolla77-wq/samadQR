package com.example.util

/**
 * Utility to convert Latin digits to Persian digits.
 */
fun String.toPersianDigits(): String {
    val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    val builder = StringBuilder(this.length)
    for (ch in this) {
        if (ch in '0'..'9') {
            builder.append(persianDigits[ch - '0'])
        } else {
            builder.append(ch)
        }
    }
    return builder.toString()
}

fun Int.toPersianDigits(): String = this.toString().toPersianDigits()
fun Long.toPersianDigits(): String = this.toString().toPersianDigits()
