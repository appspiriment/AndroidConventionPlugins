package com.appspiriment.utils.extensions

import android.util.Log

/**
 * ******************************************
 * Method actionTo Print Log
 * ******************************************
 */
fun Throwable.printLog() {
    printStackTrace()
}

fun Exception.printLog() {
    printStackTrace()
}

fun printLog(message: Any?, tag: String = "", isError: Boolean = false) {
    printLogWithClassName(
        message = message,
        tag = tag,
        type = if (isError) Log.ERROR else Log.WARN,
        className = null
    )
}

fun printLog(message: Any?, tag: String = "", type: Int, className: String? = null) {
    printLogWithClassName(message = message, tag = tag, type = type, className = className)
}

internal fun printLogWithClassName(
    message: Any?,
    tag: String = "",
    type: Int,
    className: String? = null
) {
    val messageStr = if (!className.isNullOrBlank()) "$className - $message" else "$message"
    if (message is java.lang.Exception)
        message.printStackTrace()
    else {
        when (type) {
            Log.VERBOSE -> Log.d("Test Log-$tag", messageStr)
            Log.INFO -> Log.i("Test Log-$tag", messageStr)
            Log.DEBUG -> Log.d("Test Log-$tag", messageStr)
            Log.WARN -> Log.w("Test Log-$tag", messageStr)
            Log.ERROR -> Log.e("Test Log-$tag", messageStr)
        }
    }
}
