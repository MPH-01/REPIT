package com.example.repit

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.repit.data.ExerciseDatabase
import com.example.repit.data.ExerciseRepository
import com.example.repit.ui.theme.REPITTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var exerciseRepository: ExerciseRepository

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialise the database and repository
        val database = ExerciseDatabase.getDatabase(applicationContext)
        exerciseRepository = ExerciseRepository(database.exerciseLogDao())

        // Coroutine to initialize today's records when the app opens
        val exercises = listOf("Push ups", "Sit ups", "Squats", "Pull ups") // List of exercises
        lifecycleScope.launch {
            exerciseRepository.initializeTodayRecords(exercises)
        }

        setContent {
            REPITTheme(dynamicColor = false) {
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

    // Colour variables for selected and unselected states
    val selectedColor = MaterialTheme.colorScheme.primary
    val unselectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)

    val currentDestination by navController.currentBackStackEntryAsState()

    NavigationBar (
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        items.forEach { screen ->
            val isSelected = currentDestination?.destination?.route == screen.route

            NavigationBarItem(
                label = {
                    Text(
                        text = screen.route.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                        color = if (isSelected) selectedColor else unselectedColor
                    )
                },
                selected = isSelected,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = when (screen.route) {
                            Screen.Today.route -> Icons.Default.Check
                            Screen.Calendar.route -> Icons.Default.DateRange
                            Screen.Stats.route -> Icons.Default.Info
                            Screen.Settings.route -> Icons.Default.Settings
                            else -> Icons.Default.Check
                        },
                        contentDescription = screen.route,
                        tint = if (isSelected) selectedColor else unselectedColor
                    )
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
        composable(Screen.Settings.route) {
            SettingsPage(exerciseRepository = exerciseRepository)
        }
    }
}
