package com.example.repit

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
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
import com.example.repit.data.ExerciseDatabase
import com.example.repit.data.ExerciseRepository
import kotlinx.coroutines.flow.Flow

import com.example.repit.ui.theme.REPITTheme
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    private lateinit var exerciseRepository: ExerciseRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialise the database and repository
        val database = ExerciseDatabase.getDatabase(applicationContext)
        exerciseRepository = ExerciseRepository(database.exerciseLogDao())

        setContent {
            REPITTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    MainScreen(exerciseRepository)
                }
            }
        }
    }
}

@Composable
fun MainScreen(exerciseRepository: ExerciseRepository) {
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
            exerciseRepository = exerciseRepository
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
                    when (screen.route) {
                        Screen.Today.route -> Icon(Icons.Default.Check, contentDescription = "Today")
                        Screen.Calendar.route -> Icon(Icons.Default.DateRange, contentDescription = "Calendar")
                        Screen.Stats.route -> Icon(Icons.Default.Info, contentDescription = "Stats")
                        Screen.Settings.route -> Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
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
    exerciseRepository: ExerciseRepository
) {
    NavHost(navController, startDestination = Screen.Today.route, modifier = modifier) {
        composable(Screen.Today.route) {
            TodayPage(
                selectedExercise = selectedExercise,
                onExerciseSelected = onExerciseSelected,
                exerciseRepository = exerciseRepository
            )
        }
        composable(Screen.Calendar.route) {
            CalendarPage(exerciseRepository = exerciseRepository)
        }
        composable(Screen.Stats.route) {
            StatsPage(exerciseRepository = exerciseRepository)
        }
        composable(Screen.Settings.route) { SettingsPage() }
    }
}
