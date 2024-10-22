package com.example.repit

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@SuppressLint("NewApi")
@Composable
fun CalendarPage() {
    // Get the current date
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    val scope = rememberCoroutineScope()

    // Initialise LazyListState to start from the current month (index 50)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = 50)
    val currentYearMonth = YearMonth.now()

    // Define a lazy column to scroll through months
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
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
                }
            )
        }
    }

    // Show the popup when a day is selected
    selectedDate?.let { day ->
        DayPopup(day = day) {
            selectedDate = null
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MonthView(
    yearMonth: YearMonth,
    selectedDate: LocalDate?,
    onDayClick: (LocalDate) -> Unit
) {
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
                            // Display the valid days of the month
                            DayItem(
                                day = daysInMonth[cellIndex - firstDayOfMonth],
                                isSelected = selectedDate == daysInMonth[cellIndex - firstDayOfMonth],
                                onClick = { onDayClick(daysInMonth[cellIndex - firstDayOfMonth]) }
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
fun DayItem(day: LocalDate, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .padding(4.dp)
            .size(40.dp)
            .clickable { onClick() }
            .background(
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp)
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
fun DayPopup(day: LocalDate, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("OK")
            }
        },
        title = {
            Text(text = "Selected Date: ${day.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))}")
        },
        text = {
            Text("This is a placeholder popup for the selected day.")
        }
    )
}

@Preview(showBackground = true)
@Composable
fun CalendarPagePreview() {
    CalendarPage()
}
