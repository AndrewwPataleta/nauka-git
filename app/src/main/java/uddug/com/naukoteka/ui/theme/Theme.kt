package uddug.com.naukoteka.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme as Material2Theme
import androidx.compose.material.darkColors as materialDarkColors
import androidx.compose.material.lightColors as materialLightColors
import androidx.compose.material3.MaterialTheme as Material3Theme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    secondary = PrimaryVariant,
    onSecondary = Color.White,
    background = BackgroundLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    error = ErrorColor,
    onError = Color.White,
    surfaceVariant = BackgroundMoreInfoLight,
    onSurfaceVariant = TextSecondary,
    outline = InputStrokeLight,
)

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    secondary = PrimaryVariant,
    onSecondary = Color.White,
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    error = ErrorColor,
    onError = Color.White,
    surfaceVariant = BackgroundMoreInfoDark,
    onSurfaceVariant = TextSecondary,
    outline = InputStrokeDark,
)

private val LightMaterialColors = materialLightColors(
    primary = Primary,
    primaryVariant = PrimaryVariant,
    secondary = PrimaryVariant,
    background = BackgroundLight,
    surface = SurfaceLight,
    error = ErrorColor,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
)

private val DarkMaterialColors = materialDarkColors(
    primary = Primary,
    primaryVariant = PrimaryVariant,
    secondary = PrimaryVariant,
    background = BackgroundDark,
    surface = SurfaceDark,
    error = ErrorColor,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
)

data class NaukotekaExtendedColors(
    val backgroundMoreInfo: Color,
    val inputBackground: Color,
    val inputStroke: Color,
    val accent: Color,
    val inactive: Color,
    val iconAccent: Color,
)

private val LocalNaukotekaExtendedColors = staticCompositionLocalOf {
    NaukotekaExtendedColors(
        backgroundMoreInfo = BackgroundMoreInfoLight,
        inputBackground = InputBackgroundLight,
        inputStroke = InputStrokeLight,
        accent = Primary,
        inactive = Inactive,
        iconAccent = IconAccent,
    )
}

private val LightExtendedColors = NaukotekaExtendedColors(
    backgroundMoreInfo = BackgroundMoreInfoLight,
    inputBackground = InputBackgroundLight,
    inputStroke = InputStrokeLight,
    accent = Primary,
    inactive = Inactive,
    iconAccent = IconAccent,
)

private val DarkExtendedColors = NaukotekaExtendedColors(
    backgroundMoreInfo = BackgroundMoreInfoDark,
    inputBackground = InputBackgroundDark,
    inputStroke = InputStrokeDark,
    accent = Primary,
    inactive = Inactive,
    iconAccent = IconAccent,
)

@Composable
fun NaukotekaTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (useDarkTheme) DarkColorScheme else LightColorScheme
    val materialColors = if (useDarkTheme) DarkMaterialColors else LightMaterialColors
    val extendedColors = if (useDarkTheme) DarkExtendedColors else LightExtendedColors

    CompositionLocalProvider(LocalNaukotekaExtendedColors provides extendedColors) {
        Material3Theme(
            colorScheme = colorScheme,
            typography = NaukotekaTypography,
        ) {
            Material2Theme(
                colors = materialColors,
                typography = NaukotekaMaterialTypography,
                content = content,
            )
        }
    }
}

object NauTheme {
    val extendedColors: NaukotekaExtendedColors
        @Composable
        @ReadOnlyComposable
        get() = LocalNaukotekaExtendedColors.current
}
