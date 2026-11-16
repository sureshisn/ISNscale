package com.example.isnscale.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Default Material 3 typography used as the base for the custom scale.
private val MaterialDefaults = Typography()

val AppTypography = Typography(
    headlineLarge = MaterialDefaults.headlineLarge.copy(
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp
    ),
    headlineMedium = MaterialDefaults.headlineMedium.copy(
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp
    ),
    headlineSmall = MaterialDefaults.headlineSmall.copy(
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp
    ),
    titleLarge = MaterialDefaults.titleLarge.copy(
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp
    ),
    titleMedium = MaterialDefaults.titleMedium.copy(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp
    ),
    titleSmall = MaterialDefaults.titleSmall.copy(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    ),
    bodyLarge = MaterialDefaults.bodyLarge.copy(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    bodyMedium = MaterialDefaults.bodyMedium.copy(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    ),
    bodySmall = MaterialDefaults.bodySmall.copy(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    ),
    labelLarge = MaterialDefaults.labelLarge.copy(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    ),
    labelMedium = MaterialDefaults.labelMedium.copy(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp
    ),
    labelSmall = MaterialDefaults.labelSmall.copy(
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp
    )
)
