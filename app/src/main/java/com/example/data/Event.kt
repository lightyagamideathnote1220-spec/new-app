package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val isAllDay: Boolean = false,
    val startTime: Long, // Epoch timestamp in milliseconds
    val endTime: Long,   // Epoch timestamp in milliseconds
    val categoryColor: String, // Hex string (e.g. "#0058be")
    val location: String = "",
    val reminderMinutes: Int = 15,
    val description: String = "",
    val tag: String = "", // e.g. "Conference"
    val hasAttachment: Boolean = false
) : Serializable
