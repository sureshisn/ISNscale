package com.example.isnscale.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Set of Material3 typography styles
val Typography = Typography(
    headlineLarge = androidx.compose.material3.Typography.headlineLarge.copy(
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp
    ),
    headlineMedium = androidx.compose.material3.Typography.headlineMedium.copy(
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp
    ),
    headlineSmall = androidx.compose.material3.Typography.headlineSmall.copy(
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp
    ),
    titleLarge = androidx.compose.material3.Typography.titleLarge.copy(
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp
    ),
    titleMedium = androidx.compose.material3.Typography.titleMedium.copy(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp
    ),
    titleSmall = androidx.compose.material3.Typography.titleSmall.copy(
        fontWeight = FontWeightMedium,
        fontSize = 14.sp
    ),
    bodyLarge = androidx.compose.material3.Typography.bodyLarge.copy(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    bodyMedium = androidx.compose.material3.Typography.bodyMedium.copy(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    ),
    bodySmall = androidx.compose.material3.Typography.bodySmall.copy(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    ),
    labelLarge = androidx.compose.material3.Typography.labelLarge.copy(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    ),
    labelMedium = androidx.compose.material3.Typography.labelMedium.copy(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp
    ),
    labelSmall = androidx.compose.material3.Typography.labelSmall.copy(
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp
    )
)

// Custom font family (optional - uses system default)
val AppFontFamily = FontFamily()