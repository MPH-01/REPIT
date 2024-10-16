package com.example.repit

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.example.repit.ui.theme.REPITTheme // Import your theme if you have one

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            REPITTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { innerPadding ->
        NavigationHost(navController = navController, modifier = Modifier.padding(innerPadding))
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
                selected = false, // Update this based on actual selection logic
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

@Composable
fun NavigationHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController, startDestination = Screen.Today.route, modifier = modifier) {
        composable(Screen.Today.route) { TodayPage() }
        composable(Screen.Calendar.route) { CalendarPage() }
        composable(Screen.Stats.route) { StatsPage() }
        composable(Screen.Settings.route) { SettingsPage() }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MainScreen()
}

