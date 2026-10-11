package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateTimeUtils {
    val JAKARTA_TIMEZONE: TimeZone = TimeZone.getTimeZone("Asia/Jakarta")
    val INDONESIA_LOCALE: Locale = Locale("id", "ID")

    fun getJakartaCalendar(): Calendar {
        return Calendar.getInstance(JAKARTA_TIMEZONE, INDONESIA_LOCALE)
    }

    fun formatJakarta(pattern: String, date: Date = Date()): String {
        val sdf = SimpleDateFormat(pattern, INDONESIA_LOCALE)
        sdf.timeZone = JAKARTA_TIMEZONE
        return sdf.format(date)
    }

    fun formatJakarta(pattern: String, timestamp: Long): String {
        return formatJakarta(pattern, Date(timestamp))
    }

    fun getTodayDateString(): String {
        return formatJakarta("yyyy-MM-dd")
    }

    fun getCurrentTimeString(): String {
        return formatJakarta("HH:mm")
    }

    fun getCurrentFullDateTimeString(): String {
        return formatJakarta("EEEE, dd MMMM yyyy HH:mm") + " WIB"
    }
}
