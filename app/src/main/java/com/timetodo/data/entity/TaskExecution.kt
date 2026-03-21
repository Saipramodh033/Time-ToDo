package com.timetodo.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "task_executions",
    foreignKeys = [
        ForeignKey(
            entity = Task::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("taskId"), Index("executionDate")]
)
data class TaskExecution(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskId: Long,
    val startTime: Long, // Timestamp in millis
    val elapsedSeconds: Int = 0,
    val pausedAt: Long? = null,
    val completedAt: Long? = null,
    val beforeNote: String = "",
    val afterNote: String = "",
    val executionDate: Long, // Date (day) of execution for filtering
    val isSkipped: Boolean = false
)
