package me.offeex.exethirteen.model

import androidx.annotation.DrawableRes
import com.github.shadowsocks.database.Profile
import me.offeex.exethirteen.R

enum class ServerChoice(
    @DrawableRes val icon: Int,
    val country: String,
    val city: String,
    val profile: Profile
) {
    DE(R.drawable.de, "Germany", "Frankfurt", Profile(
        host = "95.164.69.220",
        remotePort = 8388,
        password = "mypassword",
        method = "aes-256-gcm"
    )),
    IE(R.drawable.ie, "Ireland", "Dublin", Profile(
        host = "fr-ss.ipracevpn.com",
        remotePort = 2443,
        password = "vV}17305$&",
        method = "aes-256-gcm"
    ));
//    UK(R.drawable.uk, "UK", "London"),
//    AT(R.drawable.at, "Austria", "Vienna"),
}