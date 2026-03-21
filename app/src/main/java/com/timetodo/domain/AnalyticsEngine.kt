package com.timetodo.domain

import com.timetodo.data.TaskRepository
import com.timetodo.data.entity.Group
import com.timetodo.data.entity.Task
import com.timetodo.data.entity.TaskExecution
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Calendar

data class DaySummary(
    val date: Long,
    val plannedMinutes: Int,
    val actualMinutes: Int,
    val completedTasks: Int,
    val totalTasks: Int
)

data class WeekSummary(
    val weekStart: Long,
    val plannedMinutes: Int,
    val actualMinutes: Int,
    val completedTasks: Int,
    val totalTasks: Int,
    val dailySummaries: List<DaySummary>
)

data class GroupSummary(
    val groupId: Long,
    val totalMinutes: Int,
    val completedTasks: Int
)

class AnalyticsEngine(val repository: TaskRepository) {

    fun getDayStats(date: LocalDate): Flow<DaySummary?> {
        val startOfDay = date.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
        val endOfDay = date.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()

        return combine(
            repository.getTasksForDate(startOfDay),
            repository.getExecutionsForDateRange(startOfDay, endOfDay)
        ) { tasks, executions ->
            calculateDaySummary(startOfDay, tasks, executions)
        }
    }

    fun getWeekStats(date: LocalDate): Flow<WeekSummary?> {
        val weekStart = getWeekStart(date.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli())
        val weekEnd = weekStart + 7 * 24 * 60 * 60 * 1000

        return combine(
            repository.getAllTasks(),
            repository.getExecutionsForDateRange(weekStart, weekEnd)
        ) { tasks, executions ->
            calculateWeekSummary(weekStart, tasks, executions)
        }
    }

    fun getGroupStats(): Flow<List<GroupSummary>> {
        return combine(
            repository.getAllGroups(),
            repository.getAllTasks(),
            repository.getAllExecutions()
        ) { groups: List<Group>, tasks: List<Task>, executions: List<TaskExecution> ->
            calculateGroupSummaries(groups, tasks, executions)
        }
    }

    /**
     * Calculate summary for a specific day
     */
    private fun calculateDaySummary(
        date: Long,
        tasks: List<Task>,
        executions: List<TaskExecution>
    ): DaySummary {
        val plannedMinutes = tasks.sumOf { it.durationMinutes }
        val completedExecutions = executions.filter { it.completedAt != null }
        val actualMinutes = completedExecutions.sumOf { it.elapsedSeconds } / 60

        return DaySummary(
            date = date,
            plannedMinutes = plannedMinutes,
            actualMinutes = actualMinutes,
            completedTasks = completedExecutions.size,
            totalTasks = tasks.size
        )
    }

    /**
     * Calculate summary for a week
     */
    private fun calculateWeekSummary(
        weekStart: Long,
        tasks: List<Task>,
        executions: List<TaskExecution>
    ): WeekSummary {
        val calendar = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
        calendar.timeInMillis = weekStart

        val dailySummaries = mutableListOf<DaySummary>()
        var totalPlannedMinutes = 0
        var totalActualMinutes = 0
        var completedCount = 0
        var totalCount = 0

        // Generate summaries for 7 days
        for (i in 0..6) {
            val dayStart = calendar.timeInMillis
            val dayEnd = dayStart + 24 * 60 * 60 * 1000

            val dayTasks = tasks.filter { task ->
                RecurrenceCalculator.shouldShowOnDate(task, dayStart)
            }

            val dayExecutions = executions.filter {
                it.executionDate >= dayStart && it.executionDate < dayEnd
            }

            val daySummary = calculateDaySummary(dayStart, dayTasks, dayExecutions)
            dailySummaries.add(daySummary)

            totalPlannedMinutes += daySummary.plannedMinutes
            totalActualMinutes += daySummary.actualMinutes
            completedCount += daySummary.completedTasks
            totalCount += daySummary.totalTasks

            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        return WeekSummary(
            weekStart = weekStart,
            plannedMinutes = totalPlannedMinutes,
            actualMinutes = totalActualMinutes,
            completedTasks = completedCount,
            totalTasks = totalCount,
            dailySummaries = dailySummaries
        )
    }

    /**
     * Calculate time spent by group
     */
    private fun calculateGroupSummaries(
        groups: List<Group>,
        tasks: List<Task>,
        executions: List<TaskExecution>
    ): List<GroupSummary> {
        return groups.map { group ->
            val groupTasks = tasks.filter { it.groupId == group.id }
            val groupTaskIds = groupTasks.map { it.id }.toSet()
            val groupExecutions = executions.filter { it.taskId in groupTaskIds }

            val totalMinutes = groupExecutions
                .filter { it.completedAt != null }
                .sumOf { it.elapsedSeconds } / 60

            GroupSummary(
                groupId = group.id,
                totalMinutes = totalMinutes,
                completedTasks = groupExecutions.count { it.completedAt != null }
            )
        }.sortedByDescending { it.totalMinutes }
    }

    /**
     * Get start of week (Monday) for a given date
     */
    fun getWeekStart(timeMillis: Long): Long {
        val calendar = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
        calendar.timeInMillis = timeMillis
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}
