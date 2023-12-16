package me.offeex.exethirteen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.navigation.compose.rememberNavController
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.utils.currentDestinationAsState
import me.offeex.exethirteen.ui.navigation.BottomBar
import me.offeex.exethirteen.ui.navigation.BottomBarDestination
import me.offeex.exethirteen.ui.screens.NavGraphs
import me.offeex.exethirteen.ui.theme.BackgroundColor
import me.offeex.exethirteen.ui.theme.ExethirteenTheme
import me.offeex.exethirteen.ui.theme.PrimaryColor


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ExethirteenTheme {
                val navController = rememberNavController()
                val currentDest = BottomBarDestination.values().find {
                    it.direction == navController.currentDestinationAsState().value
                } ?: BottomBarDestination.Home

                Scaffold(bottomBar = {
                    BottomBar(currentDest) { navController.navigate(it.direction) }
                }) {
                    DestinationsNavHost(
                        navGraph = NavGraphs.root,
                        navController = navController,
                        modifier = Modifier.padding(it)
                    )
                }

            }
        }
    }
}