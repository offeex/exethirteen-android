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

package me.offeex.exethirteen.acl

import android.content.Context
import androidx.work.*
import me.offeex.exethirteen.Core
import me.offeex.exethirteen.utils.useCancellable
import timber.log.Timber
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

class AclSyncer(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {
    companion object {
        private const val KEY_ROUTE = "route"

        fun schedule(route: String) {
            if (!Core.user.isUserUnlocked) return    // work does not support this
            WorkManager.getInstance(Core.app).enqueueUniqueWork(
                    route, ExistingWorkPolicy.REPLACE, OneTimeWorkRequestBuilder<AclSyncer>().run {
                setInputData(Data.Builder().putString(KEY_ROUTE, route).build())
                setConstraints(Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.UNMETERED)
                        .setRequiresCharging(true)
                        .build())
                setInitialDelay(10, TimeUnit.SECONDS)
                build()
            })
        }
    }

    override suspend fun doWork(): Result = try {
        val route = inputData.getString(KEY_ROUTE)!!
        val connection = URL("https://shadowsocks.org/acl/android/v1/$route.acl").openConnection() as HttpURLConnection
        val acl = connection.useCancellable { inputStream.bufferedReader().use { it.readText() } }
        Acl.getFile(route).printWriter().use { it.write(acl) }
        Result.success()
    } catch (e: IOException) {
        Timber.d(e)
        if (runAttemptCount > 5) Result.failure() else Result.retry()
    }
}
