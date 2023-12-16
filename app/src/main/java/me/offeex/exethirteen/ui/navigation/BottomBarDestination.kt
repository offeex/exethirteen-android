package me.offeex.exethirteen.ui.navigation

import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector
import com.ramcosta.composedestinations.spec.DirectionDestinationSpec
import me.offeex.exethirteen.R
import me.offeex.exethirteen.ui.screens.destinations.HomeScreenDestination
import me.offeex.exethirteen.ui.screens.destinations.PremiumScreenDestination
import me.offeex.exethirteen.ui.screens.destinations.SettingsScreenDestination

enum class BottomBarDestination(
    val direction: DirectionDestinationSpec,
    @DrawableRes val icon: Int,
) {
    Home(HomeScreenDestination, R.drawable.home),
    Premium(PremiumScreenDestination, R.drawable.crown),
    Settings(SettingsScreenDestination, R.drawable.setting)
}