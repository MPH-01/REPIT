package com.example.repit

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File

open class ExercisePreferences(private val context: Context?) {

    // Create the DataStore for preferences with proper context
    private val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(
        produceFile = { File(context?.filesDir, "datastore_preferences.preferences_pb") }
    )

    // Define keys for each exercise
    private val pushUpsKey = intPreferencesKey("push_ups_goal")
    private val sitUpsKey = intPreferencesKey("sit_ups_goal")
    private val squatsKey = intPreferencesKey("squats_goal")
    private val pullUpsKey = intPreferencesKey("pull_ups_goal")

    // Get the goal for a specific exercise
    open fun getGoal(exercise: String): Flow<Int> {
        return dataStore.data.map { preferences ->
            when (exercise) {
                "Push ups" -> preferences[pushUpsKey] ?: 25
                "Sit ups" -> preferences[sitUpsKey] ?: 25
                "Squats" -> preferences[squatsKey] ?: 25
                "Pull ups" -> preferences[pullUpsKey] ?: 25
                else -> 25
            }
        }
    }

    // Set the goal for a specific exercise
    open suspend fun setGoal(exercise: String, goal: Int) {
        dataStore.edit { preferences ->
            when (exercise) {
                "Push ups" -> preferences[pushUpsKey] = goal
                "Sit ups" -> preferences[sitUpsKey] = goal
                "Squats" -> preferences[squatsKey] = goal
                "Pull ups" -> preferences[pullUpsKey] = goal
            }
        }
    }
}