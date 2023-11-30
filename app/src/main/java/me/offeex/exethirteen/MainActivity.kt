package me.offeex.exethirteen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import me.offeex.exethirteen.ui.theme.ExethirteenTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ExethirteenTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Conversation(messages = SampleData.conversationSample)
                }
            }
        }
    }
}

@Composable
fun Conversation(messages: List<Message>) {
    LazyColumn {
        items(messages) { message ->
            MessageCard(message)
        }
    }
}

@Composable
fun MessageCard(msg: Message) {
    Row {
        Image(
            painter = painterResource(id = R.drawable.prikol),
            contentDescription = "nigga",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .border(1.dp, colors().primary, CircleShape)
                .padding(4.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))

        var isExpanded by remember { mutableStateOf(false) }
        val surfaceColor by animateColorAsState(
            if (isExpanded) colors().primary else colors().surface, label = ""
        )

        Column {
            Text(msg.author, color = colors().secondary, style = typography().titleMedium)
            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                shape = shapes().medium,
                border = BorderStroke(.5.dp, colors().onSecondary),
                color = surfaceColor,
                modifier = Modifier
                    .clickable { isExpanded = !isExpanded }
                    .animateContentSize()
                    .padding(1.dp)
            ) {
                Text(
                    msg.body,
                    maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                    modifier = Modifier.padding(8.dp),
                    style = typography().bodyMedium
                )
            }
        }
    }
}

data class Message(val author: String, val body: String)

@Composable
private fun colors() = MaterialTheme.colorScheme

@Composable
private fun typography() = MaterialTheme.typography

@Composable
private fun shapes() = MaterialTheme.shapes