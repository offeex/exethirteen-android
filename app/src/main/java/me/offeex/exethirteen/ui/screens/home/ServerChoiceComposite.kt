import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import me.offeex.exethirteen.manager.ConnectionManager
import me.offeex.exethirteen.manager.LatencyManager
import me.offeex.exethirteen.model.ServerChoice
import me.offeex.exethirteen.ui.theme.PrimaryColor
import me.offeex.exethirteen.ui.theme.SecondaryColor

@Composable
internal fun ServerChoiceComposite(
    choice: ServerChoice,
    color: Color = PrimaryColor,
    current: Boolean = false,
    onSelect: () -> Unit = {}
) {
    val currentModifier =
        if (current) Modifier.clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
        else Modifier
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(if (current) 78.dp else 64.dp)
            .background(SecondaryColor)
            .then(currentModifier)
            .background(color)
            .clickable(
                indication = null,
                interactionSource = MutableInteractionSource(),
                onClick = if (choice != ConnectionManager.choice) onSelect else {
                    {}
                }
            ),
    ) {
        Row(
            Modifier.padding(start = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = choice.icon),
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
            val context = LocalContext.current
            Text(
                text = "${choice.country(context)} | ${choice.city(context)}",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
        Column(
            Modifier.padding(end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Text(
                text = (LatencyManager.latencies[choice] ?: "NaN").toString(),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.height(20.dp)
            )
            Text(
                text = "ping",
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}