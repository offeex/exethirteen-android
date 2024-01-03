/*******************************************************************************
 *                                                                             *
 *  Copyright (C) 2017 by Max Lv <max.c.lv@gmail.com>                          *
 *  Copyright (C) 2017 by Mygod Studio <contact-shadowsocks-android@mygod.be>  *
 *                                                                             *
 *  This program is free software: you can redistribute it and/or modify       *
 *  it under the terms of the GNU General Public License as published by       *
 *  the Free Software Foundation, either version 3 of the License, or          *
 *  (at your option) any later version.                                        *
 *                                                                             *
 *  This program is distributed in the hope that it will be useful,            *
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the              *
 *  GNU General Public License for more details.                               *
 *                                                                             *
 *  You should have received a copy of the GNU General Public License          *
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.       *
 *                                                                             *
 *******************************************************************************/

package me.offeex.exethirteen.database

import android.annotation.TargetApi
import android.os.Parcelable
import android.util.LongSparseArray
import androidx.room.*
import me.offeex.exethirteen.plugin.PluginConfiguration
import kotlinx.parcelize.Parcelize
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

@Entity
@Parcelize
data class Profile(
        // user configurable fields
        var name: String = "",

        var host: String = "example.shadowsocks.org",
        var remotePort: Int = 8388,
        var password: String = "u1rRWTssNv0p",
        var method: String = "aes-256-cfb",

        var route: String = "all",
        var remoteDns: String = "dns.google",
        var proxyApps: Boolean = false,
        var bypass: Boolean = false,
        var udpdns: Boolean = false,
        var ipv6: Boolean = false,
        @TargetApi(28)
        var metered: Boolean = false,
        var individual: String = "",
        var plugin: String? = null,
        var udpFallback: Long? = null,

        var tx: Long = 0,
        var rx: Long = 0,
        var userOrder: Long = 0,

        @Ignore // not persisted in db, only used by direct boot
        var dirty: Boolean = false
) : Parcelable {
    
    fun toJson(profiles: LongSparseArray<Profile>? = null): JSONObject = JSONObject().apply {
        put("server", host)
        put("server_port", remotePort)
        put("password", password)
        put("method", method)
        if (profiles == null) return@apply
        PluginConfiguration(plugin ?: "").getOptions().also {
            if (it.id.isNotEmpty()) {
                put("plugin", it.id)
                put("plugin_opts", it.toString())
            }
        }
        put("remarks", this@Profile.name)
        put("route", route)
        put("remote_dns", remoteDns)
        put("ipv6", ipv6)
        put("metered", metered)
        put("proxy_apps", JSONObject().apply {
            put("enabled", proxyApps)
            if (proxyApps) {
                put("bypass", bypass)
                // android_ prefix is used because package names are Android specific
                put("android_list", JSONArray(individual.split("\n")))
            }
        })
        put("udpdns", udpdns)
        val fallback = profiles.get(udpFallback ?: return@apply)
        if (fallback != null && fallback.plugin.isNullOrEmpty()) fallback.toJson().also { put("udp_fallback", it) }
    }
}
