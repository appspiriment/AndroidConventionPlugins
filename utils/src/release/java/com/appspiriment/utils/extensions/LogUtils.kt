package com.appspiriment.utils.extensions

import android.util.Log

/**
 * ******************************************
 * Method actionTo Print Log
 * ******************************************
 */
fun Throwable.printLog() {}

fun Exception.printLog() {}

fun printLog(message: Any?, tag: String = "", isError: Boolean = false) {}

fun printLog(message: Any?, tag: String = "", type: Int, className: String? = null) {}

internal fun printLogWithClassName(
    message: Any?,
    tag: String = "",
    type: Int,
    className: String? = null
) {
}
