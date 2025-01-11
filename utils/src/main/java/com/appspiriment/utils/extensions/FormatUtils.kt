package com.appspiriment.utils.extensions

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**********************************
 * Format Date To RelativeTimeSpan
 **********************************/
fun String.changeDateFormat(
    inputFormat: String = "yyyy-MM-dd",
    outputFormat: String = "dd MMM yyyy"
): String? {
    return try {
        SimpleDateFormat(inputFormat, Locale.ENGLISH)
            .parse(this)?.changeDateFormat(outputFormat) ?: this
    } catch (e: Exception) {
        null
    }
}

/**********************************
 * Format Date To RelativeTimeSpan
 **********************************/
fun Date.changeDateFormat(
    outputFormat: String = "dd MMM yyyy"
): String {
    return try {
        SimpleDateFormat(outputFormat, Locale.ENGLISH).format(this)
    } catch (e: Exception) {
        "$this"
    }
}


/**********************************
 * Format Date To RelativeTimeSpan
 **********************************/
fun String.toDate(inputFormat: String = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"): Date? {
    try {
        if (isValidDateInput() && !isEmpty()) {
            SimpleDateFormat(inputFormat).let {
//                it.isLenient = false
                return it.parse(this)
            }
        }
    } catch (e: Exception) {
        return null
    }
    return null
}

/**********************************
 * Format Date To RelativeTimeSpan
 **********************************/
fun String.toMillis(inputFormat: String = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"): Long? {
    try {
        if (!isEmpty()) {
            return LocalDateTime.parse(this, DateTimeFormatter.ofPattern(inputFormat, Locale.ENGLISH))?.let{
                it.toEpochSecond(ZoneOffset.UTC) * 1000
            }
        }
    } catch (e: Exception) {
        e.printLog()
    }
    return null
}


/***************************************
 * Setting Observers
 ***************************************/
fun String.toCurrentYearLocalDate(dateFormat: String = "yyyy MMM dd hh:mm a"): LocalDate{
    val year = Calendar.getInstance().get(Calendar.YEAR)
    return "$year $this".toLocalDate(dateFormat)
}
/***************************************
 * Setting Observers
 ***************************************/
fun String.toLocalDate(dateFormat: String = "yyyy MMM dd hh:mm a"): LocalDate{
    return LocalDate.parse(
        this,
        DateTimeFormatter.ofPattern("$dateFormat", Locale.US)
    )
}
/***************************************
 * Setting Observers
 ***************************************/
fun String.isValidDateInput(dateFormat: String = "dd/MM/yyyy"): Boolean {
    val dateArr = this.split("/")
    if (dateArr.size == 3) {
        val day = dateArr[0].toInt()
        val month = dateArr[1].toInt()
        val year = dateArr[2].toInt()
        if (day in 1..31 && month in 1..12 && year > 0) {
            return true
        }
    }
    return false
}



/***************************************
 * Setting Observers
 ***************************************/
fun <T : Any> checkAllNotNullOrBlank(vararg options: T?): List<T>? {
    return if (options.all { it != null && (if (it is String) it.isNotBlank() else true) }) {
        options.toList().filterNotNull()
    } else null
}
