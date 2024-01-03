import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.offeex.exethirteen.R
import me.offeex.exethirteen.ui.screens.UIUtils.screen
import me.offeex.exethirteen.ui.theme.TextColor

@Composable
fun NoConnectionScreen() {
    Column(
        Modifier.screen(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        Image(
            painter = painterResource(R.drawable.inet),
            contentDescription = "No connection",
            modifier = Modifier.size(150.dp)
        )
        Text(
            text = "Connection problems",
            color = TextColor,
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = "Check your internet connection or message Tech Support",
            color = TextColor.copy(alpha = 0.75f),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Light,
                fontSize = 18.sp,
                letterSpacing = 0.7.sp,
                lineHeight = 24.sp,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.width(240.dp)
        )
        TechSupportButton()
    }
}