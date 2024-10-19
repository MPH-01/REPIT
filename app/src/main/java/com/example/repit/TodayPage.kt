package com.example.repit

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TodayPage(
    selectedExercise: String,
    onExerciseSelected: (String) -> Unit,
    exercisePreferences: ExercisePreferences
) {
    var expanded by remember { mutableStateOf(false) }
    val exercises = listOf("Push ups", "Sit ups", "Squats", "Pull ups")
    var dailyGoal by remember { mutableIntStateOf(25) }
    var dailyGoalText by remember { mutableStateOf(dailyGoal.toString()) }
    val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))

    // Coroutine scope to handle the asynchronous DataStore operations
    val scope = rememberCoroutineScope()

    // Fetch the stored daily goal when the exercise changes
    LaunchedEffect(selectedExercise) {
        exercisePreferences.getGoal(selectedExercise).collect { goal ->
            dailyGoal = goal
            dailyGoalText = goal.toString()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Display Today's Date
        Text(text = currentDate, style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(24.dp))

        // Dropdown Menu (Exercise Selector)
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = if (selectedExercise.isEmpty()) "Select Exercise" else selectedExercise,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true }
                    .padding(16.dp)
                    .background(MaterialTheme.colorScheme.surface),
                style = MaterialTheme.typography.bodyLarge
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                exercises.forEach { exercise ->
                    DropdownMenuItem(
                        text = { Text(exercise) },
                        onClick = {
                            onExerciseSelected(exercise)
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // TextField to input daily goal
        OutlinedTextField(
            value = dailyGoalText,
            onValueChange = { newGoalText ->
                dailyGoalText = newGoalText
                val newGoal = newGoalText.toIntOrNull()
                if (newGoal != null) {
                    dailyGoal = newGoal
                    scope.launch {
                        exercisePreferences.setGoal(selectedExercise, newGoal)
                    }
                }
            },
            label = { Text("Set Daily Goal") },
            modifier = Modifier.fillMaxWidth(),
            isError = dailyGoalText.toIntOrNull() == null && dailyGoalText.isNotEmpty()  // Show error if input is invalid
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun TodayPagePreview() {
    // Using MockExercisePreferences for preview purposes
    val mockExercisePreferences = MockExercisePreferences()

    TodayPage(
        selectedExercise = "Push ups",
        onExerciseSelected = {},
        exercisePreferences = mockExercisePreferences
    )
}
