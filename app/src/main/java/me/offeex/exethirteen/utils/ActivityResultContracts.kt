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

package me.offeex.exethirteen.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.VpnService
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import me.offeex.exethirteen.Core
import me.offeex.exethirteen.preference.DataStore
import timber.log.Timber

class StartService : ActivityResultContract<Void?, Boolean>() {
    private var cachedIntent: Intent? = null

    override fun getSynchronousResult(context: Context, input: Void?): SynchronousResult<Boolean>? {
        VpnService.prepare(context)?.let { intent ->
            cachedIntent = intent
            return null
        }
        Core.startService()
        return SynchronousResult(false)
    }

    override fun createIntent(context: Context, input: Void?) = cachedIntent!!.also { cachedIntent = null }

    override fun parseResult(resultCode: Int, intent: Intent?) = if (resultCode == Activity.RESULT_OK) {
        Core.startService()
        false
    } else {
        Timber.e("Failed to start VpnService: $intent")
        true
    }
}
