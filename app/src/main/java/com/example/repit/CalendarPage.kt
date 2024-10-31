package com.example.repit

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.repit.data.ExerciseRepository
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@SuppressLint("NewApi")
@Composable
fun CalendarPage(
    exerciseRepository: ExerciseRepository
) {
    // Get the current date
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    val scope = rememberCoroutineScope()

    // States to store past and current rest day information
    val pastRestDays = remember { mutableStateListOf<LocalDate>() }
    val currentRestDaysOfWeek = remember { mutableStateMapOf<DayOfWeek, Boolean>() }

    // Fetch past rest days and current rest day settings
    LaunchedEffect(Unit) {
        pastRestDays.clear()
        pastRestDays.addAll(exerciseRepository.getPastRestDays())

        currentRestDaysOfWeek.clear()
        currentRestDaysOfWeek.putAll(exerciseRepository.getRestDaySettings())
    }

    // Initialise LazyListState to start from the current month (index 50)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = 50)
    val currentYearMonth = YearMonth.now()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        // Top Row with "Calendar" title on the left and "Today" button on the right
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Display the day of the week
            Text(
                text = "Calendar",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                //modifier = Modifier.padding(bottom = 16.dp)
            )

            // "Today" button
            Button(onClick = {
                // Scroll to today's month when clicked
                scope.launch {
                    listState.animateScrollToItem(50)
                }
            }) {
                Text("Today")
            }
        }

        // Define a lazy column to scroll through months
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState
        ) {
            // Render one calendar per month
            items(100) { index -> // Allow scrolling through many months
                val currentMonth = currentYearMonth.plusMonths(index.toLong() - 50) // Centre on the current month
                MonthView(
                    yearMonth = currentMonth,
                    selectedDate = selectedDate,
                    onDayClick = { day ->
                        scope.launch {
                            selectedDate = day
                        }
                    },
                    pastRestDays = pastRestDays,
                    currentRestDaysOfWeek = currentRestDaysOfWeek
                )
            }
        }
    }

    // Show the popup when a day is selected
    selectedDate?.let { day ->
        val isRestDay = day in pastRestDays
                || (day.isAfter(LocalDate.now()) && currentRestDaysOfWeek[day.dayOfWeek] == true)
                || (day == LocalDate.now() && currentRestDaysOfWeek[day.dayOfWeek] == true)

        DayPopup(day = day, onDismiss = { selectedDate = null }, isRestDay = isRestDay, exerciseRepository = exerciseRepository)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MonthView(
    yearMonth: YearMonth,
    selectedDate: LocalDate?,
    onDayClick: (LocalDate) -> Unit,
    pastRestDays: List<LocalDate>,
    currentRestDaysOfWeek: Map<DayOfWeek, Boolean>
) {
    val today = LocalDate.now()

    Column(modifier = Modifier.padding(8.dp)) {
        // Display the month and year
        Text(
            text = yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(8.dp)
        )

        // Display the days of the week
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
            daysOfWeek.forEach { dayOfWeek ->
                Text(
                    text = dayOfWeek,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Get the number of days in the current month
        val daysInMonth = (1..yearMonth.lengthOfMonth()).map { day ->
            LocalDate.of(yearMonth.year, yearMonth.month, day)
        }

        // Get the day of the week of the first day of the month
        val firstDayOfMonth = daysInMonth.first().dayOfWeek.value % 7

        // Calculate the total number of cells required to display the month (fill all rows)
        val totalCells = (firstDayOfMonth + daysInMonth.size).let {
            if (it % 7 == 0) it else it + (7 - it % 7)
        }

        //Display the grid layout (rows of 7 days)
        for (i in 0 until totalCells step 7) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (j in 0 until 7) {
                    val cellIndex = i + j

                    when {
                        cellIndex < firstDayOfMonth -> {
                            // Add empty cells before the first day of the month
                            Spacer(modifier = Modifier
                                .padding(4.dp)
                                .size(40.dp))
                        }
                        cellIndex - firstDayOfMonth < daysInMonth.size -> {
                            val day = daysInMonth[cellIndex - firstDayOfMonth]
                            val isFutureRestDay = day.isAfter(today) && currentRestDaysOfWeek[day.dayOfWeek] == true
                            val isPastRestDay = day in pastRestDays
                            val isToday = day == today
                            val isTodayRestDay = isToday && currentRestDaysOfWeek[day.dayOfWeek] == true

                            val isRestDay = isPastRestDay || isFutureRestDay || isTodayRestDay

                            // Display the valid days of the month
                            DayItem(
                                day = daysInMonth[cellIndex - firstDayOfMonth],
                                isSelected = selectedDate == day,
                                isRestDay = isRestDay,
                                isToday = isToday,
                                onClick = { onDayClick(day) }
                            )
                        }
                        else -> {
                            // Add empty cells after the last day of the month
                            Spacer(modifier = Modifier
                                .padding(4.dp)
                                .size(40.dp))
                        }
                    }
                }
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DayItem(day: LocalDate, isSelected: Boolean, isRestDay: Boolean, isToday: Boolean, onClick: () -> Unit) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isRestDay -> Color.Gray.copy(alpha = 0.3f)
        else -> MaterialTheme.colorScheme.surface
    }

    val borderColor = if (isToday) Color.Yellow else Color.Transparent

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .padding(4.dp)
            .size(40.dp)
            .clickable { onClick() }
            .background(
                color = backgroundColor,
                shape = CircleShape
            )
            .border(
                width = if (isToday) 2.dp else 0.dp,
                color = borderColor,
                shape = CircleShape
            )
    ) {
        Text(
            text = day.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DayPopup(
    day: LocalDate,
    onDismiss: () -> Unit,
    isRestDay: Boolean,
    exerciseRepository: ExerciseRepository
) {
    val exercises = listOf("Push ups", "Sit ups", "Squats", "Pull ups")
    val scope = rememberCoroutineScope()

    // Store the progress data for each exercise
    val progressData = remember { mutableStateMapOf<String, Pair<Int, Int>>() } // Pair of current reps and goal reps

    // Fetch data for each exercise when the popup is displayed
    LaunchedEffect(day) {
        exercises.forEach { exercise ->
            scope.launch {
                // Fetch goal reps and current reps for each exercise
                val goalFlow = exerciseRepository.getGoalForDate(exercise, day)
                val repsFlow = exerciseRepository.getRepsForDate(exercise, day)

                goalFlow.collect { goal ->
                    repsFlow.collect { reps ->
                        if (reps > 0) { // Only interested in exercises with non-zero reps
                            progressData[exercise] = reps to goal
                        }
                    }
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("OK")
            }
        },
        title = {
            Column {
                Text(text = day.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")))

                if (isRestDay) {
                    Text(
                        text = "Rest day",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        text = {
            // Display progress bars for each exercise that has non-zero reps
            Column {
                if (progressData.isEmpty()) {
                    Text("No exercises recorded for this day.")
                } else {
                    progressData.forEach { (exercise, data) ->
                        val (currentReps, goalReps) = data
                        Text("$exercise: $currentReps / $goalReps")

                        // Display a progress bar for the exercise
                        LinearProgressIndicator(
                            progress = { currentReps / goalReps.toFloat() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }
    )
}
