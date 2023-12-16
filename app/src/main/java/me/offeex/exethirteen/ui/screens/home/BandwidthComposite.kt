import androidx.annotation.DrawableRes
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import me.offeex.exethirteen.R

@Composable
internal fun BandwidthComposite(direction: BandwidthDirection, value: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(id = direction.icon),
            contentDescription = null,
            modifier = Modifier
                .size(24.dp, 24.dp)
                .border(2.dp, Color.White, CircleShape)
                .padding(6.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = direction.name.lowercase().replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.bodyMedium
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.alignByBaseline()
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "MB/s",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.alignByBaseline()
                )
            }
        }
    }
}

enum class BandwidthDirection(@DrawableRes val icon: Int) {
    UPLOAD(R.drawable.arrowup),
    DOWNLOAD(R.drawable.arrowdown)
}