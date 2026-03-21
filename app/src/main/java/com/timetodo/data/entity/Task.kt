package com.timetodo.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val durationMinutes: Int,
    val groupId: Long? = null,
    val assignedDate: Long? = null, // Timestamp in millis
    val recurrenceType: RecurrenceType = RecurrenceType.NONE,
    val recurrenceDays: String = "", // Comma-separated day numbers (1=Mon,7=Sun) for weekly
    val status: TaskStatus = TaskStatus.PENDING,
    val endDateTime: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

enum class TaskStatus {
    PENDING,
    IN_PROGRESS,
    PAUSED,
    COMPLETED
}

enum class RecurrenceType {
    NONE,
    DAILY,
    WEEKLY
}
