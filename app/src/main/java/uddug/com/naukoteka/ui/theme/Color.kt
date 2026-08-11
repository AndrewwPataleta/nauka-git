package uddug.com.naukoteka.ui.theme

import androidx.compose.ui.graphics.Color

val Primary = Color(0xFF2E83D9)
val PrimaryVariant = Color(0xFF0C1F5D)
val BackgroundLight = Color(0xFFFFFFFF)
val BackgroundDark = Color(0xFF242535)
// Фон экранов в светлой теме должен быть белым (как раньше). Серый #EAEAF2
// давал «грязный» фон на экранах, берущих цвет из MaterialTheme.colors.surface
// (напр. «Добавить опрос»). Поля ввода/карточки используют отдельные константы
// (InputBackgroundLight/BackgroundMoreInfoLight), их серость сохраняется.
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceDark = Color(0xFF1A1B2B)
val TextPrimaryLight = Color(0xFF10101C)
val TextPrimaryDark = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFF8083A0)
val ErrorColor = Color(0xFFD92E4D)
val BackgroundMoreInfoLight = Color(0xFFEAEAF2)
val BackgroundMoreInfoDark = Color(0xFF363850)
val InputStrokeLight = Color(0xFFDBDEE9)
val InputStrokeDark = Color(0xFF4D4D67)
val InputBackgroundLight = Color(0xFFEAEAF2)
val InputBackgroundDark = Color(0xFF242535)
val IconAccent = Color(0xFF2E83D9)
val Inactive = Color(0xFF8083A0)

val ChatBubbleOtherLight = Color(0xFFF5F5F9)
val ChatBubbleOtherDark = Color(0xFF363850)
val ChatTextOtherLight = Color(0xFF111827)
val ChatTextOtherDark = Color(0xFFFFFFFF)
val ChatTextSecondaryLight = Color(0xFF6F7A90)
val ChatTextSecondaryDark = Color(0xFF9EA1B5)
val ChatFileIconBgLight = Color(0xFFE4E8F1)
val ChatFileIconBgDark = Color(0xFF363850)
