package com.example.repit

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch
import androidx.compose.material3.CircularProgressIndicator
import com.example.repit.data.ExerciseRepository

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TodayPage(
    selectedExercise: String,
    onExerciseSelected: (String) -> Unit,
    exerciseRepository: ExerciseRepository
) {
    var expanded by remember { mutableStateOf(false) }
    val exercises = listOf("Push ups", "Sit ups", "Squats", "Pull ups")
    var dailyGoal by remember { mutableIntStateOf(25) }
    var dailyGoalText by remember { mutableStateOf(dailyGoal.toString()) }
    var currentReps by remember { mutableIntStateOf(0) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val dateFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy")
    val formattedDate = selectedDate.format(dateFormatter)

    // Coroutine scope to handle the asynchronous DataStore operations
    val scope = rememberCoroutineScope()

    // Fetch the stored daily goal when the exercise or date changes
    LaunchedEffect(selectedExercise, selectedDate) {
        Log.d("DataStore", "Executing LaunchedEffect with $selectedExercise and $selectedDate")

        // Launch goal collection in a separate coroutine
        launch {
            exerciseRepository.getGoalForDate(selectedExercise, selectedDate).collect { goal ->
                dailyGoal = goal
                dailyGoalText = goal.toString()
                Log.d("DataStore", "Stored goal for $selectedExercise on $selectedDate: $goal")
            }
        }

        // Launch reps collection in a separate coroutine
        launch {
            exerciseRepository.getRepsForDate(selectedExercise, selectedDate).collect { reps ->
                currentReps = reps
                Log.d("DataStore", "Stored reps for $selectedExercise on $selectedDate: $reps")
            }
        }

        Log.d("DataStore", "Launched both goal and reps collection")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Date Navigation with arrows
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { selectedDate = selectedDate.minusDays(1) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Day")
            }
            Text(text = formattedDate, style = MaterialTheme.typography.titleLarge)
            IconButton(onClick = { selectedDate = selectedDate.plusDays(1) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Day")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedExercise,
                onValueChange = { /* No need to change value here as selection is done via the dropdown */ },
                readOnly = true,
                label = { Text("Select Exercise") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
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
                        exerciseRepository.setGoalForDate(selectedExercise, newGoal, selectedDate)
                    }
                }
            },
            label = { Text("Set Daily Goal") },
            modifier = Modifier.fillMaxWidth(),
            isError = dailyGoalText.toIntOrNull() == null && dailyGoalText.isNotEmpty()
        )

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier.size(300.dp),
            contentAlignment = Alignment.Center
        ) {
            // Red background circle (the "empty" part of the progress)
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.fillMaxSize(),
                strokeWidth = 36.dp,
                color = Color.Red
            )

            // Foreground progress circle
            CircularProgressIndicator(
                progress = { currentReps / dailyGoal.toFloat() },
                modifier = Modifier.fillMaxSize(),
                strokeWidth = 36.dp,
                color = if (currentReps >= dailyGoal) Color.Green else MaterialTheme.colorScheme.primary
            )

            Text(
                text = "$currentReps",
                style = MaterialTheme.typography.titleLarge,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Buttons for increments
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            IncrementButton("+1", 1, currentReps) { newCount ->
                currentReps = newCount
                scope.launch {
                    exerciseRepository.setRepsForDate(selectedExercise, newCount, selectedDate)
                }
            }
            IncrementButton("+5", 5, currentReps) { newCount ->
                currentReps = newCount
                scope.launch {
                    Log.d("DataStore", "Running scope.launch")
                    exerciseRepository.setRepsForDate(selectedExercise, newCount, selectedDate)
                }
            }
            IncrementButton("+10", 10, currentReps) { newCount ->
                currentReps = newCount
                scope.launch {
                    exerciseRepository.setRepsForDate(selectedExercise, newCount, selectedDate)
                }
            }
            IncrementButton("+50", 50, currentReps) { newCount ->
                currentReps = newCount
                scope.launch {
                    exerciseRepository.setRepsForDate(selectedExercise, newCount, selectedDate)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Reset button
        Button(onClick = {
            currentReps = 0
            scope.launch {
                exerciseRepository.setRepsForDate(selectedExercise, 0, selectedDate)
            }
        }) {
            Text("Reset")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// Increment Button composable
@Composable
fun IncrementButton(label: String, increment: Int, currentReps: Int, onIncrement: (Int) -> Unit) {
    Log.d("DataStore", "IncrementButton running with arguments $label, $increment, $currentReps, $onIncrement")
    Button(
        onClick = { onIncrement(currentReps + increment) },
        modifier = Modifier.size(80.dp)
    ) {
        Text(label)
    }
}
