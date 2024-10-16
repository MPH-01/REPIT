package com.example.repit

sealed class Screen(val route: String) {
    object Today : Screen("today")
    object Calendar : Screen("calendar")
    object Stats : Screen("stats")
    object Settings : Screen("settings")
}
