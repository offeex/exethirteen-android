package me.offeex.exethirteen.ui.navigation

import androidx.annotation.DrawableRes
import com.ramcosta.composedestinations.spec.DirectionDestinationSpec
import me.offeex.exethirteen.R
import me.offeex.exethirteen.ui.screens.destinations.HomeScreenDestination
import me.offeex.exethirteen.ui.screens.destinations.SettingsScreenDestination

enum class BottomBarDestination(
    val direction: DirectionDestinationSpec,
    @DrawableRes val icon: Int,
) {
    Home(HomeScreenDestination, R.drawable.home),
    Settings(SettingsScreenDestination, R.drawable.setting)
}