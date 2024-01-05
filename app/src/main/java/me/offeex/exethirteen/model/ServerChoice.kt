package me.offeex.exethirteen.model

import android.content.Context
import androidx.annotation.DrawableRes
import me.offeex.exethirteen.App
import me.offeex.exethirteen.Core
import me.offeex.exethirteen.database.Profile
import me.offeex.exethirteen.R

private val zatichka = Profile(
    host = "95.164.69.220",
    remotePort = 8388,
    password = "mypassword",
    method = "aes-256-gcm"
)

private val zatichka2 = Profile(
    host = "fr-ss.ipracevpn.com",
    remotePort = 2443,
    password = "vV}17305$&",
    method = "aes-256-gcm"
)

private fun res(id: Int): Context.() -> String = { getString(id) }

enum class ServerChoice(
    @DrawableRes val icon: Int,
    val country: Context.() -> String,
    val city: Context.() -> String,
    val profile: Profile
) {
    DE(R.drawable.de, res(R.string.germany), res(R.string.frankfurt), zatichka),
    IE(R.drawable.ie, res(R.string.ireland), res(R.string.dublin), zatichka2),
    UK(R.drawable.uk, res(R.string.uk), res(R.string.london), zatichka),
//    AT(R.drawable.at, res(R.string.austria), res(R.string.vienna), zatichka),
    US(R.drawable.us, res(R.string.usa), res(R.string.newyork), zatichka),
    ;

    init {
        profile.name = toString()
    }
}