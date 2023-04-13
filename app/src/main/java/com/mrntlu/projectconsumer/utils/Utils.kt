package com.mrntlu.projectconsumer.utils

import android.util.Log

fun printLog(message:String, tag: String = "TestLog") = Log.d(tag,message)

fun compactMediumCalculator(height: Float): Double {
    var i = 20
    var span = i
    while (i < 30) {
        val remainder = height.toDouble().roundOffDecimal() % i

        if (remainder <= 0.5) {
            if (i > span)
                span = i
        }
        i += 1
    }
    return span / 10.0
}

fun expandedMediumCalculator(height: Float): Double {
    var i = 200
    var span = i
    while (i < 300) {
        val remainder = height.toDouble().roundSingleDecimal() % i

        if (remainder.toInt() <= 10) {
            if (i > span)
                span = i
        }
        i += 1
    }
    return span / 100.0
}