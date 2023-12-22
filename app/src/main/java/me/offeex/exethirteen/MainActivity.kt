package me.offeex.exethirteen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.utils.currentDestinationAsState
import me.offeex.exethirteen.ui.navigation.BottomBar
import me.offeex.exethirteen.ui.navigation.BottomBarDestination
import me.offeex.exethirteen.ui.screens.NavGraphs
import me.offeex.exethirteen.ui.theme.ExethirteenTheme
import me.offeex.exethirteen.manager.ConnectionManager
import me.offeex.exethirteen.manager.LatencyManager


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        LatencyManager.init(this)
        ConnectionManager.init(this)

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

    override fun onStart() {
        super.onStart()
        ConnectionManager.bandwidthTimeout = 500
    }

    override fun onStop() {
        ConnectionManager.bandwidthTimeout = 0
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        ConnectionManager.unbindService()
    }
}