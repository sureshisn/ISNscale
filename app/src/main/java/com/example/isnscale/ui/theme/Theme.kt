package com.example.isnscale.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = mdTheme_lightPrimary,
    onPrimary = mdTheme_lightOnPrimary,
    primaryContainer = mdTheme_lightPrimaryContainer,
    onPrimaryContainer = mdTheme_lightOnPrimaryContainer,
    secondary = mdTheme_lightSecondary,
    onSecondary = mdTheme_lightOnSecondary,
    secondaryContainer = mdTheme_lightSecondaryContainer,
    onSecondaryContainer = mdTheme_lightOnSecondaryContainer,
    tertiary = mdTheme_lightTertiary,
    onTertiary = mdTheme_lightOnTertiary,
    tertiaryContainer = mdTheme_lightTertiaryContainer,
    onTertiaryContainer = mdTheme_lightOnTertiaryContainer,
    error = mdTheme_lightError,
    onError = mdTheme_lightOnError,
    errorContainer = mdTheme_lightErrorContainer,
    onErrorContainer = mdTheme_lightOnErrorContainer,
    background = mdTheme_lightBackground,
    onBackground = mdTheme_lightOnBackground,
    surface = mdTheme_lightSurface,
    onSurface = mdTheme_lightOnSurface,
    surfaceVariant = mdTheme_lightSurfaceVariant,
    onSurfaceVariant = mdTheme_lightOnSurfaceVariant,
    outline = mdTheme_lightOutline,
    inverseOnSurface = mdTheme_lightInverseOnSurface,
    inverseSurface = mdTheme_lightInverseSurface,
    inverseTopAppBar = mdTheme_lightInverseTopAppBar,
    surfaceTint = mdTheme_lightSurfaceTint,
    outlineVariant = mdTheme_lightOutlineVariant,
    scrim = mdTheme_lightScrim
)

private val DarkColorScheme = darkColorScheme(
    primary = mdTheme_darkPrimary,
    onPrimary = mdTheme_darkOnPrimary,
    primaryContainer = mdTheme_darkPrimaryContainer,
    onPrimaryContainer = mdTheme_darkOnPrimaryContainer,
    secondary = mdTheme_darkSecondary,
    onSecondary = mdTheme_darkOnSecondary,
    secondaryContainer = mdTheme_darkSecondaryContainer,
    onSecondaryContainer = mdTheme_darkOnSecondaryContainer,
    tertiary = mdTheme_darkTertiary,
    onTertiary = mdTheme_darkOnTertiary,
    tertiaryContainer = mdTheme_darkTertiaryContainer,
    onTertiaryContainer = mdTheme_darkOnTertiaryContainer,
    error = mdTheme_darkError,
    onError = mdTheme_darkOnError,
    errorContainer = mdTheme_darkErrorContainer,
    onErrorContainer = mdTheme_darkOnErrorContainer,
    background = mdTheme_darkBackground,
    onBackground = mdTheme_darkOnBackground,
    surface = mdTheme_darkSurface,
    onSurface = mdTheme_darkOnSurface,
    surfaceVariant = mdTheme_darkSurfaceVariant,
    onSurfaceVariant = mdTheme_darkOnSurfaceVariant,
    outline = mdTheme_darkOutline,
    inverseOnSurface = mdTheme_darkInverseOnSurface,
    inverseSurface = mdTheme_darkInverseSurface,
    inverseTopAppBar = mdTheme_darkInverseTopAppBar,
    surfaceTint = mdTheme_darkSurfaceTint,
    outlineVariant = mdTheme_darkOutlineVariant,
    scrim = mdTheme_darkScrim
)

// Light theme colors
val mdTheme_lightPrimary = Color(0xFF006D44)
val mdTheme_lightOnPrimary = Color(0xFFFFFFFF)
val mdTheme_lightPrimaryContainer = Color(0xFF80FFB4)
val mdTheme_lightOnPrimaryContainer = Color(0xFF002A0F)
val mdTheme_lightSecondary = Color(0xFF625C6A)
val mdTheme_lightOnSecondary = Color(0xFFFFFFFF)
val mdTheme_lightSecondaryContainer = Color(0xFFE8DEFD)
val mdTheme_lightOnSecondaryContainer = Color(0xFF1E1824)
val mdTheme_lightTertiary = Color(0xFF495458)
val mdTheme_lightOnTertiary = Color(0xFFFFFFFF)
val mdTheme_lightTertiaryContainer = Color(0xFFD0E4E8)
val mdTheme_lightOnTertiaryContainer = Color(0xFF0E1519)
val mdTheme_lightError = Color(0xFFBA1B1B)
val mdTheme_lightOnError = Color(0xFFFFFFFF)
val mdTheme_lightErrorContainer = Color(0xFFFFDAD4)
val mdTheme_lightOnErrorContainer = Color(0xFF390B0B)
val mdTheme_lightBackground = Color(0xFFF8FFFF)
val mdTheme_lightOnBackground = Color(0xFF001F1F)
val mdTheme_lightSurface = Color(0xFFF8FFFF)
val mdTheme_lightOnSurface = Color(0xFF001F1F)
val mdTheme_lightSurfaceVariant = Color(0xFFE1D8E3)
val mdTheme_lightOnSurfaceVariant = Color(0xFF4A444E)
val mdTheme_lightOutline = Color(0xFF756F79)
val mdTheme_lightInverseOnSurface = Color(0xFF001F1F)
val mdTheme_lightInverseSurface = Color(0xFF003333)
val mdTheme_lightInverseTopAppBar = Color(0xFF80FFB4)
val mdTheme_lightSurfaceTint = Color(0xFF004E36)
val mdTheme_lightOutlineVariant = Color(0xFFC6BFC9)
val mdTheme_lightScrim = Color(0xFF000000)

// Dark theme colors
val mdTheme_darkPrimary = Color(0xFF80FFB4)
val mdTheme_darkOnPrimary = Color(0xFF003517)
val mdTheme_darkPrimaryContainer = Color(0xFF004E36)
val mdTheme_darkOnPrimaryContainer = Color(0xFF80FFB4)
val mdTheme_darkSecondary = Color(0xFFCCC3D4)
val mdTheme_darkOnSecondary = Color(0xFF352F3D)
val mdTheme_darkSecondaryContainer = Color(0xFF493F53)
val mdTheme_darkOnSecondaryContainer = Color(0xFFE8DEFD)
val mdTheme_darkTertiary = Color(0xFFBBD5D9)
val mdTheme_darkOnTertiary = Color(0xFF242E32)
val mdTheme_darkTertiaryContainer = Color(0xFF364146)
val mdTheme_darkOnTertiaryContainer = Color(0xFFBBD5D9)
val mdTheme_darkError = Color(0xFFFFB4AB)
val mdTheme_darkOnError = Color(0xFF521616)
val mdTheme_darkErrorContainer = Color(0xFF781C1C)
val mdTheme_darkOnErrorContainer = Color(0xFFFFB4AB)
val mdTheme_darkBackground = Color(0xFF001F1F)
val mdTheme_darkOnBackground = Color(0xFF80FFB4)
val mdTheme_darkSurface = Color(0xFF001F1F)
val mdTheme_darkOnSurface = Color(0xFF80FFB4)
val mdTheme_darkSurfaceVariant = Color(0xFF3A333D)
val mdTheme_darkOnSurfaceVariant = Color(0xFFC6BFC9)
val mdTheme_darkOutline = Color(0xFF918A93)
val mdTheme_darkInverseOnSurface = Color(0xFF80FFB4)
val mdTheme_darkInverseSurface = Color(0xFF333333)
val mdTheme_darkInverseTopAppBar = Color(0xFF003517)
val mdTheme_darkSurfaceTint = Color(0xFF006D44)
val mdTheme_darkOutlineVariant = Color(0xFF4A444E)
val mdTheme_darkScrim = Color(0xFF000000)

@Composable
fun ISNscaleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            dynamicLightColorScheme(context = context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(
            headlineLarge = androidx.compose.material3.Typography.headlineLarge.copy(
                fontFamily = androidx.compose.ui.text.font.FontFamily(
                    androidx.compose.ui.text.font.Font(
                        androidx.compose.ui.res.stringResource(id = android.R.string.ok)
                    )
                )
            )
        ),
        shapes = Shapes(
            small = RoundedCornerShape(4.dp),
            medium = RoundedCornerShape(8.dp),
            large = RoundedCornerShape(16.dp)
        ),
        content = content
    )
}

private val Shapes = Shapes()