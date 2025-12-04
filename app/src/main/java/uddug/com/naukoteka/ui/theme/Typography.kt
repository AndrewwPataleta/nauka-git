package uddug.com.naukoteka.ui.theme

import androidx.compose.material.Typography
import androidx.compose.material3.Typography as Typography3
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import uddug.com.naukoteka.R

private val GolosFontFamily = FontFamily(
    Font(resId = R.font.golos_text_regular, weight = FontWeight.Normal),
    Font(resId = R.font.golos_text_medium, weight = FontWeight.Medium),
    Font(resId = R.font.golos_text_demi_bold, weight = FontWeight.SemiBold),
    Font(resId = R.font.golos_text_bold, weight = FontWeight.Bold),
    Font(resId = R.font.golos_black, weight = FontWeight.Black),
)

val NaukotekaMaterialTypography = Typography(defaultFontFamily = GolosFontFamily)

private val defaultM3Typography = Typography3()

private fun TextStyle.withFontFamily(): TextStyle = copy(fontFamily = GolosFontFamily)

val NaukotekaTypography = Typography3(
    displayLarge = defaultM3Typography.displayLarge.withFontFamily(),
    displayMedium = defaultM3Typography.displayMedium.withFontFamily(),
    displaySmall = defaultM3Typography.displaySmall.withFontFamily(),
    headlineLarge = defaultM3Typography.headlineLarge.withFontFamily(),
    headlineMedium = defaultM3Typography.headlineMedium.withFontFamily(),
    headlineSmall = defaultM3Typography.headlineSmall.withFontFamily(),
    titleLarge = defaultM3Typography.titleLarge.withFontFamily(),
    titleMedium = defaultM3Typography.titleMedium.withFontFamily(),
    titleSmall = defaultM3Typography.titleSmall.withFontFamily(),
    bodyLarge = defaultM3Typography.bodyLarge.withFontFamily(),
    bodyMedium = defaultM3Typography.bodyMedium.withFontFamily(),
    bodySmall = defaultM3Typography.bodySmall.withFontFamily(),
    labelLarge = defaultM3Typography.labelLarge.withFontFamily(),
    labelMedium = defaultM3Typography.labelMedium.withFontFamily(),
    labelSmall = defaultM3Typography.labelSmall.withFontFamily(),
)
