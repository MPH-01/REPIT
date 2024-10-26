package com.example.repit.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface ExerciseLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExerciseLog(exerciseLog: ExerciseLogEntity)

    @Query("SELECT * FROM exercise_logs WHERE exercise = :exercise AND date = :date LIMIT 1")
    fun getExerciseLog(exercise: String, date: LocalDate): Flow<ExerciseLogEntity?>

    @Query("SELECT SUM(reps) FROM exercise_logs WHERE exercise = :exercise AND date BETWEEN :startDate AND :endDate")
    suspend fun getTotalReps(exercise: String, startDate: LocalDate, endDate: LocalDate): Int?

    @Query("SELECT COUNT(DISTINCT date) FROM exercise_logs WHERE exercise = :exercise AND reps > 0 AND date BETWEEN :startDate AND :endDate")
    suspend fun getDaysWithReps(exercise: String, startDate: LocalDate, endDate: LocalDate): Int?

    @Query("SELECT MAX(reps) FROM exercise_logs WHERE exercise = :exercise AND date BETWEEN :startDate AND :endDate")
    suspend fun getBestDayReps(exercise: String, startDate: LocalDate, endDate: LocalDate): Int?

    @Query("SELECT DISTINCT date FROM exercise_logs WHERE exercise = :exercise AND reps > 0 AND date BETWEEN :startDate AND :endDate ORDER BY date")
    suspend fun getDatesWithReps(exercise: String, startDate: LocalDate, endDate: LocalDate): List<LocalDate>?
}

