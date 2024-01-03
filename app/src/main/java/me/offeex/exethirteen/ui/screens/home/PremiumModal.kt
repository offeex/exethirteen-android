import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.offeex.exethirteen.R

@Composable
fun PremiumModal(
    isOpen: Boolean,
    gradient: Brush,
    modifier: Modifier,
    onClose: () -> Unit
) {
    AnimatedVisibility(
        visible = isOpen,
        enter = slideInVertically { it * 2 },
        exit = slideOutVertically { it * 2 },
        modifier = Modifier
            .then(modifier)
            .wrapContentSize(Alignment.Center)
    ) {
        Box(
            modifier = Modifier
                .background(gradient, RoundedCornerShape(36.dp))
                .fillMaxWidth(0.84f)
        ) {
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 40.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        stringResource(R.string.pro_version),
                        color = Color.Black,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        painter = painterResource(R.drawable.close02),
                        contentDescription = "",
                        tint = Color.Black,
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .clickable(onClick = onClose)
                            .size(16.dp)
                    )
                }

                Box(
                    Modifier
                        .padding(vertical = 20.dp)
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.Black)
                )

                Feature(stringResource(R.string.more_speed))
                Feature(stringResource(R.string.premium_regions))
                Feature(stringResource(R.string.no_ads))

                Row(
                    modifier = Modifier.padding(vertical = 28.dp)
                ) {
                    Text(
                        "from $5",
                        color = Color.Black,
                        style = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold),
                        modifier = Modifier.alignByBaseline()
                    )
                    Text(
                        "/ month",
                        color = Color.Black,
                        style = TextStyle(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Light
                        ),
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .alignByBaseline()
                    )
                }

                BuyNow()
            }
        }
    }
}

@Composable
private fun Feature(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 3.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.checkmark),
            contentDescription = "",
            tint = Color.Black,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text,
            color = Color.Black,
            style = TextStyle(
                fontWeight = FontWeight.SemiBold,
                fontSize = 22.sp,
                letterSpacing = 0.2.sp
            ),
            modifier = Modifier.padding(start = 6.dp)
        )
    }
}

@Composable
private fun BuyNow() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .background(Color.Black, RoundedCornerShape(16.dp))
            .clickable(onClick = { })
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Text(
            "Buy now",
            style = TextStyle(
                fontSize = 26.sp,
                fontWeight = FontWeight.Light,
            ),
            color = Color.White
        )
    }
}