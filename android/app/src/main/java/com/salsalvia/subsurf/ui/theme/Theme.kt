package com.salsalvia.subsurf.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --------------------------------------------------------------------------- //
// Brand identity: teal + moss + earth. Tuned for outdoor readability (high
// contrast surfaces, no pure-black dark mode) while keeping the scientific,
// calm character of the tool.
// --------------------------------------------------------------------------- //
private val Teal = Color(0xFF0F6155)
private val TealBright = Color(0xFF83CDBE)
private val Moss = Color(0xFF546E3C)
private val Earth = Color(0xFF8A5A2B)

private val LightColors = lightColorScheme(
    primary = Teal,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFA7D3C9),
    onPrimaryContainer = Color(0xFF00201B),
    secondary = Moss,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDDE7C9),
    onSecondaryContainer = Color(0xFF1B2410),
    tertiary = Earth,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF3DFC9),
    onTertiaryContainer = Color(0xFF2E1A05),
    background = Color(0xFFF6F8F6),
    onBackground = Color(0xFF171C1A),
    surface = Color.White,
    onSurface = Color(0xFF171C1A),
    surfaceVariant = Color(0xFFE1E7E3),
    onSurfaceVariant = Color(0xFF3F4945),
    outline = Color(0xFF6F7975),
    outlineVariant = Color(0xFFC3CDC8),
    surfaceTint = Teal,
    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
)

private val DarkColors = darkColorScheme(
    primary = TealBright,
    onPrimary = Color(0xFF00382F),
    primaryContainer = Teal,
    onPrimaryContainer = Color(0xFFCDEDE4),
    secondary = Color(0xFFB5CBA0),
    onSecondary = Color(0xFF22301A),
    secondaryContainer = Color(0xFF38472B),
    onSecondaryContainer = Color(0xFFDDE7C9),
    tertiary = Color(0xFFE0BE99),
    onTertiary = Color(0xFF2E1A05),
    tertiaryContainer = Color(0xFF5A3A17),
    onTertiaryContainer = Color(0xFFF3DFC9),
    background = Color(0xFF0F1211),
    onBackground = Color(0xFFE1E3E1),
    surface = Color(0xFF141817),
    onSurface = Color(0xFFE1E3E1),
    surfaceVariant = Color(0xFF3F4945),
    onSurfaceVariant = Color(0xFFBFC9C4),
    outline = Color(0xFF89938E),
    outlineVariant = Color(0xFF3F4945),
    surfaceTint = TealBright,
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC),
)

private val SubsurfTypography = Typography(
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.1.sp,
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 19.sp,
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp,
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 15.sp,
    ),
)

/** Numeric style with tabular figures so aligned metric values never jitter. */
val MetricTextStyle = TextStyle(
    fontWeight = FontWeight.SemiBold,
    fontFeatureSettings = "tnum",
)

private val SubsurfShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

@Composable
fun SubsurfTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = SubsurfTypography,
        shapes = SubsurfShapes,
        content = content,
    )
}
