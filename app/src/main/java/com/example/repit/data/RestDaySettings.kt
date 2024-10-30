package com.example.repit.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.DayOfWeek

@Entity(tableName = "rest_day_settings")
data class RestDaySettings(
    @PrimaryKey val dayOfWeek: DayOfWeek,
    val isRestDay: Boolean
)