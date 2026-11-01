package com.mihnati.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color

// ==========================================
// ☀️ LIGHT THEME PALETTE (Moroccan Chérifien)
// ==========================================
val LightPrimary = Color(0xFF003EC8)
val LightPrimaryContainer = Color(0xFF0D53FC)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightOnPrimaryContainer = Color(0xFFE0E3FF)
val LightPrimaryFixed = Color(0xFFDDE1FF)
val LightOnPrimaryFixed = Color(0xFF001452)
val LightPrimaryFixedDim = Color(0xFFB7C4FF)

val LightSecondary = Color(0xFF006C4A)
val LightSecondaryContainer = Color(0xFF82F5C1)
val LightOnSecondaryContainer = Color(0xFF00714E)
val LightSecondaryFixed = Color(0xFF85F8C4)
val LightSecondaryFixedDim = Color(0xFF68DBA9)
val LightOnSecondaryFixed = Color(0xFF002114)
val LightOnSecondaryFixedVariant = Color(0xFF005137)

val LightTertiary = Color(0xFF793F00)
val LightTertiaryContainer = Color(0xFF9C5300)
val LightOnTertiaryContainer = Color(0xFFFFDFC8)
val LightTertiaryFixed = Color(0xFFFFDCC3)
val LightOnTertiaryFixed = Color(0xFF2F1500)
val LightTertiaryFixedDim = Color(0xFFFFB77D)

val LightSurface = Color(0xFFFAF8FF)
val LightSurfaceContainerLowest = Color(0xFFFFFFFF)
val LightSurfaceContainerLow = Color(0xFFF2F3FF)
val LightSurfaceContainer = Color(0xFFEAEDFF)
val LightSurfaceContainerHigh = Color(0xFFE2E7FF)
val LightSurfaceContainerHighest = Color(0xFFDAE2FD)
val LightSurfaceVariant = Color(0xFFDAE2FD)

val LightOnSurface = Color(0xFF131B2E)
val LightOnSurfaceVariant = Color(0xFF434656)
val LightOutline = Color(0xFF737688)
val LightOutlineVariant = Color(0xFFC3C5D9)

val LightError = Color(0xFFBA1A1A)
val LightErrorContainer = Color(0xFFFFDAD6)
val LightOnErrorContainer = Color(0xFF93000A)
val LightOnError = Color(0xFFFFFFFF)

// ==========================================
// 🌙 DARK THEME PALETTE (Moroccan Deep Emerald & Obsidian)
// ==========================================
val DarkPrimary = Color(0xFF8BAAFF)
val DarkPrimaryContainer = Color(0xFF003ABF)
val DarkOnPrimary = Color(0xFF001F6E)
val DarkOnPrimaryContainer = Color(0xFFDCE2FF)
val DarkPrimaryFixed = Color(0xFF1D325E)
val DarkOnPrimaryFixed = Color(0xFFDCE2FF)
val DarkPrimaryFixedDim = Color(0xFF6E91ED)

val DarkSecondary = Color(0xFF4EE2A4)
val DarkSecondaryContainer = Color(0xFF005237)
val DarkOnSecondaryContainer = Color(0xFF80FBC0)
val DarkSecondaryFixed = Color(0xFF003825)
val DarkSecondaryFixedDim = Color(0xFF38B280)
val DarkOnSecondaryFixed = Color(0xFF80FBC0)
val DarkOnSecondaryFixedVariant = Color(0xFF55D89F)

val DarkTertiary = Color(0xFFFFB77D)
val DarkTertiaryContainer = Color(0xFF6B3600)
val DarkOnTertiaryContainer = Color(0xFFFFDCC3)
val DarkTertiaryFixed = Color(0xFF482300)
val DarkOnTertiaryFixed = Color(0xFFFFDCC3)
val DarkTertiaryFixedDim = Color(0xFFDF9258)

// Deep AMOLED dark slate surfaces
val DarkSurface = Color(0xFF0B101B)
val DarkSurfaceContainerLowest = Color(0xFF070B13)
val DarkSurfaceContainerLow = Color(0xFF101726)
val DarkSurfaceContainer = Color(0xFF161F32)
val DarkSurfaceContainerHigh = Color(0xFF1E2A42)
val DarkSurfaceContainerHighest = Color(0xFF263654)
val DarkSurfaceVariant = Color(0xFF1A253B)

val DarkOnSurface = Color(0xFFE8EEFC)
val DarkOnSurfaceVariant = Color(0xFFA5B2CD)
val DarkOutline = Color(0xFF4D5E80)
val DarkOutlineVariant = Color(0xFF2B3A54)

val DarkError = Color(0xFFFFB4AB)
val DarkErrorContainer = Color(0xFF93000A)
val DarkOnErrorContainer = Color(0xFFFFDAD6)
val DarkOnError = Color(0xFF690005)

// ==========================================
// 🎨 DYNAMIC ADAPTIVE COLOR GETTERS (Adapts automatically!)
// ==========================================
val Primary: Color
    @Composable get() = if (ThemeManager.isDarkMode.collectAsState().value) DarkPrimary else LightPrimary

val PrimaryContainer: Color
    @Composable get() = if (ThemeManager.isDarkMode.collectAsState().value) DarkPrimaryContainer else LightPrimaryContainer

val OnPrimary: Color
    @Composable get() = if (ThemeManager.isDarkMode.collectAsState().value) DarkOnPrimary else LightOnPrimary

val OnPrimaryContainer: Color
    @Composable get() = if (ThemeManager.isDarkMode.collectAsState().value) DarkOnPrimaryContainer else LightOnPrimaryContainer

val PrimaryFixed: Color
    @Composable get() = if (ThemeManager.isDarkMode.collectAsState().value) DarkPrimaryFixed else LightPrimaryFixed

val OnPrimaryFixed: Color
    @Composable get() = if (ThemeManager.isDarkMode.collectAsState().value) DarkOnPrimaryFixed else LightOnPrimaryFixed

val PrimaryFixedDim: Color
    @Composable get() = if (ThemeManager.isDarkMode.collectAsState().value) DarkPrimaryFixedDim else LightPrimaryFixedDim

val Secondary: Color
    @Composable get() = if (ThemeManager.isDarkMode.collectAsState().value) DarkSecondary else LightSecondary

val SecondaryContainer: Color
    @Composable get() = if (ThemeManager.isDarkMode.collectAsState().value) DarkSecondaryContainer else LightSecondaryContainer

val OnSecondaryContainer: Color
    @Composable get() = if (ThemeManager.isDarkMode.collectAsState().value) DarkOnSecondaryContainer else LightOnSecondaryContainer

val SecondaryFixed: Color
    @Composable get() = if (ThemeManager.isDarkMode.collectAsState().value) DarkSecondaryFixed else LightSecondaryFixed

val SecondaryFixedDim: Color
    @Composable get() = if (ThemeManager.isDarkMode.collectAsState().value) DarkSecondaryFixedDim else LightSecondaryFixedDim

val OnSecondaryFixed: Color
    @Composable get() = if (ThemeManager.isDarkMode.collectAsState().value) DarkOnSecondaryFixed else LightOnSecondaryFixed

val OnSecondaryFixedVariant: Color
    @Composable get() = if (ThemeManager.isDarkMode.collectAsState().value) DarkOnSecondaryFixedVariant else LightOnSecondaryFixedVariant

val Tertiary: Color
    @Composable get() = if (ThemeManager.isDarkMode.collectAsState().value) DarkTertiary else LightTertiary

val TertiaryContainer: Color
    @Composable get() = if (ThemeManager.isDarkMode.collectAsState().value) DarkTertiaryContainer else LightTertiaryContainer

val OnTertiaryContainer: Color
    @Composable get() = if (ThemeManager.isDarkMode.collectAsState().value) DarkOnTertiaryContainer else LightOnTertiaryContainer

val TertiaryFixed: Color
    @Composable get() = if (ThemeManager.isDarkMode.collectAsState().value) DarkTertiaryFixed else LightTertiaryFixed

val OnTertiaryFixed: Color
    @Composable get() = if (ThemeManager.isDarkMode.collectAsState().value) DarkOnTertiaryFixed else LightOnTertiaryFixed

val TertiaryFixedDim: Color
    @Composable get() = if (ThemeManager.isDarkMode.collectAsState().value) DarkTertiaryFixedDim else LightTertiaryFixedDim

val Surface: Color
    @Composable get() = if (ThemeManager.isDarkMode.collectAsState().value) DarkSurface else LightSurface

val SurfaceContainerLowest: Color
    @Composable get() = if (ThemeManager.isDarkMode.collectAsState().value) DarkSurfaceContainerLowest else LightSurfaceContainerLowest

val SurfaceContainerLow: Color
    @Composable get() = if (ThemeManager.isDarkMode.collectAsState().value) DarkSurfaceContainerLow else LightSurfaceContainerLow

val SurfaceContainer: Color
    @Composable get() = if (ThemeManager.isDarkMode.collectAsState().value) DarkSurfaceContainer else LightSurfaceContainer

val SurfaceContainerHigh: Color
    @Composable get() = if (ThemeManager.isDarkMode.collectAsState().value) DarkSurfaceContainerHigh else LightSurfaceContainerHigh

val SurfaceContainerHighest: Color
    @Composable get() = if (ThemeManager.isDarkMode.collectAsState().value) DarkSurfaceContainerHighest else LightSurfaceContainerHighest

val SurfaceVariant: Color
    @Composable get() = if (ThemeManager.isDarkMode.collectAsState().value) DarkSurfaceVariant else LightSurfaceVariant

val OnSurface: Color
    @Composable get() = if (ThemeManager.isDarkMode.collectAsState().value) DarkOnSurface else LightOnSurface

val OnSurfaceVariant: Color
    @Composable get() = if (ThemeManager.isDarkMode.collectAsState().value) DarkOnSurfaceVariant else LightOnSurfaceVariant

val Outline: Color
    @Composable get() = if (ThemeManager.isDarkMode.collectAsState().value) DarkOutline else LightOutline

val OutlineVariant: Color
    @Composable get() = if (ThemeManager.isDarkMode.collectAsState().value) DarkOutlineVariant else LightOutlineVariant

val Error: Color
    @Composable get() = if (ThemeManager.isDarkMode.collectAsState().value) DarkError else LightError

val ErrorContainer: Color
    @Composable get() = if (ThemeManager.isDarkMode.collectAsState().value) DarkErrorContainer else LightErrorContainer

val OnErrorContainer: Color
    @Composable get() = if (ThemeManager.isDarkMode.collectAsState().value) DarkOnErrorContainer else LightOnErrorContainer

val OnError: Color
    @Composable get() = if (ThemeManager.isDarkMode.collectAsState().value) DarkOnError else LightOnError
