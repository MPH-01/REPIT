package com.example.repit.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "exercise_logs")
data class ExerciseLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val exercise: String,
    val date: LocalDate,
    val reps: Int,
    val goal: Int,
    val isRestDay: Boolean = false
)