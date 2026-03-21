package com.timetodo.data.dao

import androidx.room.*
import com.timetodo.data.entity.Task
import com.timetodo.data.entity.TaskStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE id = :id")
    fun getTaskById(id: Long): Flow<Task?>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskByIdSync(id: Long): Task?

    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE assignedDate = :date OR assignedDate IS NULL")
    fun getTasksForDate(date: Long): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE assignedDate BETWEEN :startDate AND :endDate OR assignedDate IS NULL")
    fun getTasksBetweenDates(startDate: Long, endDate: Long): Flow<List<Task>>
    
    @Query("SELECT * FROM tasks WHERE assignedDate = :date OR assignedDate IS NULL")
    suspend fun getTasksForDateSync(date: Long): List<Task>

    @Query("SELECT * FROM tasks WHERE groupId = :groupId")
    fun getTasksByGroup(groupId: Long): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE status = :status")
    fun getTasksByStatus(status: TaskStatus): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("UPDATE tasks SET status = :status WHERE id = :taskId")
    suspend fun updateTaskStatus(taskId: Long, status: TaskStatus)

    @Query("DELETE FROM tasks WHERE groupId = :groupId")
    suspend fun deleteTasksByGroup(groupId: Long)
}
