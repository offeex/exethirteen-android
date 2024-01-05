import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.offeex.exethirteen.R
import me.offeex.exethirteen.ui.theme.TextColor

@Composable
internal fun ConnectedStatus(isConnected: Boolean, modifier: Modifier) {
    Row(
        modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val connectedColor = if (isConnected) Color.Green else Color.Gray
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color = connectedColor, shape = CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = stringResource(R.string.connected), color = TextColor)
    }
}