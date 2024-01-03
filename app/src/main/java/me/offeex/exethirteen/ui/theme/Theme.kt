package me.offeex.exethirteen.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val PrimaryColor = Color(0xFF191919)
val SecondaryColor = Color(0xFF222222)
val TetriaryColor = Color(0xFF2F2F2F)
val BackgroundColor = Color(0xFF111111)
val TextColor = Color.White
val InactiveColor = Color(0x55FFFFFF)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryColor,
    secondary = SecondaryColor,
    tertiary = TetriaryColor,
    background = BackgroundColor,
    surface = TetriaryColor,
    onPrimary = TextColor,
    onSecondary = TextColor,
    onTertiary = TextColor,
    onBackground = TextColor,
    onSurface = TextColor,
)

private val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)

@Composable
fun ExethirteenTheme(content: @Composable () -> Unit) {
    val scheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) SideEffect {
        val window = (view.context as Activity).window
        window.statusBarColor = scheme.background.toArgb()
    }

    MaterialTheme(colorScheme = scheme, typography = Typography, content = content)
}