/*******************************************************************************
 *                                                                             *
 *  Copyright (C) 2018 by Max Lv <max.c.lv@gmail.com>                          *
 *  Copyright (C) 2018 by Mygod Studio <contact-shadowsocks-android@mygod.be>  *
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

package me.offeex.exethirteen

import android.app.*
import android.app.admin.DevicePolicyManager
import android.content.*
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.UserManager
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.work.Configuration
import me.offeex.exethirteen.aidl.ShadowsocksConnection
import me.offeex.exethirteen.database.Profile
import me.offeex.exethirteen.preference.DataStore
import me.offeex.exethirteen.utils.Action
import me.offeex.exethirteen.utils.DeviceStorageApp
import me.offeex.exethirteen.utils.Key
import kotlinx.coroutines.DEBUG_PROPERTY_NAME
import kotlinx.coroutines.DEBUG_PROPERTY_VALUE_ON
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.io.IOException
import kotlin.reflect.KClass

object Core : Configuration.Provider {
    lateinit var app: Application
        @VisibleForTesting set
    lateinit var configureIntent: (Context) -> PendingIntent
    val activity by lazy { app.getSystemService<ActivityManager>()!! }
    val connectivity by lazy { app.getSystemService<ConnectivityManager>()!! }
    val notification by lazy { app.getSystemService<NotificationManager>()!! }
    val user by lazy { app.getSystemService<UserManager>()!! }
    val packageInfo: PackageInfo by lazy { getPackageInfo(app.packageName) }
    val deviceStorage by lazy { DeviceStorageApp(app) }

    var currentProfile: Profile? = null

    fun init(app: Application, configureClass: KClass<out Any>) {
        this.app = app
        this.configureIntent = {
            PendingIntent.getActivity(
                it,
                0,
                Intent(it, configureClass.java)
                    .setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT),
                PendingIntent.FLAG_IMMUTABLE
            )
        }

        // overhead of debug mode is minimal: https://github.com/Kotlin/kotlinx.coroutines/blob/f528898/docs/debugging.md#debug-mode
        System.setProperty(DEBUG_PROPERTY_NAME, DEBUG_PROPERTY_VALUE_ON)
        Timber.plant(object : Timber.DebugTree() {
            override fun log(
                priority: Int,
                tag: String?,
                message: String,
                t: Throwable?
            ) {
                if (t == null) {
                    if (priority != Log.DEBUG || BuildConfig.DEBUG) Log.println(
                        priority,
                        tag,
                        message
                    )
                } else {
                    if (priority >= Log.WARN || priority == Log.DEBUG) Log.println(
                        priority,
                        tag,
                        message
                    )
                }
            }
        })

        if (DataStore.publicStore.getLong(
                Key.assetUpdateTime,
                -1
            ) != packageInfo.lastUpdateTime
        ) {
            val assetManager = app.assets
            try {
                for (file in assetManager.list("acl")!!) assetManager.open("acl/$file")
                    .use { input ->
                        File(deviceStorage.noBackupFilesDir, file).outputStream()
                            .use { output -> input.copyTo(output) }
                    }
            } catch (e: IOException) {
                Timber.w(e)
            }
            DataStore.publicStore.putLong(Key.assetUpdateTime, packageInfo.lastUpdateTime)
        }
        updateNotificationChannels()
    }

    override fun getWorkManagerConfiguration() = Configuration.Builder().apply {
        setDefaultProcessName(app.packageName + ":bg")
        setMinimumLoggingLevel(if (BuildConfig.DEBUG) Log.VERBOSE else Log.INFO)
        setExecutor { GlobalScope.launch { it.run() } }
        setTaskExecutor { GlobalScope.launch { it.run() } }
    }.build()

    fun updateNotificationChannels() {
        notification.createNotificationChannels(
            listOf(
                NotificationChannel(
                    "service-vpn", app.getText(R.string.service_vpn),
                    NotificationManager.IMPORTANCE_MIN
                ),   // #1355
            )
        )
        notification.deleteNotificationChannel("service-nat")   // NAT mode is gone for good
    }

    fun getPackageInfo(packageName: String) = app.packageManager.getPackageInfo(
        packageName,
        PackageManager.GET_SIGNING_CERTIFICATES
    )!!

    fun startService() {
        Intent(app, ShadowsocksConnection.serviceClass)
            .putExtra("profile", currentProfile)
            .let { app.startForegroundService(it) }
    }

    fun reloadService() {
        app.sendBroadcast(Intent(Action.RELOAD).setPackage(app.packageName))
        Timber.d("Restart nigga 0")
    }

    fun stopService() =
        app.sendBroadcast(Intent(Action.CLOSE).setPackage(app.packageName))
}
