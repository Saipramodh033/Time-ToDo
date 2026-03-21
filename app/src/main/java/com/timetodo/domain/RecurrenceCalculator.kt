package com.timetodo.domain

import com.timetodo.data.entity.RecurrenceType
import com.timetodo.data.entity.Task
import java.util.Calendar

object RecurrenceCalculator {

    /**
     * Checks if a recurring task should appear on the given date
     */
    fun shouldShowOnDate(task: Task, date: java.time.LocalDate): Boolean {
        val dateMillis = date.atStartOfDay(java.time.ZoneOffset.UTC).toInstant().toEpochMilli()
        return shouldShowOnDate(task, dateMillis)
    }

    fun shouldShowOnDate(task: Task, dateMillis: Long): Boolean {
        // Normalized start date: Use assignedDate if available, otherwise createdAt
        val taskStartMillis = task.assignedDate ?: task.createdAt

        // Use a Calendar to normalize the task start time to the beginning of its day in UTC
        val startCal = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = taskStartMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If the query date is strictly before the task's start date (normalized), don't show it
        if (dateMillis < startCal.timeInMillis) return false

        if (task.recurrenceType == RecurrenceType.NONE) {
            // Non-recurring task - check if assigned date matches
            return task.assignedDate?.let {
                isSameDay(it, dateMillis)
            } ?: isSameDay(task.createdAt, dateMillis)
        }

        // For recurring tasks, check recurrence rules
        when (task.recurrenceType) {
            RecurrenceType.DAILY -> return true
            RecurrenceType.WEEKLY -> {
                val dayOfWeek = getDayOfWeek(dateMillis)
                val selectedDays = parseRecurrenceDays(task.recurrenceDays)
                return dayOfWeek in selectedDays
            }
            RecurrenceType.NONE -> return false
        }
    }

    /**
     * Gets day of week (1=Monday, 7=Sunday)
     */
    private fun getDayOfWeek(timeMillis: Long): Int {
        val calendar = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
        calendar.timeInMillis = timeMillis
        val javaDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        // Convert Java's 1=Sunday to 1=Monday
        return when (javaDayOfWeek) {
            Calendar.SUNDAY -> 7
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            else -> 1
        }
    }

    /**
     * Parses comma-separated day numbers
     */
    private fun parseRecurrenceDays(daysString: String): Set<Int> {
        if (daysString.isBlank()) return emptySet()
        return daysString.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .toSet()
    }

    /**
     * Checks if two timestamps are on the same day
     */
    private fun isSameDay(time1: Long, time2: Long): Boolean {
        val cal1 = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply { timeInMillis = time1 }
        val cal2 = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply { timeInMillis = time2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    /**
     * Formats recurrence days as readable string
     */
    fun formatRecurrenceDays(daysString: String): String {
        if (daysString.isBlank()) return ""
        val days = parseRecurrenceDays(daysString)
        val dayNames = mapOf(
            1 to "Mon", 2 to "Tue", 3 to "Wed", 4 to "Thu",
            5 to "Fri", 6 to "Sat", 7 to "Sun"
        )
        return days.sorted().joinToString(", ") { dayNames[it] ?: "" }
    }
}
