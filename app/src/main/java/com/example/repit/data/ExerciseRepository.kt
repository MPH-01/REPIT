package com.example.repit.data

import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class ExerciseRepository(private val dao: ExerciseLogDao) {

    suspend fun setRepsForDate(exercise: String, reps: Int, date: LocalDate) {
        val existingLog = dao.getExerciseLog(exercise, date)
        val currentLog = existingLog.firstOrNull()
        val newLog = ExerciseLogEntity(
            id = currentLog?.id ?: 0,
            exercise = exercise,
            date = date,
            reps = reps,
            goal = currentLog?.goal ?: 25
        )
        dao.insertExerciseLog(newLog)
    }

    suspend fun setGoalForDate(exercise: String, goal: Int, date: LocalDate) {
        val existingLog = dao.getExerciseLog(exercise, date)
        val currentLog = existingLog.firstOrNull()
        val newLog = ExerciseLogEntity(
            id = currentLog?.id ?: 0,
            exercise = exercise,
            date = date,
            reps = currentLog?.reps ?: 0,
            goal = goal
        )
        dao.insertExerciseLog(newLog)
    }

    fun getRepsForDate(exercise: String, date: LocalDate): Flow<Int> {
        return dao.getExerciseLog(exercise, date).map { it?.reps ?: 0 }
    }

    fun getGoalForDate(exercise: String, date: LocalDate): Flow<Int> {
        return dao.getExerciseLog(exercise, date).map { it?.goal ?: 25 }
    }

    suspend fun getTotalRepsForPeriod(exercise: String, startDate: LocalDate, endDate: LocalDate): Int {
        return dao.getTotalReps(exercise, startDate, endDate) ?: 0
    }

    suspend fun getDaysWithReps(exercise: String, startDate: LocalDate, endDate: LocalDate): Int {
        return dao.getDaysWithReps(exercise, startDate, endDate) ?: 0
    }

    suspend fun getBestDayReps(exercise: String, startDate: LocalDate, endDate: LocalDate): Int {
        return dao.getBestDayReps(exercise, startDate, endDate) ?: 0
    }

    suspend fun getTotalExerciseDays(exercise: String, startDate: LocalDate, endDate: LocalDate): Int {
        return dao.getDaysWithReps(exercise, startDate, endDate) ?: 0
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getLongestStreak(exercise: String, startDate: LocalDate, endDate: LocalDate): Int {
        val datesWithReps = dao.getDatesWithReps(exercise, startDate, endDate) ?: emptyList()
        return calculateLongestStreak(datesWithReps)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getCurrentStreak(exercise: String): Int {
        val datesWithReps = dao.getDatesWithReps(exercise, LocalDate.of(2000, 1, 1), LocalDate.now()) ?: emptyList()
        return calculateCurrentStreak(datesWithReps)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun calculateLongestStreak(dates: List<LocalDate>): Int {
        var longestStreak = 0
        var currentStreak = 0
        var previousDate: LocalDate? = null

        for (date in dates.sorted()) {
            if (previousDate != null && date == previousDate.plusDays(1)) {
                currentStreak++
            } else {
                currentStreak = 1
            }
            longestStreak = maxOf(longestStreak, currentStreak)
            previousDate = date
        }
        return longestStreak
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun calculateCurrentStreak(dates: List<LocalDate>): Int {
        var currentStreak = 0
        var previousDate: LocalDate? = null

        for (date in dates.sorted().reversed()) {
            if (previousDate == null || date == previousDate.minusDays(1)) {
                currentStreak++
            } else {
                break
            }
            previousDate = date
        }
        return currentStreak
    }
}
