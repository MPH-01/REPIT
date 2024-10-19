package com.example.repit

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

open class ExercisePreferences(private val context: Context?) {

    private val dateFormatter = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        DateTimeFormatter.ofPattern("yyyy-MM-dd")
    } else {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    }


    // Create the DataStore for preferences with proper context
    private val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(
        produceFile = { File(context?.filesDir, "datastore_preferences.preferences_pb") }
    )

    // Generate a unique key for the exercise and date
    @SuppressLint("NewApi")
    private fun getGoalKey(exercise: String, date: LocalDate): Preferences.Key<Int> {
        val keyName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Use DateTimeFormatter for API 26+
            "${exercise}_${date.format(dateFormatter as DateTimeFormatter)}_goal"
        } else {
            // Convert LocalDate to java.util.Date for lower API levels
            val localDateToDate = java.util.Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant())
            val formattedDate = (dateFormatter as SimpleDateFormat).format(localDateToDate)
            "${exercise}_${formattedDate}_goal"
        }
        return intPreferencesKey(keyName)
    }

    // Get the goal for a specific exercise on a specific date
    open fun getGoalForDate(exercise: String, date: LocalDate): Flow<Int> {
        return dataStore.data.map { preferences ->
            preferences[getGoalKey(exercise, date)] ?: 25  // Default goal is 25
        }
    }

    // Set the goal for a specific exercise on a specific date
    open suspend fun setGoalForDate(exercise: String, goal: Int, date: LocalDate) {
        dataStore.edit { preferences ->
            preferences[getGoalKey(exercise, date)] = goal
        }
    }
}
