import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.offeex.exethirteen.R
import me.offeex.exethirteen.ui.theme.SecondaryColor
import me.offeex.exethirteen.ui.theme.TextColor

@Composable
internal fun CascadeButton(onCascade: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(SecondaryColor)
            .clickable(onClick = onCascade),
    ) {
        Text(
            text = stringResource(R.string.more_locations),
            style = MaterialTheme.typography.bodyLarge,
        )
        Box(
            modifier = Modifier
                .width(24.dp)
                .height(4.dp)
                .clip(CircleShape)
                .background(TextColor)
        )
    }
}