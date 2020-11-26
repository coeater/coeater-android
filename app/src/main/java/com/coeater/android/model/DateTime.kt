package com.coeater.android.model

import android.icu.text.DateFormat
import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import java.util.*

class DateTime {
    companion object {
        fun periodToAPI(date: Date) : String {
            val sdf = SimpleDateFormat("yyyy-MM-dd")
            return sdf.format(date)
        }
        fun toDate(dateTime: String) : Date {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")
            return try {
                sdf.parse(dateTime)
            } catch(e: Exception) { Date() }
        }
        fun getAgo(dateTime: String) : String = getAgo(toDate(dateTime))
        fun getAgo(history: Date) : String {
            val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
            sdf.timeZone = TimeZone.GMT_ZONE
            val now = Date(sdf.format(Date()))

            var count = now.year - history.year
            if(count != 0) return _getAgo(count, "year")
            count = now.month - history.month
            if(count != 0) return _getAgo(count, "month")
            count = now.date - history.date
            if(count != 0) return _getAgo(count, "day")
            count = now.hours - history.hours
            if(count != 0) return _getAgo(count, "hour")
            count = now.minutes - history.minutes
            if(count != 0) return _getAgo(count, "minute")
            count = now.seconds - history.seconds
            if(count != 0) return _getAgo(count, "second")
            return "now"
        }

        private fun _getAgo(count: Int, type: String): String {
            return if(count == 1) "$count$type ago"
            else count.toString() + type + "s ago"
        }
    }
}