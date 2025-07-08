package com.example.blockchainaccess.ui.theme

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
import androidx.compose.ui.res.colorResource
import com.example.blockchainaccess.R

@Composable
fun BlockchainAccessTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val darkColorScheme = darkColorScheme(
        primary = colorResource(R.color.color_primary_dark),
        onPrimary = colorResource(R.color.color_on_primary_dark),
        secondary = colorResource(R.color.color_secondary_dark),
        onSecondary = colorResource(R.color.color_on_secondary_dark),
        background = colorResource(R.color.color_background_dark),
        onBackground = colorResource(R.color.color_on_background_dark),
        surface = colorResource(R.color.color_surface_dark),
        onSurface = colorResource(R.color.color_on_surface_dark),
    )
    val lightColorScheme = lightColorScheme(
        primary = colorResource(R.color.color_primary),
        onPrimary = colorResource(R.color.color_on_primary),
        secondary = colorResource(R.color.color_secondary),
        onSecondary = colorResource(R.color.color_on_secondary),
        background = colorResource(R.color.color_background),
        onBackground = colorResource(R.color.color_on_background),
        surface = colorResource(R.color.color_surface),
        onSurface = colorResource(R.color.color_on_surface),
    )


    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> darkColorScheme
        else -> lightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}