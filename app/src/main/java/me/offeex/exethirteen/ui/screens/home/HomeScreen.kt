package me.offeex.exethirteen.ui.screens.home

import BandwidthComposite
import BandwidthDirection
import CascadeButton
import ConnectButton
import ConnectedStatus
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import me.offeex.exethirteen.R
import me.offeex.exethirteen.entity.ServerChoice
import me.offeex.exethirteen.ui.screens.ScreenUtils.screen
import me.offeex.exethirteen.ui.theme.PrimaryColor
import me.offeex.exethirteen.ui.theme.SecondaryColor
import me.offeex.exethirteen.ui.theme.TetriaryColor

@Composable
@Destination
@RootNavGraph(start = true)
fun HomeScreen() {
    var isConnected by remember { mutableStateOf(false) }

    Crossfade(targetState = isConnected, label = "", animationSpec = tween(700)) {
        Box(Modifier.screen(if (it) R.drawable.bgactive else R.drawable.bg)) {
            ConnectButton(isConnected) { isConnected = !isConnected }
            ConnectedStatus(isConnected)
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier
                    .padding(bottom = 190.dp)
                    .align(Alignment.BottomCenter)
            ) {
                BandwidthComposite(direction = BandwidthDirection.DOWNLOAD, 64)
                Spacer(modifier = Modifier.width(96.dp))
                BandwidthComposite(direction = BandwidthDirection.UPLOAD, 57)
            }
            Column(Modifier.align(Alignment.BottomCenter)) {
                var isCascaded by remember { mutableStateOf(false) }
                val fillerAlpha by animateFloatAsState(
                    targetValue = if (isCascaded) 0.7f else 0.0f,
                    label = ""
                )
                if (isCascaded) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1.1f)
                            .offset(y = 10.dp)
                            .background(SecondaryColor.copy(alpha = fillerAlpha))
                            .clickable(MutableInteractionSource(), null) {
                                isCascaded = !isCascaded
                            }
                    )
                }
                CascadeButton { isCascaded = !isCascaded }
                var currentChoice by remember { mutableStateOf(ServerChoice.DE) }
                LazyColumn(
                    modifier = Modifier
                        .background(PrimaryColor)
                        .animateContentSize()
                ) {
                    if (isCascaded) {
                        items(ServerChoice.values().toList()) {
                            val color =
                                if (currentChoice == it) TetriaryColor
                                else SecondaryColor
                            ServerChoiceComposite(it, color) { currentChoice = it }
                            Spacer(modifier = Modifier.padding(top = 1.dp, bottom = 1.dp))
                        }
                    }
                }
                ServerChoiceComposite(currentChoice, current = true)
            }
        }
    }
}