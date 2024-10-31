package com.example.repit.data

import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
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

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun initializeTodayRecords(exercises: List<String>) {
        val today = LocalDate.now()
        val isRestDayToday = isRestDay(today) // Determine if today is a rest day

        exercises.forEach { exercise ->
            // Check if there's already a record for today
            val existingRecord = dao.getExerciseLog(exercise, today).firstOrNull()

            // If no record exists, create a new one
            if (existingRecord == null) {
                val newLog = ExerciseLogEntity(
                    exercise = exercise,
                    date = today,
                    reps = 0,
                    goal = 25,
                    isRestDay = isRestDayToday
                )
                dao.insertExerciseLog(newLog)
            }
        }
    }

    fun getRepsForDate(exercise: String, date: LocalDate): Flow<Int> {
        return dao.getExerciseLog(exercise, date).map { it?.reps ?: 0 }
    }

    fun getGoalForDate(exercise: String, date: LocalDate): Flow<Int> {
        return dao.getExerciseLog(exercise, date).map { it?.goal ?: 25 }
    }

    suspend fun getNonRestRepsForPeriod(exercise: String, startDate: LocalDate, endDate: LocalDate): Int {
        return dao.getNonRestTotalReps(exercise, startDate, endDate) ?: 0
    }

    suspend fun getRepsForPeriod(exercise: String, startDate: LocalDate, endDate: LocalDate): Int {
        return dao.getTotalReps(exercise, startDate, endDate) ?: 0
    }

    suspend fun getNumberOfNonRestDays(exercise: String, startDate: LocalDate, endDate: LocalDate): Int {
        return dao.getNoNonRestDays(exercise, startDate, endDate) ?: 0
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

    suspend fun getRestDaysCount(exercise: String, startDate: LocalDate, endDate: LocalDate): Int {
        return dao.getRestDaysCount(exercise, startDate, endDate) ?: 0
    }

    suspend fun getFirstExerciseDate(exercise: String): LocalDate? {
        return dao.getFirstExerciseDate(exercise)
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
    private suspend fun calculateLongestStreak(dates: List<LocalDate>): Int {
        var longestStreak = 0
        var currentStreak = 0
        var previousDate: LocalDate? = null

        for (date in dates.sorted()) {
            // Check if previousDate is a rest day or if current date is a direct continuation of streak
            val isPreviousRestDay = previousDate?.let { dao.isRestDayOnDate(it) } == true
            if (previousDate != null && (date == previousDate.plusDays(1) || isPreviousRestDay)) {
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
    private suspend fun calculateCurrentStreak(dates: List<LocalDate>): Int {
        var currentStreak = 0
        var previousDate: LocalDate? = LocalDate.now()
        // Iterate over the dates from most recent to oldest
        for (date in dates.sortedDescending()) {
            // Check if the date is consecutive to the previous date
            val isConsecutive = previousDate == date || previousDate == date.plusDays(1)
            // Skip rest days in the streak calculation
            val isRestDay = dao.isRestDayOnDate(date)
            // If it's a rest day, we skip it without breaking the streak
            if (isRestDay) {
                previousDate = date // Move to the next date in the sequence
                continue
            }
            // If it's a non-rest day and consecutive, increment the streak
            if (isConsecutive) {
                currentStreak++
            } else {
                // Break the streak if the day is not consecutive and not a rest day
                break
            }
            // Update the previousDate for the next iteration
            previousDate = date
        }
        return currentStreak
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun setRestDay(dayOfWeek: DayOfWeek, isRestDay: Boolean) {
        val setting = RestDaySettings(dayOfWeek, isRestDay)
        dao.insertOrUpdateRestDaySetting(setting)

        val today = LocalDate.now()
        val dayOfWeekInt = dayOfWeek.value % 7

        // Update future dates to reflect rest day status
        dao.updateRestDayStatus(today, dayOfWeekInt, isRestDay)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun isRestDay(date: LocalDate): Boolean {
        val today = LocalDate.now()

        return if (date.isBefore(today)) {
            // If the date is in the past, check the `exercise_logs` table
            dao.isRestDayOnDate(date) == true // Return false if no record is found
        } else {
            // If the date is today or in the future, check the rest day settings
            val dayOfWeek = date.dayOfWeek
            dao.isRestDayForDayOfWeek(dayOfWeek) == true
        }
    }

    suspend fun getRestDaySettings(): Map<DayOfWeek, Boolean> {
        return dao.getAllRestDaySettings().associate { it.dayOfWeek to it.isRestDay }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getPastRestDays(): List<LocalDate> {
        return dao.getDistinctPastRestDays()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getRepsOverTime(exercise: String, startDate: LocalDate, endDate: LocalDate): List<Pair<LocalDate, Int>> {
        // Fetch data from DAO as a list of DateReps
        val dateRepsList = dao.getRepsOverTime(exercise, startDate, endDate) ?: emptyList()

        // Convert the list of DateReps to a list of Pair<LocalDate, Int>
        return dateRepsList.map { it.date to it.reps }
    }
}
