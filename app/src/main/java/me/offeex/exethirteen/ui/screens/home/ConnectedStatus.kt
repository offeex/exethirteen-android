import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import me.offeex.exethirteen.ui.theme.TextColor

@Composable
internal fun ConnectedStatus(isConnected: Boolean) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center)
            .padding(top = 200.dp)
    ) {
        val connectedColor = if (isConnected) Color.Green else Color.Gray
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color = connectedColor, shape = CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = "Connected", color = TextColor)
    }
}