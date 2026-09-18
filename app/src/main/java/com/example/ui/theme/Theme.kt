package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

enum class AppThemeSetting {
    GRAPHITE,
    MIDNIGHT,
    OCEAN,
    EMERALD,
    PURPLE,
    SYSTEM
}

enum class DarkModeSetting {
    SYSTEM,
    DARK,
    LIGHT
}

@Composable
fun CalcxTheme(
    themeSetting: AppThemeSetting = AppThemeSetting.GRAPHITE,
    darkModeSetting: DarkModeSetting = DarkModeSetting.DARK,
    content: @Composable () -> Unit
) {
    val darkTheme = when (darkModeSetting) {
        DarkModeSetting.DARK -> true
        DarkModeSetting.LIGHT -> false
        DarkModeSetting.SYSTEM -> isSystemInDarkTheme()
    }

    val context = LocalContext.current
    val colorScheme = when (themeSetting) {
        AppThemeSetting.SYSTEM -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (darkTheme) graphiteDarkScheme() else graphiteLightScheme()
            }
        }
        AppThemeSetting.MIDNIGHT -> midnightDarkScheme()
        AppThemeSetting.OCEAN -> if (darkTheme) oceanDarkScheme() else graphiteLightScheme()
        AppThemeSetting.EMERALD -> if (darkTheme) emeraldDarkScheme() else graphiteLightScheme()
        AppThemeSetting.PURPLE -> if (darkTheme) purpleDarkScheme() else graphiteLightScheme()
        AppThemeSetting.GRAPHITE -> if (darkTheme) graphiteDarkScheme() else graphiteLightScheme()
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

private fun graphiteDarkScheme() = darkColorScheme(
    primary = SlateDarkPrimary,
    onPrimary = Color.Black,
    secondary = SlateDarkSecondary,
    onSecondary = Color.Black,
    tertiary = SlateDarkTertiary,
    background = SlateDarkBackground,
    surface = SlateDarkSurface,
    surfaceVariant = SlateDarkSurfaceVariant,
    onBackground = Color(0xFFF1F5F9),
    onSurface = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF94A3B8)
)

private fun graphiteLightScheme() = lightColorScheme(
    primary = SlateLightPrimary,
    onPrimary = Color.White,
    secondary = SlateLightSecondary,
    onSecondary = Color.White,
    tertiary = SlateLightTertiary,
    background = SlateLightBackground,
    surface = SlateLightSurface,
    surfaceVariant = SlateLightSurfaceVariant,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A),
    onSurfaceVariant = Color(0xFF475569)
)

private fun midnightDarkScheme() = darkColorScheme(
    primary = MidnightPrimary,
    onPrimary = Color.White,
    secondary = Color(0xFF60A5FA),
    background = MidnightBackground,
    surface = MidnightSurface,
    surfaceVariant = Color(0xFF1E1E1E),
    onBackground = Color(0xFFFFFFFF),
    onSurface = Color(0xFFFFFFFF),
    onSurfaceVariant = Color(0xFFAAAAAA)
)

private fun oceanDarkScheme() = darkColorScheme(
    primary = OceanPrimary,
    onPrimary = Color.Black,
    secondary = Color(0xFF2DD4BF),
    background = OceanBackground,
    surface = OceanSurface,
    surfaceVariant = Color(0xFF133E68),
    onBackground = Color(0xFFE2E8F0),
    onSurface = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF94A3B8)
)

private fun emeraldDarkScheme() = darkColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.Black,
    secondary = Color(0xFF34D399),
    background = EmeraldBackground,
    surface = EmeraldSurface,
    surfaceVariant = Color(0xFF134235),
    onBackground = Color(0xFFE2E8F0),
    onSurface = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF94A3B8)
)

private fun purpleDarkScheme() = darkColorScheme(
    primary = PurpleThemePrimary,
    onPrimary = Color.White,
    secondary = Color(0xFFC084FC),
    background = PurpleThemeBackground,
    surface = PurpleThemeSurface,
    surfaceVariant = Color(0xFF331B54),
    onBackground = Color(0xFFF3E8FF),
    onSurface = Color(0xFFF3E8FF),
    onSurfaceVariant = Color(0xFFD8B4FE)
)
