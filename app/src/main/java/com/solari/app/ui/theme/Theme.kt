package com.solari.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

enum class SolariThemeVariant(val displayName: String) {
    DEFAULT_DARK("Solari Dark"),
    DEFAULT_LIGHT("Solari Light"),
    OCEAN_DARK("Ocean Dark"),
    GRUVBOX_DARK("Gruvbox Dark"),
    SOLARIZED_DARK("Solarized Dark"),
    NORD("Nord"),
    DRACULA("Dracula"),
    MONOKAI("Monokai")
}

data class SolariColors(
    val name: String = "Theme",
    val isDark: Boolean = true,
    val primary: Color = Color(0xFFF98028),
    val secondary: Color = Color(0xFFFFB68A),
    val tertiary: Color = Color(0xFFDDC1B1),
    val background: Color = Color(0xFF111316),
    val surface: Color = Color(0xFF1A1A1F),
    val surfaceVariant: Color = Color(0xFF1F1F23),
    val onPrimary: Color = Color.Black,
    val onSecondary: Color = Color.Black,
    val onTertiary: Color = Color.Black,
    val onBackground: Color = Color.White,
    val onSurface: Color = Color.White,
    val onSurfaceVariant: Color = Color.White,
    val navBarColor: Color = Color.Black
)

val DefaultSolariDark = SolariColors(
    name = "Solari Dark",
    isDark = true
)

val DefaultSolariLight = SolariColors(
    name = "Solari Light",
    isDark = false,
    primary = Color(0xFFF98028),
    background = Color.White,
    surface = Color(0xFFF0F0F0),
    surfaceVariant = Color(0xFFE0E0E0),
    onBackground = Color.Black,
    onSurface = Color.Black,
    onSurfaceVariant = Color.Black
)

val OceanDark = SolariColors(
    name = "Ocean Dark",
    isDark = true,
    primary = Color(0xFF0077FF),
    secondary = Color(0xFF80BFFF),
    background = Color(0xFF001122),
    surface = Color(0xFF002233),
    surfaceVariant = Color(0xFF003344)
)

val GruvboxDark = SolariColors(
    name = "Gruvbox Dark",
    isDark = true,
    primary = Color(0xFFFABD2F),
    secondary = Color(0xFF83A598),
    tertiary = Color(0xFF8EC07C),
    background = Color(0xFF282828),
    surface = Color(0xFF3C3836),
    surfaceVariant = Color(0xFF504945),
    onBackground = Color(0xFFEBDBB2),
    onSurface = Color(0xFFEBDBB2),
    onSurfaceVariant = Color(0xFFBDAE93)
)

val SolarizedDark = SolariColors(
    name = "Solarized Dark",
    isDark = true,
    primary = Color(0xFF268BD2),
    secondary = Color(0xFF2AA198),
    tertiary = Color(0xFF859900),
    background = Color(0xFF002B36),
    surface = Color(0xFF073642),
    surfaceVariant = Color(0xFF586E75),
    onBackground = Color(0xFF839496),
    onSurface = Color(0xFF93A1A1),
    onSurfaceVariant = Color(0xFF657B83)
)

val NordTheme = SolariColors(
    name = "Nord",
    isDark = true,
    primary = Color(0xFF88C0D0),
    secondary = Color(0xFF81A1C1),
    tertiary = Color(0xFF8FBCBB),
    background = Color(0xFF2E3440),
    surface = Color(0xFF3B4252),
    surfaceVariant = Color(0xFF434C5E),
    onBackground = Color(0xFFECEFF4),
    onSurface = Color(0xFFECEFF4),
    onSurfaceVariant = Color(0xFFD8DEE9)
)

val DraculaTheme = SolariColors(
    name = "Dracula",
    isDark = true,
    primary = Color(0xFFBD93F9),
    secondary = Color(0xFFFF79C6),
    tertiary = Color(0xFF8BE9FD),
    background = Color(0xFF282A36),
    surface = Color(0xFF44475A),
    surfaceVariant = Color(0xFF6272A4),
    onBackground = Color(0xFFF8F8F2),
    onSurface = Color(0xFFF8F8F2),
    onSurfaceVariant = Color(0xFFF8F8F2)
)

val MonokaiTheme = SolariColors(
    name = "Monokai",
    isDark = true,
    primary = Color(0xFFF92672),
    secondary = Color(0xFFFD971F),
    tertiary = Color(0xFFA6E22E),
    background = Color(0xFF272822),
    surface = Color(0xFF3E3D32),
    surfaceVariant = Color(0xFF49483E),
    onBackground = Color(0xFFF8F8F2),
    onSurface = Color(0xFFF8F8F2),
    onSurfaceVariant = Color(0xFFF8F8F2)
)

val ThemeMap: Map<SolariThemeVariant, SolariColors> = mapOf(
    SolariThemeVariant.DEFAULT_DARK to DefaultSolariDark,
    SolariThemeVariant.DEFAULT_LIGHT to DefaultSolariLight,
    SolariThemeVariant.OCEAN_DARK to OceanDark,
    SolariThemeVariant.GRUVBOX_DARK to GruvboxDark,
    SolariThemeVariant.SOLARIZED_DARK to SolarizedDark,
    SolariThemeVariant.NORD to NordTheme,
    SolariThemeVariant.DRACULA to DraculaTheme,
    SolariThemeVariant.MONOKAI to MonokaiTheme
)

val LocalSolariColors = staticCompositionLocalOf { DefaultSolariDark }

object SolariTheme {
    val colors: SolariColors
        @Composable
        get() = LocalSolariColors.current
}

@Composable
fun SolariTheme(
    variant: SolariThemeVariant = SolariThemeVariant.DEFAULT_DARK,
    content: @Composable () -> Unit
) {
    val colors = ThemeMap[variant] ?: DefaultSolariDark
    val materialColors = if (colors.isDark) {
        darkColorScheme(
            primary = colors.primary,
            secondary = colors.secondary,
            tertiary = colors.tertiary,
            background = colors.background,
            surface = colors.surface,
            surfaceVariant = colors.surfaceVariant,
            onPrimary = colors.onPrimary,
            onSecondary = colors.onSecondary,
            onTertiary = colors.onTertiary,
            onBackground = colors.onBackground,
            onSurface = colors.onSurface,
            onSurfaceVariant = colors.onSurfaceVariant
        )
    } else {
        lightColorScheme(
            primary = colors.primary,
            secondary = colors.secondary,
            tertiary = colors.tertiary,
            background = colors.background,
            surface = colors.surface,
            surfaceVariant = colors.surfaceVariant,
            onPrimary = colors.onPrimary,
            onSecondary = colors.onSecondary,
            onTertiary = colors.onTertiary,
            onBackground = colors.onBackground,
            onSurface = colors.onSurface,
            onSurfaceVariant = colors.onSurfaceVariant
        )
    }

    CompositionLocalProvider(LocalSolariColors provides colors) {
        MaterialTheme(colorScheme = materialColors) {
            content()
        }
    }
}
