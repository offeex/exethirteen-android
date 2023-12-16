package me.offeex.exethirteen.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import me.offeex.exethirteen.ui.theme.InactiveColor
import me.offeex.exethirteen.ui.theme.PrimaryColor
import me.offeex.exethirteen.ui.theme.TetriaryColor
import me.offeex.exethirteen.ui.theme.TextColor

@Composable
fun BottomBar(
    currentDest: BottomBarDestination,
    onSelect: (BottomBarDestination) -> Unit
) {
    NavigationBar(
        containerColor = PrimaryColor,
        contentColor = TextColor,
        modifier = Modifier.height(60.dp)
    ) {
        BottomBarDestination.values().forEach {
            val isSelected = currentDest == it
            NavigationBarItem(
                selected = isSelected,
                onClick = { onSelect(it) },
                icon = {
                    Column {
                        Icon(it)
                        if (isSelected) Box(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .width(24.dp)
                                .height(2.dp)
                                .background(Color.White) // Change the color to your preference
                        )
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = TextColor,
                    unselectedIconColor = InactiveColor,
                    indicatorColor = PrimaryColor
                ),
                interactionSource = object : MutableInteractionSource {
                    override val interactions: Flow<Interaction> = emptyFlow()
                    override suspend fun emit(interaction: Interaction) {}
                    override fun tryEmit(interaction: Interaction) = true
                },
            )
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(2.dp)
            .background(TetriaryColor),
    )
}

@Composable
private fun Icon(dest: BottomBarDestination) = Icon(
    painter = painterResource(dest.icon),
    contentDescription = "Navigation icon",
    modifier = Modifier.size(24.dp)
)