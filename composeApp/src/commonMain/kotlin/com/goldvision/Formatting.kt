package com.goldvision

import kotlin.math.abs
import kotlin.math.round

// تنسيق أرقام لا يعتمد على java.text/java.util.Locale، لأنها غير متاحة
// خارج JVM (لن تُصرَّف على iOS). يبقى نفس شكل الإخراج المستخدم سابقاً
// عبر String.format(Locale.US, "%,.2f", value) وما شابه.

private fun pow10(decimals: Int): Long {
    var result = 1L
    repeat(decimals) { result *= 10 }
    return result
}

private fun groupThousands(digits: String): String {
    if (digits.length <= 3) return digits
    val builder = StringBuilder()
    val offset = digits.length % 3
    digits.forEachIndexed { index, c ->
        if (index != 0 && (index - offset) % 3 == 0) builder.append(',')
        builder.append(c)
    }
    return builder.toString()
}

fun fmt(value: Double, decimals: Int, grouped: Boolean = false): String {
    val factor = pow10(decimals)
    val negative = value < 0
    val scaled = round(abs(value) * factor).toLong()
    val intPart = scaled / factor
    val fracPart = scaled % factor
    val intText = if (grouped) groupThousands(intPart.toString()) else intPart.toString()
    val fracText = if (decimals > 0) "." + fracPart.toString().padStart(decimals, '0') else ""
    return (if (negative && scaled != 0L) "-" else "") + intText + fracText
}
