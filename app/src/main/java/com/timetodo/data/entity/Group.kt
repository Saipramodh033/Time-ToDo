package com.timetodo.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "groups")
data class Group(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val color: Int, // Color index in GroupColors list
    val icon: String = "work" // Icon name/identifier
)
