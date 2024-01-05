package me.offeex.exethirteen.ui.screens.home

import BandwidthComposite
import BandwidthDirection
import CascadeButton
import ConnectButton
import ConnectedStatus
import PremiumButton
import ServerChoiceComposite
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.offeex.exethirteen.bg.BaseService
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import me.offeex.exethirteen.MainActivity
import me.offeex.exethirteen.R
import me.offeex.exethirteen.model.ServerChoice
import me.offeex.exethirteen.ui.screens.UIUtils.screen
import me.offeex.exethirteen.ui.theme.PrimaryColor
import me.offeex.exethirteen.ui.theme.SecondaryColor
import me.offeex.exethirteen.ui.theme.TetriaryColor
import me.offeex.exethirteen.manager.ConnectionManager
import me.offeex.exethirteen.ui.screens.UIUtils

@Composable
@Destination
@RootNavGraph(start = true)
fun HomeScreen() {
    val isConnected = ConnectionManager.connected == BaseService.State.Connected

    Crossfade(targetState = isConnected, animationSpec = tween(700)) {
        Box(Modifier.screen(if (it) R.drawable.bgactive else R.drawable.bg)) {
            PremiumButton(UIUtils.goldenGradient) {
                MainActivity.isPremiumOpen = true
                MainActivity.isModalOpen = true
            }

            Box(
                modifier = Modifier.align(Alignment.Center).offset(y = (-40).dp)
            ) {
                ConnectButton(isConnected, Modifier.align(Alignment.Center)) {
                    ConnectionManager.toggle()
                }
                ConnectedStatus(
                    isConnected, Modifier
                        .align(Alignment.Center)
                        .padding(top = 250.dp)
                )
            }

            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier
                    .padding(bottom = 190.dp)
                    .align(Alignment.BottomCenter)
            ) {
                BandwidthComposite(
                    direction = BandwidthDirection.DOWNLOAD,
                    ConnectionManager.downRate
                )
                Spacer(modifier = Modifier.width(96.dp))
                BandwidthComposite(
                    direction = BandwidthDirection.UPLOAD,
                    ConnectionManager.upRate
                )
            }

            Column(Modifier.align(Alignment.BottomCenter)) {
                var isCascaded by remember { mutableStateOf(false) }
                val fillerAlpha by animateFloatAsState(
                    targetValue = if (isCascaded) 0.7f else 0.0f,
                    label = ""
                )
                if (isCascaded) {
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.1f)
                        .offset(y = 10.dp)
                        .background(SecondaryColor.copy(alpha = fillerAlpha))
                        .clickable(MutableInteractionSource(), null) {
                            isCascaded = !isCascaded
                        })
                }
                CascadeButton { isCascaded = !isCascaded }
                val currentChoice = ConnectionManager.choice
                LazyColumn(
                    modifier = Modifier
                        .background(PrimaryColor)
                        .animateContentSize()
                ) {
                    if (isCascaded) {
                        item { Divider(color = PrimaryColor, thickness = 2.dp) }
                        items(ServerChoice.values().toList()) { choice ->
                            val color =
                                if (currentChoice == choice) TetriaryColor
                                else SecondaryColor
                            ServerChoiceComposite(choice, color) {
                                ConnectionManager.disconnect()
                                ConnectionManager.switchProfile(choice)
                            }
                            Spacer(modifier = Modifier.padding(top = 1.dp, bottom = 1.dp))
                        }
                    }
                }
                ServerChoiceComposite(currentChoice, current = true)
            }
        }
    }
}