package me.offeex.exethirteen.ui.screens.settings

import SwitchLanguageButton
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ramcosta.composedestinations.annotation.Destination
import me.offeex.exethirteen.R
import me.offeex.exethirteen.ui.screens.UIUtils.screen
import me.offeex.exethirteen.ui.theme.TetriaryColor

@Composable
@Destination
fun SettingsScreen() = Column(Modifier.screen()) {
    Text(
        text = stringResource(R.string.settings),
        style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 32.sp),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
        textAlign = TextAlign.Start
    )
    Spacer(modifier = Modifier
        .background(TetriaryColor)
        .fillMaxWidth()
        .height(2.dp))
    SwitchLanguageButton(Modifier.align(Alignment.CenterHorizontally))
}