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

package me.offeex.exethirteen.bg

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.os.RemoteCallbackList
import android.os.RemoteException
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startForegroundService
import kotlinx.coroutines.*
import me.offeex.exethirteen.Core
import me.offeex.exethirteen.R
import me.offeex.exethirteen.acl.Acl
import me.offeex.exethirteen.aidl.IShadowsocksService
import me.offeex.exethirteen.aidl.IShadowsocksServiceCallback
import me.offeex.exethirteen.aidl.ShadowsocksConnection
import me.offeex.exethirteen.aidl.TrafficStats
import me.offeex.exethirteen.database.Profile
import me.offeex.exethirteen.net.DnsResolverCompat
import me.offeex.exethirteen.utils.Action
import me.offeex.exethirteen.utils.broadcastReceiver
import me.offeex.exethirteen.utils.readableMessage
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.net.URL
import java.net.URLConnection

/**
 * This object uses WeakMap to simulate the effects of multi-inheritance.
 */
object BaseService {
    enum class State(val canStop: Boolean = false) {
        /**
         * Idle state is only used by UI and will never be returned by BaseService.
         */
        Idle,
        Connecting(true),
        Connected(true),
        Stopping,
        Stopped,
    }

    const val CONFIG_FILE = "shadowsocks.conf"
    const val CONFIG_FILE_UDP = "shadowsocks-udp.conf"

    class Data internal constructor(private val service: Interface) {
        var state = State.Stopped
        var processes: GuardedProcessPool? = null
        var proxy: ProxyInstance? = null
        var localDns: LocalDnsWorker? = null

        var notification: ServiceNotification? = null
        val closeReceiver = broadcastReceiver { _, intent ->
            when (intent.action) {
                Intent.ACTION_SHUTDOWN -> service.persistStats()
                Action.RELOAD -> service.forceLoad(intent)
                else -> service.stopRunner()
            }
        }
        var closeReceiverRegistered = false

        val binder = Binder(this)
        var connectingJob: Job? = null

        fun changeState(s: State, msg: String? = null) {
            if (state == s && msg == null) return
            binder.stateChanged(s, msg)
            state = s
        }
    }

    class Binder(private var data: Data? = null) : IShadowsocksService.Stub(),
        CoroutineScope, AutoCloseable {
        private val callbacks =
            object : RemoteCallbackList<IShadowsocksServiceCallback>() {
                override fun onCallbackDied(
                    callback: IShadowsocksServiceCallback?,
                    cookie: Any?
                ) {
                    super.onCallbackDied(callback, cookie)
                    stopListeningForBandwidth(callback ?: return)
                }
            }
        private val bandwidthListeners =
            mutableMapOf<IBinder, Long>()  // the binder is the real identifier
        override val coroutineContext = Dispatchers.Main.immediate + Job()
        private var looper: Job? = null

        override fun getState(): Int = (data?.state ?: State.Idle).ordinal
        override fun getProfileName(): String = data?.proxy?.profile?.name ?: "Idle"

        override fun registerCallback(cb: IShadowsocksServiceCallback) {
            callbacks.register(cb)
        }

        private fun broadcast(work: (IShadowsocksServiceCallback) -> Unit) {
            val count = callbacks.beginBroadcast()
            try {
                repeat(count) {
                    try {
                        work(callbacks.getBroadcastItem(it))
                    } catch (_: RemoteException) {
                    } catch (e: Exception) {
                        Timber.w(e)
                    }
                }
            } finally {
                callbacks.finishBroadcast()
            }
        }

        private suspend fun loop() {
            while (true) {
                delay(bandwidthListeners.values.minOrNull() ?: return)
                val traffic = data?.proxy?.trafficMonitor?.requestUpdate() ?: continue
                if (!traffic.second
                    || data?.state != State.Connected
                    || bandwidthListeners.isEmpty()
                ) continue

                broadcast { item ->
                    if (bandwidthListeners.contains(item.asBinder())) {
                        item.trafficUpdated(profileName, traffic.first)
//                        item.trafficUpdated("", traffic.first)
                    }
                }
            }
        }

        override fun startListeningForBandwidth(
            cb: IShadowsocksServiceCallback,
            timeout: Long
        ) {
            launch {
                if (bandwidthListeners.isEmpty() and (bandwidthListeners.put(
                        cb.asBinder(),
                        timeout
                    ) == null)
                ) {
                    check(looper == null)
                    looper = launch { loop() }
                }
                if (data?.state != State.Connected) return@launch
                var sum = TrafficStats()
                val data = data
                val proxy = data?.proxy ?: return@launch
                proxy.trafficMonitor?.out.also { stats ->
                    cb.trafficUpdated(
                        proxy.profile.name,
                        if (stats == null) sum else {
                            sum += stats
                            stats
                        }
                    )
                }
                cb.trafficUpdated("", sum)
            }
        }

        override fun stopListeningForBandwidth(cb: IShadowsocksServiceCallback) {
            launch {
                if (bandwidthListeners.remove(cb.asBinder()) != null && bandwidthListeners.isEmpty()) {
                    looper!!.cancel()
                    looper = null
                }
            }
        }

        override fun unregisterCallback(cb: IShadowsocksServiceCallback) {
            stopListeningForBandwidth(cb)   // saves an RPC, and safer
            callbacks.unregister(cb)
        }

        fun stateChanged(s: State, msg: String?) = launch {
            val profileName = profileName
            broadcast { it.stateChanged(s.ordinal, profileName, msg) }
        }

        fun trafficPersisted(id: String?) = launch {
            if (bandwidthListeners.isNotEmpty() && id != null) broadcast { item ->
                if (bandwidthListeners.contains(item.asBinder()))
                    item.trafficPersisted(id)
            }
        }

        override fun close() {
            callbacks.kill()
            cancel()
            data = null
        }
    }

    interface Interface {
        val data: Data
        val tag: String
        fun createNotification(profileName: String): ServiceNotification

        fun onBind(intent: Intent): IBinder? =
            if (intent.action == Action.SERVICE) data.binder else null

        fun forceLoad(intent: Intent) {
            val s = data.state
            when {
                s == State.Stopped -> {
                    Timber.d("plis don't let dis shit happen: " + data.proxy?.profile?.name)
                    startRunner(data.proxy!!.profile)
                }

                s.canStop -> stopRunner(true)
                else -> Timber.w("Illegal state $s when invoking use")
            }
        }

        val isVpnService get() = false

        fun startRunner(profile: Profile) {
            Timber.d("Restart nigga 4: $profile")
            Intent(Core.app, ShadowsocksConnection.serviceClass)
                .putExtra("profile", profile)
                .let { Core.app.startForegroundService(it) }
        }

        fun killProcesses(scope: CoroutineScope) {
            data.processes?.run {
                close(scope)
                data.processes = null
            }
            data.localDns?.shutdown(scope)
            data.localDns = null
        }

        fun stopRunner(restart: Boolean = false, msg: String? = null) {
            if (data.state == State.Stopping) return
            // channge the state
            data.changeState(State.Stopping)
            GlobalScope.launch(Dispatchers.Main.immediate) {
                data.connectingJob?.cancelAndJoin() // ensure stop connecting first

                // 🤖🤖🤖🤖 bro thinks he can trick android
                this@Interface as Service

                // we use a coroutineScope here to allow clean-up in parallel
                coroutineScope {
                    killProcesses(this)
                    // clean up receivers
                    val data = data
                    if (data.closeReceiverRegistered) {
                        unregisterReceiver(data.closeReceiver)
                        data.closeReceiverRegistered = false
                    }

                    data.notification?.destroy()
                    data.notification = null

                    val id = data.proxy?.let {
                        it.shutdown(this)
                        it.profile.name
                    }
                    if (!restart) data.proxy = null
                    data.binder.trafficPersisted(id)
                }

                // change the state
                data.changeState(State.Stopped, msg)

                // stop the service if nothing has bound to it
                if (restart) startRunner(data.proxy!!.profile) else stopSelf()
            }
        }

        fun persistStats() = data.proxy?.run { trafficMonitor?.persistStats(profile) }
    }
}
