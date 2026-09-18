package com.example.engine

import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlin.math.abs

data class AgeResult(
    val years: Int,
    val months: Int,
    val days: Int,
    val totalDays: Long,
    val daysUntilNextBirthday: Int
)

data class DateDiffResult(
    val totalDays: Long,
    val workingDays: Long,
    val weekends: Long,
    val fullWeeks: Long
)

object DateTimeEngine {

    fun calculateAge(birthYear: Int, birthMonth: Int, birthDay: Int): AgeResult {
        val today = Calendar.getInstance()
        val birth = Calendar.getInstance().apply {
            set(birthYear, birthMonth - 1, birthDay, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }

        var years = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR)
        var months = today.get(Calendar.MONTH) - birth.get(Calendar.MONTH)
        var days = today.get(Calendar.DAY_OF_MONTH) - birth.get(Calendar.DAY_OF_MONTH)

        if (days < 0) {
            months--
            val prevMonth = Calendar.getInstance().apply {
                time = today.time
                add(Calendar.MONTH, -1)
            }
            days += prevMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
        }

        if (months < 0) {
            years--
            months += 12
        }

        val diffMillis = abs(today.timeInMillis - birth.timeInMillis)
        val totalDays = TimeUnit.MILLISECONDS.toDays(diffMillis)

        // Next birthday countdown
        val nextBday = Calendar.getInstance().apply {
            set(today.get(Calendar.YEAR), birthMonth - 1, birthDay, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (nextBday.before(today)) {
            nextBday.add(Calendar.YEAR, 1)
        }
        val daysUntilBday = TimeUnit.MILLISECONDS.toDays(nextBday.timeInMillis - today.timeInMillis).toInt()

        return AgeResult(
            years = maxOf(0, years),
            months = maxOf(0, months),
            days = maxOf(0, days),
            totalDays = totalDays,
            daysUntilNextBirthday = maxOf(0, daysUntilBday)
        )
    }

    fun calculateDifference(
        y1: Int, m1: Int, d1: Int,
        y2: Int, m2: Int, d2: Int
    ): DateDiffResult {
        val cal1 = Calendar.getInstance().apply {
            set(y1, m1 - 1, d1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val cal2 = Calendar.getInstance().apply {
            set(y2, m2 - 1, d2, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val start = if (cal1.before(cal2)) cal1 else cal2
        val end = if (cal1.before(cal2)) cal2 else cal1

        val diffMillis = end.timeInMillis - start.timeInMillis
        val totalDays = TimeUnit.MILLISECONDS.toDays(diffMillis)

        var workingDays = 0L
        var weekends = 0L
        val walker = start.clone() as Calendar

        while (walker.before(end)) {
            val dayOfWeek = walker.get(Calendar.DAY_OF_WEEK)
            if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
                weekends++
            } else {
                workingDays++
            }
            walker.add(Calendar.DAY_OF_MONTH, 1)
        }

        return DateDiffResult(
            totalDays = totalDays,
            workingDays = workingDays,
            weekends = weekends,
            fullWeeks = totalDays / 7
        )
    }

    fun addDaysToDate(year: Int, month: Int, day: Int, daysToAdd: Int): Triple<Int, Int, Int> {
        val cal = Calendar.getInstance().apply {
            set(year, month - 1, day, 0, 0, 0)
            add(Calendar.DAY_OF_MONTH, daysToAdd)
        }
        return Triple(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }
}
