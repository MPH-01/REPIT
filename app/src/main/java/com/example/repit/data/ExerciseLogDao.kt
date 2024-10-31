package com.example.repit.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek
import java.time.LocalDate

@Dao
interface ExerciseLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExerciseLog(exerciseLog: ExerciseLogEntity)

    @Query("SELECT * FROM exercise_logs WHERE exercise = :exercise AND date = :date LIMIT 1")
    fun getExerciseLog(exercise: String, date: LocalDate): Flow<ExerciseLogEntity?>

    @Query("SELECT SUM(reps) FROM exercise_logs WHERE exercise = :exercise AND date BETWEEN :startDate AND :endDate")
    suspend fun getTotalReps(exercise: String, startDate: LocalDate, endDate: LocalDate): Int?

    @Query("SELECT SUM(reps) FROM exercise_logs WHERE exercise = :exercise AND date BETWEEN :startDate AND :endDate AND isRestDay = 0")
    suspend fun getNonRestTotalReps(exercise: String, startDate: LocalDate, endDate: LocalDate): Int?

    @Query("SELECT COUNT(DISTINCT date) FROM exercise_logs WHERE exercise = :exercise AND reps > 0 AND date BETWEEN :startDate AND :endDate")
    suspend fun getDaysWithReps(exercise: String, startDate: LocalDate, endDate: LocalDate): Int?

    @Query("SELECT COUNT(DISTINCT date) FROM exercise_logs WHERE exercise = :exercise AND date BETWEEN :startDate AND :endDate AND isRestDay = 0")
    suspend fun getNoNonRestDays(exercise: String, startDate: LocalDate, endDate: LocalDate): Int?

    @Query("SELECT MAX(reps) FROM exercise_logs WHERE exercise = :exercise AND date BETWEEN :startDate AND :endDate")
    suspend fun getBestDayReps(exercise: String, startDate: LocalDate, endDate: LocalDate): Int?

    @Query("SELECT DISTINCT date FROM exercise_logs WHERE exercise = :exercise AND reps > 0 AND date BETWEEN :startDate AND :endDate ORDER BY date")
    suspend fun getDatesWithReps(exercise: String, startDate: LocalDate, endDate: LocalDate): List<LocalDate>?

    @Query("UPDATE exercise_logs SET isRestDay = :isRestDay, reps = CASE WHEN :isRestDay THEN 0 ELSE reps END, goal = CASE WHEN :isRestDay THEN 0 ELSE 25 END WHERE date >= :startDate AND strftime('%w', date) = :dayOfWeek")
    suspend fun updateRestDayStatus(startDate: LocalDate, dayOfWeek: Int, isRestDay: Boolean)

    @Query("SELECT MAX(isRestDay) FROM exercise_logs WHERE date = :date")
    suspend fun isRestDayOnDate(date: LocalDate): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateRestDaySetting(restDaySettings: RestDaySettings)

    @Query("SELECT isRestDay FROM rest_day_settings WHERE dayOfWeek = :dayOfWeek LIMIT 1")
    suspend fun isRestDayForDayOfWeek(dayOfWeek: DayOfWeek): Boolean?

    @Query("SELECT * FROM rest_day_settings")
    suspend fun getAllRestDaySettings(): List<RestDaySettings>

    @Query("SELECT COUNT(DISTINCT date) FROM exercise_logs WHERE exercise = :exercise AND date BETWEEN :startDate AND :endDate AND isRestDay = 1")
    suspend fun getRestDaysCount(exercise: String, startDate: LocalDate, endDate: LocalDate): Int?

    @Query("SELECT MIN(date) FROM exercise_logs WHERE exercise = :exercise")
    suspend fun getFirstExerciseDate(exercise: String): LocalDate?

    @Query("SELECT DISTINCT date FROM exercise_logs WHERE isRestDay = 1")
    suspend fun getDistinctPastRestDays(): List<LocalDate>

    @Query("SELECT date, reps FROM exercise_logs WHERE exercise = :exercise AND date BETWEEN :startDate AND :endDate ORDER BY date")
    suspend fun getRepsOverTime(exercise: String, startDate: LocalDate, endDate: LocalDate): List<DateReps>?
}
