package com.example.repit

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.repit.data.ExerciseRepository
import kotlinx.coroutines.launch
import java.time.DayOfWeek

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SettingsPage(exerciseRepository: ExerciseRepository) {
    val daysOfWeek = DayOfWeek.entries
    val scope = rememberCoroutineScope()
    val selectedRestDays = remember { mutableStateMapOf<DayOfWeek, Boolean>().apply { daysOfWeek.forEach { put(it, false) } } }
    val restDayCount by remember { derivedStateOf { selectedRestDays.count { it.value } } }

    LaunchedEffect(Unit) {
        val restDaySettings = exerciseRepository.getRestDaySettings()
        restDaySettings.forEach { (dayOfWeek, isRestDay) ->
            selectedRestDays[dayOfWeek] = isRestDay
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Select Rest Days (Up to 3)")
        Spacer(modifier = Modifier.height(16.dp))

        daysOfWeek.forEach { day ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = day.name)
                Checkbox(
                    checked = selectedRestDays[day] == true,
                    onCheckedChange = { isChecked ->
                        if (isChecked && restDayCount < 3 || !isChecked) {
                            selectedRestDays[day] = isChecked
                            scope.launch {
                                exerciseRepository.setRestDay(day, isChecked)
                            }
                        }
                    }
                )
            }
        }
    }
}
