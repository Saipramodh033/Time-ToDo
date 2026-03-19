package com.example.time_todo.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val plannedMinutes: Int,
    val status: String,
    val elapsedSeconds: Int = 0
)

