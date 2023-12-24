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

package com.github.shadowsocks.preference

import android.os.Binder
import com.github.shadowsocks.database.PublicDatabase
import com.github.shadowsocks.utils.Key
import com.github.shadowsocks.utils.parsePort

object DataStore {
    val publicStore = RoomPreferenceDataStore(PublicDatabase.kvPairDao)

    // hopefully hashCode = mHandle doesn't change, currently this is true from KitKat to Nougat
    private val userIndex by lazy { Binder.getCallingUserHandle().hashCode() }
    private fun getLocalPort(key: String, default: Int): Int {
        val value = publicStore.getInt(key)
        return if (value != null) {
            publicStore.putString(key, value.toString())
            value
        } else parsePort(publicStore.getString(key), default + userIndex)
    }

    val serviceMode get() = publicStore.getString(Key.serviceMode) ?: Key.modeVpn
    val listenAddress get() = if (publicStore.getBoolean(Key.shareOverLan, false)) "0.0.0.0" else "127.0.0.1"
    var portProxy: Int
        get() = getLocalPort(Key.portProxy, 1080)
        set(value) = publicStore.putString(Key.portProxy, value.toString())
    var portLocalDns: Int
        get() = getLocalPort(Key.portLocalDns, 5450)
        set(value) = publicStore.putString(Key.portLocalDns, value.toString())
    var portTransproxy: Int
        get() = getLocalPort(Key.portTransproxy, 8200)
        set(value) = publicStore.putString(Key.portTransproxy, value.toString())
}
