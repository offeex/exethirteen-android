package me.offeex.exethirteen.entity

import androidx.annotation.DrawableRes
import me.offeex.exethirteen.R

enum class ServerChoice(@DrawableRes val icon: Int, val country: String, val city: String) {
    DE(R.drawable.de, "Germany", "Frankfurt"),
    IE(R.drawable.ie, "Ireland", "Dublin"),
    UK(R.drawable.uk, "UK", "London"),
    AT(R.drawable.at, "Austria", "Vienna"),
}