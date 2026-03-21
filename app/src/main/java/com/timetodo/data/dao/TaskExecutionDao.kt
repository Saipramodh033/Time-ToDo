package com.timetodo.data.dao

import androidx.room.*
import com.timetodo.data.entity.TaskExecution
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskExecutionDao {
    @Query("SELECT * FROM task_executions WHERE id = :id")
    suspend fun getExecutionById(id: Long): TaskExecution?

    @Query("SELECT * FROM task_executions ORDER BY startTime DESC")
    fun getAllExecutions(): Flow<List<TaskExecution>>

    @Query("SELECT * FROM task_executions WHERE taskId = :taskId ORDER BY startTime DESC")
    fun getExecutionsForTask(taskId: Long): Flow<List<TaskExecution>>

    @Query("SELECT * FROM task_executions WHERE executionDate = :date ORDER BY startTime DESC")
    fun getExecutionsForDate(date: Long): Flow<List<TaskExecution>>

    @Query("""
        SELECT * FROM task_executions 
        WHERE executionDate >= :startDate AND executionDate <= :endDate 
        ORDER BY startTime DESC
    """)
    fun getExecutionsForDateRange(startDate: Long, endDate: Long): Flow<List<TaskExecution>>

    @Query("SELECT * FROM task_executions WHERE taskId = :taskId AND executionDate = :date")
    suspend fun getExecutionForTaskOnDate(taskId: Long, date: Long): TaskExecution?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExecution(execution: TaskExecution): Long

    @Update
    suspend fun updateExecution(execution: TaskExecution)

    @Delete
    suspend fun deleteExecution(execution: TaskExecution)

    @Query("DELETE FROM task_executions WHERE taskId = :taskId")
    suspend fun deleteExecutionsForTask(taskId: Long)

    @Query("DELETE FROM task_executions WHERE taskId = :taskId AND executionDate = :date")
    suspend fun deleteExecutionsForTaskOnDate(taskId: Long, date: Long)

    @Query("""
        SELECT te.id, te.taskId, te.startTime, te.elapsedSeconds, 
               te.executionDate, te.beforeNote, te.afterNote, te.completedAt,
               t.title as taskTitle
        FROM task_executions te
        INNER JOIN tasks t ON te.taskId = t.id
        WHERE te.executionDate >= :startDate AND te.executionDate <= :endDate
        ORDER BY te.startTime DESC
    """)
    fun getExecutionsWithTitlesForDateRange(startDate: Long, endDate: Long): Flow<List<TaskExecutionWithDetails>>
}

data class TaskExecutionWithDetails(
    val id: Long,
    val taskId: Long,
    val startTime: Long,
    val elapsedSeconds: Int,
    val executionDate: Long,
    val beforeNote: String,
    val afterNote: String,
    val completedAt: Long?,
    val taskTitle: String
)
