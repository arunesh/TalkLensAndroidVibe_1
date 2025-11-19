package com.talkbox.docs.talklens.core.designsystem.theme

import androidx.compose.ui.graphics.Color

// Primary colors - Blue theme for TalkLens
val Primary = Color(0xFF1976D2) // Material Blue 700
val PrimaryVariant = Color(0xFF1565C0) // Material Blue 800
val PrimaryLight = Color(0xFF42A5F5) // Material Blue 400

val Secondary = Color(0xFF26A69A) // Material Teal 400
val SecondaryVariant = Color(0xFF00897B) // Material Teal 600

val Tertiary = Color(0xFFFF6F00) // Material Orange 800

// Background colors
val BackgroundLight = Color(0xFFFAFAFA)
val BackgroundDark = Color(0xFF121212)

val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceDark = Color(0xFF1E1E1E)

// Text colors
val TextPrimaryLight = Color(0xFF212121)
val TextSecondaryLight = Color(0xFF757575)
val TextPrimaryDark = Color(0xFFE0E0E0)
val TextSecondaryDark = Color(0xFFB0B0B0)

// Status colors
val Success = Color(0xFF4CAF50)
val Warning = Color(0xFFFFC107)
val Error = Color(0xFFF44336)
val Info = Color(0xFF2196F3)

// Translation specific colors
val SourceTextBackground = Color(0xFFE3F2FD) // Light blue
val TranslatedTextBackground = Color(0xFFE0F2F1) // Light teal

// Document boundary overlay
val DocumentBoundary = Color(0xFF4CAF50).copy(alpha = 0.6f)
val DocumentBoundaryError = Color(0xFFF44336).copy(alpha = 0.6f)
