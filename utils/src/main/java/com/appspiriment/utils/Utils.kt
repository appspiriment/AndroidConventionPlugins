package org.sankarasmrithi.utils

import android.content.Context
import java.io.InputStream
import java.util.Calendar

fun Context.readStringFromAssets(filename:String): String {
    val inputStream: InputStream = assets.open(filename)
    val size = inputStream.available()
    val buffer = ByteArray(size)
    inputStream.read(buffer)
    return String(buffer)
}

fun getCurrentMonthInt():Int{
    return Calendar.getInstance().get(Calendar.MONTH) + 1
}