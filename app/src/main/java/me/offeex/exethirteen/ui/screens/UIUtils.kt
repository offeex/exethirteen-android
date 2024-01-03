package me.offeex.exethirteen.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import me.offeex.exethirteen.ui.theme.BackgroundColor

object UIUtils {
    val goldenGradient =
        Brush.linearGradient(listOf(Color(0xFFE17E4B), Color(0xFFFFF873)))

    fun Modifier.screen(@DrawableRes backgroundImage: Int? = null): Modifier =
        composed {
            fillMaxSize().background(color = BackgroundColor).run {
                paint(
                    painter = painterResource(backgroundImage ?: return@run this),
                    contentScale = ContentScale.FillHeight,
                )
            }
        }
}