package com.example.repit

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import com.example.repit.data.ExerciseRepository
import kotlinx.coroutines.launch
import java.time.LocalDate

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StatsPage(exerciseRepository: ExerciseRepository) {
    var selectedExercise by remember { mutableStateOf("Push ups") }
    var selectedPeriod by remember { mutableStateOf("Last week") }
    val exercises = listOf("Push ups", "Sit ups", "Squats", "Pull ups")
    val periods = listOf("All time", "Last year", "Last month", "Last week")

    var totalReps by remember { mutableIntStateOf(0) }
    var averageReps by remember { mutableFloatStateOf(0f) }
    var bestDayReps by remember { mutableIntStateOf(0) }
    var totalExerciseDays by remember { mutableIntStateOf(0) }
    var longestStreak by remember { mutableIntStateOf(0) }
    var currentStreak by remember { mutableIntStateOf(0) }
    var repsOverTime by remember { mutableStateOf(emptyList<Pair<LocalDate, Int>>()) }

    val scope = rememberCoroutineScope()
    LaunchedEffect(selectedExercise, selectedPeriod) {
        scope.launch {
            val dateRange = if (selectedPeriod == "All time") {
                val firstExerciseDate = exerciseRepository.getFirstExerciseDate(selectedExercise) ?: LocalDate.now()
                firstExerciseDate to LocalDate.now()
            } else {
                getDateRange(selectedPeriod)
            }
            calculateStats(exerciseRepository, selectedExercise, dateRange) { total, average ->
                totalReps = total
                averageReps = average
            }
            bestDayReps = exerciseRepository.getBestDayReps(selectedExercise, dateRange.first, dateRange.second)
            totalExerciseDays = exerciseRepository.getTotalExerciseDays(selectedExercise, dateRange.first, dateRange.second)
            longestStreak = exerciseRepository.getLongestStreak(selectedExercise, dateRange.first, dateRange.second)
            currentStreak = exerciseRepository.getCurrentStreak(selectedExercise)
            repsOverTime = exerciseRepository.getRepsOverTime(selectedExercise, dateRange.first, dateRange.second)
        }
    }

    var isExerciseDropdownExpanded by remember { mutableStateOf(false) }
    var isPeriodDropdownExpanded by remember { mutableStateOf(false) }
    var isLineGraphVisible by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        // Title at the top
        Text(
            text = "Statistics",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Row for Select Exercise and Select Period dropdowns
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                        .fillMaxWidth(fraction = 0.5f)
                        .menuAnchor()
                )

                DropdownMenu(
                    expanded = isExerciseDropdownExpanded,
                    onDismissRequest = { isExerciseDropdownExpanded = false }
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
                        .fillMaxWidth(fraction = 1f)
                        .menuAnchor()
                )

                DropdownMenu(
                    expanded = isPeriodDropdownExpanded,
                    onDismissRequest = { isPeriodDropdownExpanded = false }
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
        }

        // Display Stats in a Scrollable Column
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                StatsCard(
                    title = "Total Reps",
                    value = totalReps.toString(),
                    isContentVisible = isLineGraphVisible,
                    onClick = { isLineGraphVisible = !isLineGraphVisible }
                ) {
                    LineGraph(dataPoints = repsOverTime)
                }
            }
            item { StatsCard(title = "Average Daily Reps", value = String.format("%.2f", averageReps)) }
            item { StatsCard(title = "Best Day Reps", value = bestDayReps.toString()) }
            item { StatsCard(title = "Total Exercise Days", value = totalExerciseDays.toString()) }
            item { StatsCard(title = "Longest Streak", value = longestStreak.toString()) }
            item { StatsCard(title = "Current Streak", value = currentStreak.toString()) }
        }
    }
}

@Composable
fun StatsCard(
    title: String,
    value: String,
    isContentVisible: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick?.invoke() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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

            // Show content if it's visible and provided
            if (isContentVisible && content != null) {
                Spacer(modifier = Modifier.height(8.dp))
                content()
            }
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
        "Last week" -> endDate.minusDays(6)
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
    val totalReps = exerciseRepository.getRepsForPeriod(exercise, startDate, endDate)
    val daysWithReps = exerciseRepository.getNumberOfNonRestDays(exercise, startDate, endDate)

    // Calculate total days in range and subtract rest days
    val totalDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1
    val restDays = exerciseRepository.getRestDaysCount(exercise, startDate, endDate)
    val effectiveDays = totalDays - restDays

    // Calculate average reps per day, considering only non-rest days
    val averageReps = if (effectiveDays > 0) totalReps.toFloat() / effectiveDays else 0f

    onResult(totalReps, averageReps)
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LineGraph(dataPoints: List<Pair<LocalDate, Int>>) {
    if (dataPoints.isEmpty()) return // No data to plot

    // Retrieve colors from MaterialTheme outside the Canvas scope
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val textColor = MaterialTheme.colorScheme.onBackground

    val maxReps = dataPoints.maxOf { it.second }
    val minDate = dataPoints.minOf { it.first }
    val maxDate = dataPoints.maxOf { it.first }

    // Determine the label format based on the date range and point density
    val dateInterval = when {
        dataPoints.size > 30 -> dataPoints.size / 10 // Display fewer labels for large data sets
        else -> 1
    }
    val dateFormatter = when {
        maxDate.year > minDate.year -> { date: LocalDate -> date.year.toString().takeLast(2) }
        maxDate.month != minDate.month -> { date: LocalDate -> date.month.name.take(3) }
        else -> { date: LocalDate -> date.dayOfMonth.toString() }
    }
    val datesForLabels = dataPoints.mapIndexedNotNull { index, (date, _) ->
        if (index % dateInterval == 0) date else null
    }

    Canvas(modifier = Modifier
        .fillMaxWidth()
        .height(250.dp)
        .padding(16.dp)
    ) {
        // Set padding for the axes
        val xPadding = 40.dp.toPx()
        val yPadding = 30.dp.toPx()

        // Graph area size after padding
        val graphWidth = size.width - xPadding
        val graphHeight = size.height - yPadding

        // X-axis spacing based on visible points and graph width
        val xSpacing = graphWidth / (dataPoints.size - 1).coerceAtLeast(1)
        val yRatio = if (maxReps > 0) graphHeight / maxReps else 1f

        // Draw the Y-axis labels and ticks
        val yStep = (maxReps / 5).coerceAtLeast(1) // Show 5 ticks or less on Y-axis
        for (i in 0..5) {
            val yValue = i * yStep
            val yPos = graphHeight - (yValue * yRatio)
            drawContext.canvas.nativeCanvas.drawText(
                yValue.toString(),
                xPadding / 4, // Position to the left of the graph area
                yPos + yPadding / 2, // Offset for alignment
                android.graphics.Paint().apply {
                    color = textColor.toArgb()
                    textSize = 32f
                }
            )
            drawLine(
                color = secondaryColor.copy(alpha = 0.2f),
                start = Offset(xPadding, yPos),
                end = Offset(size.width, yPos),
                strokeWidth = 2f
            )
        }

        // Draw the X-axis labels and ticks with the selected date format
        datesForLabels.forEachIndexed { index, date ->
            val xPos = xPadding + (index * xSpacing * dateInterval)
            drawContext.canvas.nativeCanvas.drawText(
                dateFormatter(date),
                xPos,
                size.height - yPadding / 4, // Offset for alignment
                android.graphics.Paint().apply {
                    color = textColor.toArgb()
                    textSize = 28f
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            )
        }

        // Draw the line graph
        dataPoints.forEachIndexed { index, point ->
            if (index > 0) {
                val previousPoint = dataPoints[index - 1]
                val startX = xPadding + (index - 1) * xSpacing
                val startY = graphHeight - (previousPoint.second * yRatio)
                val endX = xPadding + index * xSpacing
                val endY = graphHeight - (point.second * yRatio)
                drawLine(
                    color = primaryColor,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 4f
                )
            }
        }

        // Draw circles at each data point
        dataPoints.forEachIndexed { index, point ->
            val x = xPadding + index * xSpacing
            val y = graphHeight - (point.second * yRatio)
            drawCircle(
                color = secondaryColor,
                radius = 6f,
                center = Offset(x, y)
            )
        }
    }
}

