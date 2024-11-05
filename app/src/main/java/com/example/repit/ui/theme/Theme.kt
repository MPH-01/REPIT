package com.example.repit.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// Dark Theme Colors
private val DarkColorScheme = darkColorScheme(
    primary = AccentBlue,
    secondary = HighlightTeal,
    tertiary = MotivationalOrange,
    background = DarkBlue,
    surface = DarkBlue,
    onPrimary = PrimaryText,
    onSecondary = PrimaryText,
    onTertiary = PrimaryText,
    onBackground = OffWhite,
    onSurface = OffWhite,
    error = AlertRed,
    onError = PrimaryText
)

// Light Theme Colors
private val LightColorScheme = lightColorScheme(
    primary = AccentBlue,
    secondary = HighlightTeal,
    tertiary = MotivationalOrange,
    background = OffWhite,
    surface = DarkBlue,
    onPrimary = DarkBlue,
    onSecondary = DarkBlue,
    onTertiary = DarkBlue,
    onBackground = DarkBlue,
    onSurface = DarkBlue,
    error = AlertRed,
    onError = PrimaryText
)

@Composable
fun REPITTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme // Light colour scheme is disgusting at the moment
//        when {
//        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
//            val context = LocalContext.current
//            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
//        }
//
//        darkTheme -> DarkColorScheme
//        else -> LightColorScheme
//    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
