package com.example.repit.ui.theme

import android.app.Activity
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
    primary = ElectricBlue,
    secondary = LimeGreen,
    tertiary = FieryOrange,
    background = DarkGray,
    surface = DarkGray,
    onPrimary = PrimaryText,
    onSecondary = PrimaryText,
    onTertiary = PrimaryText,
    onBackground = SecondaryText,
    onSurface = SecondaryText,
    error = AlertRed,
    onError = PrimaryText
)

// Light Theme Colors
private val LightColorScheme = lightColorScheme(
    primary = ElectricBlue,
    secondary = LimeGreen,
    tertiary = FieryOrange,
    background = OffWhite,
    surface = LightGray,
    onPrimary = DarkGray,
    onSecondary = DarkGray,
    onTertiary = DarkGray,
    onBackground = DarkGray,
    onSurface = DarkGray,
    error = AlertRed,
    onError = PrimaryText
)

@Composable
fun REPITTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
