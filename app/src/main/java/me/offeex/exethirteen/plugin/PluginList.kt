/*******************************************************************************
 *                                                                             *
 *  Copyright (C) 2020 by Max Lv <max.c.lv@gmail.com>                          *
 *  Copyright (C) 2020 by Mygod Studio <contact-shadowsocks-android@mygod.be>  *
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

package me.offeex.exethirteen.plugin

import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import me.offeex.exethirteen.Core

class PluginList : ArrayList<Plugin>() {
    init {
        add(NoPlugin)
        addAll(Core.app.packageManager.queryIntentContentProviders(
                Intent(PluginContract.ACTION_NATIVE_PLUGIN), PackageManager.GET_META_DATA)
                .filter { it.providerInfo.exported }.map { NativePlugin(it) })
    }

    val lookup = mutableMapOf<String, Plugin>().apply {
        for (plugin in this@PluginList) {
            fun check(old: Plugin?) {
                if (old != null && old !== plugin) {
                    val packages = this@PluginList.filter { it.id == plugin.id }.joinToString { it.packageName }
                    val message = "Conflicting plugins found from: $packages"
                    Toast.makeText(Core.app, message, Toast.LENGTH_LONG).show()
                    throw IllegalStateException(message)
                }
            }
            check(put(plugin.id, plugin))
            for (alias in plugin.idAliases) check(put(alias, plugin))
        }
    }
}
