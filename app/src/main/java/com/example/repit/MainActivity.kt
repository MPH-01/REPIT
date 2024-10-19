package com.example.repit

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.Flow

import com.example.repit.ui.theme.REPITTheme
import kotlinx.coroutines.flow.flowOf

class MainActivity : ComponentActivity() {
    private lateinit var exercisePreferences: ExercisePreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exercisePreferences = ExercisePreferences(applicationContext)

        setContent {
            REPITTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    MainScreen(exercisePreferences)
                }
            }
        }
    }
}

@Composable
fun MainScreen(exercisePreferences: ExercisePreferences) {
    var selectedExercise by remember { mutableStateOf("Push ups") }
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { innerPadding ->
        NavigationHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
            selectedExercise = selectedExercise,
            onExerciseSelected = { exercise -> selectedExercise = exercise },
            exercisePreferences = exercisePreferences
        )
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        Screen.Today,
        Screen.Calendar,
        Screen.Stats,
        Screen.Settings
    )

    NavigationBar {
        items.forEach { screen ->
            NavigationBarItem(
                label = { Text(screen.route.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }) },
                selected = false,
                onClick = {
                    navController.navigate(screen.route)
                },
                icon = {
                    // Placeholder icon (you can replace with actual icons)
                    Icon(Icons.Default.Home, contentDescription = null)
                }
            )
        }
    }
}

@SuppressLint("NewApi")
@Composable
fun NavigationHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    selectedExercise: String,
    onExerciseSelected: (String) -> Unit,
    exercisePreferences: ExercisePreferences
) {
    NavHost(navController, startDestination = Screen.Today.route, modifier = modifier) {
        composable(Screen.Today.route) {
            TodayPage(
                selectedExercise = selectedExercise,
                onExerciseSelected = onExerciseSelected,
                exercisePreferences = exercisePreferences
            )
        }
        composable(Screen.Calendar.route) { CalendarPage() }
        composable(Screen.Stats.route) { StatsPage() }
        composable(Screen.Settings.route) { SettingsPage() }
    }
}

// Mock ExercisePreferences for Preview
class MockExercisePreferences : ExercisePreferences(null) {
    override fun getGoal(exercise: String): Flow<Int> {
        return flowOf(25) // Return a default goal value for preview
    }

    override suspend fun setGoal(exercise: String, goal: Int) {
        // No-op for preview
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MainScreen(
        exercisePreferences = MockExercisePreferences() // Use the mock for preview
    )
}

