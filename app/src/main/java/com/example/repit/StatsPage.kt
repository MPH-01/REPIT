package com.example.repit

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import com.example.repit.data.ExerciseRepository
import kotlinx.coroutines.launch
import java.time.LocalDate

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StatsPage(exerciseRepository: ExerciseRepository) {
    var selectedExercise by remember { mutableStateOf("Push ups") }
    var selectedPeriod by remember { mutableStateOf("All time") }
    val exercises = listOf("Push ups", "Sit ups", "Squats", "Pull ups")
    val periods = listOf("All time", "Last year", "Last month", "Last week")

    var totalReps by remember { mutableIntStateOf(0) }
    var averageReps by remember { mutableFloatStateOf(0f) }
    var bestDayReps by remember { mutableIntStateOf(0) }
    var totalExerciseDays by remember { mutableIntStateOf(0) }
    var longestStreak by remember { mutableIntStateOf(0) }
    var currentStreak by remember { mutableIntStateOf(0) }

    val scope = rememberCoroutineScope()
    LaunchedEffect(selectedExercise, selectedPeriod) {
        scope.launch {
            val dateRange = getDateRange(selectedPeriod)
            calculateStats(exerciseRepository, selectedExercise, dateRange) { total, average ->
                totalReps = total
                averageReps = average
            }
            bestDayReps = exerciseRepository.getBestDayReps(selectedExercise, dateRange.first, dateRange.second)
            totalExerciseDays = exerciseRepository.getTotalExerciseDays(selectedExercise, dateRange.first, dateRange.second)
            longestStreak = exerciseRepository.getLongestStreak(selectedExercise, dateRange.first, dateRange.second)
            currentStreak = exerciseRepository.getCurrentStreak(selectedExercise)
        }
    }

    var isExerciseDropdownExpanded by remember { mutableStateOf(false) }
    var isPeriodDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Exercise Dropdown Menu
        ExposedDropdownMenuBox(
            expanded = isExerciseDropdownExpanded,
            onExpandedChange = { isExerciseDropdownExpanded = !isExerciseDropdownExpanded }
        ) {
            OutlinedTextField(
                value = selectedExercise,
                onValueChange = {},
                readOnly = true,
                label = { Text("Select Exercise") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = isExerciseDropdownExpanded
                    )
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            DropdownMenu(
                expanded = isExerciseDropdownExpanded,
                onDismissRequest = { isExerciseDropdownExpanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                exercises.forEach { exercise ->
                    DropdownMenuItem(
                        text = { Text(exercise) },
                        onClick = {
                            selectedExercise = exercise
                            isExerciseDropdownExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Period Dropdown Menu
        ExposedDropdownMenuBox(
            expanded = isPeriodDropdownExpanded,
            onExpandedChange = { isPeriodDropdownExpanded = !isPeriodDropdownExpanded }
        ) {
            OutlinedTextField(
                value = selectedPeriod,
                onValueChange = {},
                readOnly = true,
                label = { Text("Select Period") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = isPeriodDropdownExpanded
                    )
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            DropdownMenu(
                expanded = isPeriodDropdownExpanded,
                onDismissRequest = { isPeriodDropdownExpanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                periods.forEach { period ->
                    DropdownMenuItem(
                        text = { Text(period) },
                        onClick = {
                            selectedPeriod = period
                            isPeriodDropdownExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display Stats in a Scrollable Column
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { StatsCard(title = "Total Reps", value = totalReps.toString()) }
            item { StatsCard(title = "Average Daily Reps", value = String.format("%.2f", averageReps)) }
            item { StatsCard(title = "Best Day Reps", value = bestDayReps.toString()) }
            item { StatsCard(title = "Total Exercise Days", value = totalExerciseDays.toString()) }
            item { StatsCard(title = "Longest Streak", value = longestStreak.toString()) }
            item { StatsCard(title = "Current Streak", value = currentStreak.toString()) }
        }
    }
}

@Composable
fun StatsCard(title: String, value: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
    }
}

// Utility function to determine the date range based on the selected period
@RequiresApi(Build.VERSION_CODES.O)
fun getDateRange(period: String): Pair<LocalDate, LocalDate> {
    val endDate = LocalDate.now()
    val startDate = when (period) {
        "Last year" -> endDate.minusYears(1)
        "Last month" -> endDate.minusMonths(1)
        "Last week" -> endDate.minusWeeks(1)
        else -> LocalDate.of(2000, 1, 1) // Arbitrary start date for "All time"
    }
    return startDate to endDate
}

// Calculate total and average reps based on selected exercise and date range using the repository
@RequiresApi(Build.VERSION_CODES.O)
suspend fun calculateStats(
    exerciseRepository: ExerciseRepository,
    exercise: String,
    dateRange: Pair<LocalDate, LocalDate>,
    onResult: (total: Int, average: Float) -> Unit
) {
    val (startDate, endDate) = dateRange
    val totalReps = exerciseRepository.getTotalRepsForPeriod(exercise, startDate, endDate)
    val daysWithReps = exerciseRepository.getDaysWithReps(exercise, startDate, endDate)

    // Calculate the average reps per day where reps > 0
    val averageReps = if (daysWithReps > 0) totalReps.toFloat() / daysWithReps else 0f

    onResult(totalReps, averageReps)
}
