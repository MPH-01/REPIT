package com.example.repit

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.repit.data.ExerciseRepository
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SettingsPage(exerciseRepository: ExerciseRepository) {
    var isRestDaysDialogOpen by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Rest Days Option
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = MaterialTheme.shapes.medium,
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Rest Days", style = MaterialTheme.typography.bodyLarge)
                Button(
                    onClick = { isRestDaysDialogOpen = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    )
                ) {
                    Text("Select Days")
                }
            }
        }

        // Open rest days dialog when "Select Days" is clicked
        if (isRestDaysDialogOpen) {
            RestDaysDialog(
                exerciseRepository = exerciseRepository,
                onDismiss = { isRestDaysDialogOpen = false }
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RestDaysDialog(
    exerciseRepository: ExerciseRepository,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val daysOfWeek = listOf(
        DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY
    )
    val selectedRestDays = remember { mutableStateMapOf<DayOfWeek, Boolean>().apply { daysOfWeek.forEach { put(it, false) } } }
    val restDayCount by remember { derivedStateOf { selectedRestDays.count { it.value } } }

    LaunchedEffect(Unit) {
        val restDaySettings = exerciseRepository.getRestDaySettings()
        restDaySettings.forEach { (dayOfWeek, isRestDay) ->
            selectedRestDays[dayOfWeek] = isRestDay
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Text(
                    text = "Select Rest Days",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Warning message for max selection
                if (restDayCount >= 3) {
                    Text(
                        text = "You can only select up to 3 rest days.",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                daysOfWeek.forEach { day ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = day.name.lowercase().replaceFirstChar { it.titlecase(Locale.getDefault()) },
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Checkbox(
                            checked = selectedRestDays[day] == true,
                            onCheckedChange = { isChecked ->
                                if ((isChecked && restDayCount < 3) || !isChecked) {
                                    selectedRestDays[day] = isChecked
                                    scope.launch {
                                        exerciseRepository.setRestDay(day, isChecked)
                                    }
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Done")
                }
            }
        }
    }
}
