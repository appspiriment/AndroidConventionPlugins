package com.appspiriment.utils.extensions.time

import android.text.format.DateFormat
import org.sankarasmrithi.utils.extensions.printLog
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatterBuilder
import java.util.Locale
import kotlin.math.roundToLong

fun ZonedDateTime.format(pattern: String): String =
    DateTimeFormatterBuilder().appendPattern(pattern).toFormatter(Locale.ENGLISH).format(this)

fun zonedTimeFromMillis(millis: Long): ZonedDateTime = ZonedDateTime.ofInstant(java.time.Instant.ofEpochMilli(millis), ZoneId.systemDefault())
fun millisToNazhikaVinazhika(millis: Long) : Pair<Int, Int>  = ((millis.toDouble()/1000)/24).let{
    val nazhika = (it/60).toInt()
    Pair(nazhika, (it-(nazhika*60)).toInt())
}

val ZonedDateTime.time_hhmm_a: String get() = format("hh:mm a")
val ZonedDateTime.time_hhmmss_a: String get() = format("hh:mm:ss a")
val ZonedDateTime.time_HHmm: String get() = format("HH:mm")
val ZonedDateTime.date_mmm_dd: String get() = format("MMM dd")
val ZonedDateTime.dateTime_mmm_dd_hh_mm_a: String get() = format("MMM dd hh:mm a")
val ZonedDateTime.dateTime_mmm_dd_HH_mm: String get() = format("MMM dd HH:mm")
val ZonedDateTime.dateTime_mmm_dd_split_hh_mm_a: String get() = format("MMM dd\nhh:mm a")
val ZonedDateTime.dateTime_mmm_dd_splut_HH_mm: String get() = format("MMM dd\nHH:mm")

val ZonedDateTime.midnightInstance: ZonedDateTime get() = withHour(0).withMinute(0).withSecond(1)
val ZonedDateTime.noonInstance: ZonedDateTime get() = withHour(12).withMinute(0).withSecond(1)

val ZonedDateTime.nextDay: ZonedDateTime get() = plusDays(1)
val ZonedDateTime.previousDay: ZonedDateTime get() = plusDays(-1)
val ZonedDateTime.previousMonth: ZonedDateTime get() = plusMonths(-1)
val ZonedDateTime.nextMonth: ZonedDateTime get() = plusMonths(1)

val ZonedDateTime.decimalHours: Double get() = hour.toDouble() + minute.toDouble() / 60 + second.toDouble() / 3600
val ZonedDateTime.decimalYears: Double get() = year.toDouble() + (month.value.toDouble() / 12) + (dayOfMonth.toDouble() / 365.25) - 1.086
val ZonedDateTime.decimalTime: Double get() = hour.toDouble() + (minute.toDouble() / 60) + (second.toDouble() / 3600)

val ZonedDateTime.millis: Long get() = toEpochSecond() * 1000
val ZonedDateTime.midnightMillis: Long get() = midnightInstance.millis
val ZonedDateTime.noonMillis: Long get() = noonInstance.millis

val Double.dms
    get(): Triple<Int, Int, Int> {
        var degree = this
        degree += 0.5 / 3600.0 / 10000.0 // round to 1/1000 of a second
        val deg = degree.toInt()
        degree = (degree - deg) * 60
        val min = degree.toInt()
        degree = (degree - min) * 60
        val sec = degree
        return Triple(deg, min, sec.toInt())
    }

fun Double.fromHoursToMillis() = this * 60 * 60 * 1000
fun Double.fromHoursToSeconds(): Long = (this * 3600).roundToLong()
fun Double.toDMSString(): String = dms.run { "$first° $second' $third\"" }

fun Double.fromHoursToHMS(): Triple<Int, Int, Int> {
    val totalSeconds = (this * 3600).toInt()
    val hoursResult = totalSeconds / 3600
    val minutesResult = (totalSeconds % 3600) / 60
    val secondsResult = totalSeconds % 60
    return Triple(hoursResult, minutesResult, secondsResult)
}
fun Double.fromHoursToNazhika(): Pair<Int, Int> = (this * 2.5).fromHoursToHMS().let {
    Pair(it.first, it.second)
}
fun Double.fromHourstoNVString(): String = fromHoursToNazhika().run { "$first നാ $second വി" }
fun Double.fromNazhikaToNV(): Pair<Int, Int> = fromHoursToHMS().let {
    Pair(it.first, it.second)
}
fun Double.fromNazhikaToNVString(): String  = fromNazhikaToNV().let { "${it.first} നാ ${it.second} വി" }



fun Long.millisToDecimalHour(): Double = this / 3.6e+6
fun Long.millisToDays(): Double = this.toDouble() / (1000 * 60 * 60 * 24)

fun (Long?).millisToHmaTime(): String?{
    return this?.let{
        try {
            DateFormat.format("hh:mm a", it).toString()
        } catch (e: Exception) {
            e.printLog()
            throw RuntimeException("Invalid Long in millisToHmaTimeFormat - $this - ${e.message}")
        }
    }
}

fun (Long?).millisToMMddHmaTime(): String?{
    return this?.let{
        try {
            DateFormat.format("MMM dd hh:mm a", it).toString()
        } catch (e: Exception) {
            null
        }
    }
}