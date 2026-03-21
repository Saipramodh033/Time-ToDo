package com.timetodo.data

import com.timetodo.data.dao.TaskExecutionWithDetails
import com.timetodo.data.entity.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.ZoneOffset

class TaskRepository(
    private val database: AppDatabase
) {
    private val taskDao = database.taskDao()
    private val groupDao = database.groupDao()
    private val executionDao = database.taskExecutionDao()

    // Task operations
    fun getAllTasks(): Flow<List<Task>> = taskDao.getAllTasks()

    fun getTaskById(id: Long): Flow<Task?> = taskDao.getTaskById(id)

    suspend fun getTaskByIdSync(id: Long): Task? = taskDao.getTaskByIdSync(id)

    fun getTasksByStatus(status: TaskStatus): Flow<List<Task>> = taskDao.getTasksByStatus(status)

    fun getTasksByGroup(groupId: Long): Flow<List<Task>> = taskDao.getTasksByGroup(groupId)

    fun getTasksForDate(date: LocalDate): Flow<List<Task>> {
        val dateMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        return taskDao.getTasksForDate(dateMillis)
    }

    fun getTasksForDate(dateMillis: Long): Flow<List<Task>> =
        taskDao.getTasksForDate(dateMillis)

    fun getTasksBetweenDates(startDate: LocalDate, endDate: LocalDate): Flow<List<Task>> {
        val startMillis = startDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val endMillis = endDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        return taskDao.getTasksBetweenDates(startMillis, endMillis)
    }

    suspend fun insertTask(task: Task): Long = taskDao.insertTask(task)

    suspend fun updateTask(task: Task) = taskDao.updateTask(task)

    suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)

    suspend fun updateTaskStatus(taskId: Long, status: TaskStatus) =
        taskDao.updateTaskStatus(taskId, status)

    // Group operations
    fun getAllGroups(): Flow<List<Group>> = groupDao.getAllGroups()

    fun getGroupById(id: Long): Flow<Group?> = groupDao.getGroupById(id)

    suspend fun getGroupByIdSync(id: Long): Group? = groupDao.getGroupByIdSync(id)

    suspend fun insertGroup(group: Group): Long = groupDao.insertGroup(group)

    suspend fun updateGroup(group: Group) = groupDao.updateGroup(group)

    suspend fun deleteGroup(groupId: Long) {
        groupDao.deleteGroupById(groupId)
        taskDao.deleteTasksByGroup(groupId)
    }

    // Execution operations
    fun getExecutionsForTask(taskId: Long): Flow<List<TaskExecution>> =
        executionDao.getExecutionsForTask(taskId)

    fun getExecutionsForDate(date: LocalDate): Flow<List<TaskExecution>> {
        val dateMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        return executionDao.getExecutionsForDate(dateMillis)
    }

    fun getExecutionsForDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<TaskExecution>> {
        val startMillis = startDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val endMillis = endDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        return executionDao.getExecutionsForDateRange(startMillis, endMillis)
    }

    fun getExecutionsForDateRange(startMillis: Long, endMillis: Long): Flow<List<TaskExecution>> =
        executionDao.getExecutionsForDateRange(startMillis, endMillis)

    fun getAllExecutions(): Flow<List<TaskExecution>> =
        executionDao.getAllExecutions()

    suspend fun insertTaskExecution(execution: TaskExecution): Long =
        executionDao.insertExecution(execution)

    suspend fun updateExecution(execution: TaskExecution) =
        executionDao.updateExecution(execution)

    suspend fun getExecutionById(id: Long): TaskExecution? =
        database.taskExecutionDao().getExecutionById(id)

    // Data management
    suspend fun clearAllData() {
        database.clearAllTables()
    }

    fun getRecentExecutions(days: Int): Flow<List<TaskExecutionWithDetails>> {
        val endDate = LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val startDate = LocalDate.now().minusDays(days.toLong()).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        return executionDao.getExecutionsWithTitlesForDateRange(startDate, endDate)
    }

    suspend fun resetTask(taskId: Long) {
        val todayMillis = LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        // Delete executions for today
        executionDao.deleteExecutionsForTaskOnDate(taskId, todayMillis)
        // Set status to PENDING
        taskDao.updateTaskStatus(taskId, TaskStatus.PENDING)
    }

    suspend fun deleteTaskForToday(taskId: Long) {
        val todayMillis = LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val execution = TaskExecution(
            taskId = taskId,
            startTime = System.currentTimeMillis(),
            executionDate = todayMillis,
            isSkipped = true
        )
        executionDao.insertExecution(execution)
        // Also ensure status is NOT active if it was
        taskDao.updateTaskStatus(taskId, TaskStatus.PENDING)
    }
}
