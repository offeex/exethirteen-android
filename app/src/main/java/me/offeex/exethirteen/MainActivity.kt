package me.offeex.exethirteen

import NoConnectionScreen
import PremiumModal
import android.content.pm.ActivityInfo
import android.net.NetworkCapabilities
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.navigation.compose.rememberNavController
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.utils.currentDestinationAsState
import me.offeex.exethirteen.manager.ConnectionManager
import me.offeex.exethirteen.manager.LatencyManager
import me.offeex.exethirteen.ui.navigation.BottomBar
import me.offeex.exethirteen.ui.navigation.BottomBarDestination
import me.offeex.exethirteen.ui.screens.NavGraphs
import me.offeex.exethirteen.ui.screens.UIUtils
import me.offeex.exethirteen.ui.theme.ExethirteenTheme


class MainActivity : AppCompatActivity() {
    companion object {
        var isModalOpen by mutableStateOf(false)
        var isPremiumOpen by mutableStateOf(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        LatencyManager.init(this)
        ConnectionManager.init(this)

        setContent {
            ExethirteenTheme {
                CompositionLocalProvider(
                    LocalDensity provides Density(2.4750001f, .8f)
                ) {
                    val noInternet = !Core.connectivity.run {
                        getNetworkCapabilities(activeNetwork)
                            ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true
                    }

                    if (noInternet) {
                        NoConnectionScreen()
                        return@CompositionLocalProvider
                    }

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

                    val dim by animateFloatAsState(targetValue = if (isModalOpen) 0.5f else 0.0f)
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = dim))
                    )

                    PremiumModal(
                        isPremiumOpen,
                        UIUtils.goldenGradient,
                        Modifier.fillMaxSize()
                    ) {
                        isPremiumOpen = false
                        isModalOpen = false
                    }
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