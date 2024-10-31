package com.example.repit

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
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
    var currentDate = LocalDate.now()
    val dateFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy")
    val formattedDate = currentDate.format(dateFormatter)
    val dayOfWeek = currentDate.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
    var isRestDay by remember { mutableStateOf(false) }

    // Coroutine scope to handle the asynchronous DataStore operations
    val scope = rememberCoroutineScope()

    // Fetch the rest day settings and check if the selected date is a rest day
    LaunchedEffect(currentDate) {
        isRestDay = false // Reset the rest day status for each date change

        scope.launch {
            isRestDay = exerciseRepository.isRestDay(currentDate)
            Log.d("TodayPage", "Is rest day: $isRestDay for $currentDate")
        }
    }

    // Fetch the stored daily goal when the exercise or date changes
    LaunchedEffect(selectedExercise, currentDate) {
        // Launch goal collection in a separate coroutine
        launch {
            exerciseRepository.getGoalForDate(selectedExercise, currentDate).collect { goal ->
                dailyGoal = goal
                dailyGoalText = goal.toString()
            }
        }

        // Launch reps collection in a separate coroutine
        launch {
            exerciseRepository.getRepsForDate(selectedExercise, currentDate).collect { reps ->
                currentReps = reps
                Log.d("DataStore", "Stored reps for $selectedExercise on $currentDate: $reps")
            }
        }

        Log.d("DataStore", "Launched both goal and reps collection")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        //horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Display the day of the week
        Text(
            text = dayOfWeek,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Display the current date with "Rest Day" if applicable
        Text(
            text = if (isRestDay) "$formattedDate (Rest Day)" else formattedDate,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // Drop-down menu for exercise selection
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedExercise,
                    onValueChange = { /* No need to change value here as selection is done via the dropdown */ },
                    readOnly = true,
                    label = { Text("Select Exercise", color = MaterialTheme.colorScheme.onSurface) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    exercises.forEach { exercise ->
                        DropdownMenuItem(
                            text = { Text(exercise, color = MaterialTheme.colorScheme.onSurface) },
                            onClick = {
                                onExerciseSelected(exercise)
                                expanded = false
                            }
                        )
                    }
                }
            }

            // TextField to input daily goal
            OutlinedTextField(
                value = dailyGoalText,
                onValueChange = { newGoalText ->
                    dailyGoalText = newGoalText
                    val newGoal = newGoalText.toIntOrNull()
                    if (newGoal != null) {
                        dailyGoal = newGoal
                        scope.launch {
                            exerciseRepository.setGoalForDate(selectedExercise, newGoal, currentDate)
                        }
                    }
                },
                label = { Text("Goal", color = MaterialTheme.colorScheme.onSurface) },
                modifier = Modifier.weight(1f),
                isError = dailyGoalText.toIntOrNull() == null && dailyGoalText.isNotEmpty(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            CustomProgressCircle(
                progress = currentReps / dailyGoal.toFloat(),
                modifier = Modifier.size(300.dp),
                color = MaterialTheme.colorScheme.primary,
                reps = currentReps
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
                    exerciseRepository.setRepsForDate(selectedExercise, newCount, currentDate)
                }
            }
            IncrementButton("+5", 5, currentReps) { newCount ->
                currentReps = newCount
                scope.launch {
                    Log.d("DataStore", "Running scope.launch")
                    exerciseRepository.setRepsForDate(selectedExercise, newCount, currentDate)
                }
            }
            IncrementButton("+10", 10, currentReps) { newCount ->
                currentReps = newCount
                scope.launch {
                    exerciseRepository.setRepsForDate(selectedExercise, newCount, currentDate)
                }
            }
            IncrementButton("+50", 50, currentReps) { newCount ->
                currentReps = newCount
                scope.launch {
                    exerciseRepository.setRepsForDate(selectedExercise, newCount, currentDate)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Reset button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = {
                    currentReps = 0
                    scope.launch {
                        exerciseRepository.setRepsForDate(selectedExercise, 0, currentDate)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary
                )
            ) {
                Text("Reset")
            }
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
        modifier = Modifier.size(80.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary
        )
    ) {
        Text(label)
    }
}

@Composable
fun CustomProgressCircle(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    strokeWidth: Float = 36f,
    reps: Int
) {
    // Infinite transition for the glint animation
    val infiniteTransition = rememberInfiniteTransition(label = "GlintTransition")

    val fireAnimationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = ""
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Fire ring effect background
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color.Red, Color(0xFFFFA500), Color.Yellow, Color.Red
                    ),
                    center = Offset(size.width / 2, size.height / 2)
                ),
                startAngle = fireAnimationAngle,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth * 1.2f)
            )

            // Draw the main progress arc with a gradient to simulate a glowing effect
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(
                        color,
                        color.copy(alpha = 0.8f),
                        color.copy(alpha = 0.6f),
                        color // Repeat to keep the gradient consistent
                    )
                ),
                startAngle = -90f,
                sweepAngle = 360 * progress,
                useCenter = false,
                style = Stroke(width = strokeWidth)
            )

            // Draw the reps text with gradient in the center
            val paint = Paint().apply {
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
                textSize = 150f // Adjust text size as needed
                style = Paint.Style.FILL

                // Set the gradient shader
                shader = LinearGradient(
                    0f, 0f, 0f, size.height,
                    intArrayOf(Color.Magenta.toArgb(), Color.Cyan.toArgb()),
                    null,
                    Shader.TileMode.CLAMP
                )
            }

            // Draw the text centered in the canvas
            drawContext.canvas.nativeCanvas.drawText(
                reps.toString(),
                size.width / 2,
                size.height / 2 - (paint.descent() + paint.ascent()) / 2, // Centers the text vertically
                paint
            )
        }
    }
}

