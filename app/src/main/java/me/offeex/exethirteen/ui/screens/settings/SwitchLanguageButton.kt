import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import me.offeex.exethirteen.R
import me.offeex.exethirteen.ui.theme.SecondaryColor
import me.offeex.exethirteen.ui.theme.TetriaryColor
import me.offeex.exethirteen.ui.theme.TextColor
import java.util.*

@Composable
internal fun SwitchLanguageButton(modifier: Modifier) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    val map = mapOf(
        "en" to "English",
        "ru" to "Русский"
    )

    Column(
        modifier
            .padding(top = 20.dp)
            .width(180.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable { expanded = true }
                .background(SecondaryColor, RoundedCornerShape(12.dp))
                .border(2.dp, TetriaryColor, RoundedCornerShape(12.dp))
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.language),
                    contentDescription = "Switch language",
                    modifier = Modifier
                        .padding(end = 6.dp)
                        .size(20.dp)
                )
                Text(
                    text = map[AppCompatDelegate.getApplicationLocales()
                        .get(0)!!.language]!!,
                    fontSize = 18.sp
                )
            }
            Icon(
                painter = painterResource(R.drawable.downarrow),
                contentDescription = "Switch language",
                tint = TextColor,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        MaterialTheme(
            colorScheme = MaterialTheme.colorScheme.copy(
                surface = Color.Transparent
            ),
        ) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .background(SecondaryColor, RoundedCornerShape(18.dp))
                    .border(2.dp, TetriaryColor, RoundedCornerShape(18.dp))
                    .width(180.dp)
                    .padding(horizontal = 2.dp)
            ) {
                val modifierItem = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SecondaryColor)
                val textStyle = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal
                )
                DropdownMenuItem(
                    text = { Text(text = "English", style = textStyle) },
                    onClick = {
                        localeSelection(context, "en")
                        expanded = false
                    },
                    modifier = modifierItem
                )
                DropdownMenuItem(
                    text = { Text(text = "Русский", style = textStyle) },
                    onClick = {
                        localeSelection(context, Locale("ru").toLanguageTag())
                        expanded = false
                    },
                    modifier = modifierItem
                )
            }
        }
    }
}

private fun localeSelection(context: Context, localeTag: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        context.getSystemService(LocaleManager::class.java).applicationLocales =
            LocaleList.forLanguageTags(localeTag)
    } else {
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(localeTag)
        )
    }
}