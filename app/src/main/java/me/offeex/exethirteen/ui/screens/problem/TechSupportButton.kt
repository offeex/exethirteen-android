import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import me.offeex.exethirteen.R
import me.offeex.exethirteen.ui.theme.PrimaryColor
import me.offeex.exethirteen.ui.theme.SecondaryColor
import me.offeex.exethirteen.ui.theme.TextColor

@Composable
internal fun TechSupportButton(onClick: () -> Unit = {}) {
    val corner = RoundedCornerShape(12.dp)
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(top = 20.dp)
            .height(42.dp)
            .background(PrimaryColor, corner)
            .border(2.dp, SecondaryColor, corner)
            .width(180.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.phone),
            contentDescription = "Tech Support",
            tint = TextColor,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = "Tech Support",
            color = TextColor,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}