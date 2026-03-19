package com.example.time_todo.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskData {

    @Query("SELECT * FROM tasks")
    fun observeTasks(): Flow<List<TaskEntity>>

    @Insert
    suspend fun insert(task: TaskEntity)

    @Update
    suspend fun update(task: TaskEntity)

    @Query("UPDATE tasks SET status = :status WHERE id = :taskId")
    suspend fun updateStatus(taskId: Long, status: String)

    @Query("UPDATE tasks SET elapsedSeconds = :elapsed WHERE id = :taskId")
    suspend fun updateElapsed(taskId: Long, elapsed: Int)

}
