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
import android.content.pm.PackageManager
import android.net.LocalSocket
import android.net.LocalSocketAddress
import android.net.Network
import android.os.ParcelFileDescriptor
import android.system.ErrnoException
import android.system.Os
import android.system.OsConstants
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import me.offeex.exethirteen.Core
import me.offeex.exethirteen.acl.Acl
import me.offeex.exethirteen.R
import me.offeex.exethirteen.net.ConcurrentLocalSocketListener
import me.offeex.exethirteen.net.DefaultNetworkListener
import me.offeex.exethirteen.net.DnsResolverCompat
import me.offeex.exethirteen.net.Subnet
import me.offeex.exethirteen.preference.DataStore
import me.offeex.exethirteen.utils.int
import me.offeex.exethirteen.database.Profile
import me.offeex.exethirteen.utils.Action
import me.offeex.exethirteen.utils.readableMessage
import timber.log.Timber
import java.io.File
import java.io.FileDescriptor
import java.io.IOException
import java.net.URISyntaxException
import java.net.URL
import android.net.VpnService as BaseVpnService

class VpnService : BaseVpnService(), BaseService.Interface {
    companion object {
        private const val VPN_MTU = 1500
        private const val PRIVATE_VLAN4_CLIENT = "172.19.0.1"
        private const val PRIVATE_VLAN4_ROUTER = "172.19.0.2"
        private const val PRIVATE_VLAN6_CLIENT = "fdfe:dcba:9876::1"
        private const val PRIVATE_VLAN6_ROUTER = "fdfe:dcba:9876::2"

        private fun <T> FileDescriptor.use(block: (FileDescriptor) -> T) = try {
            block(this)
        } finally {
            try {
                Os.close(this)
            } catch (_: ErrnoException) {
            }
        }
    }

    private inner class ProtectWorker : ConcurrentLocalSocketListener(
        "ShadowsocksVpnThread",
        File(Core.deviceStorage.noBackupFilesDir, "protect_path")
    ) {
        override fun acceptInternal(socket: LocalSocket) {
            if (socket.inputStream.read() == -1) return
            val success = socket.ancillaryFileDescriptors!!.single()!!.use { fd ->
                underlyingNetwork.let { network ->
                    if (network != null) try {
                        network.bindSocket(fd)
                        return@let true
                    } catch (e: IOException) {
                        when ((e.cause as? ErrnoException)?.errno) {
                            OsConstants.EPERM, OsConstants.EACCES -> Timber.d(e)
//                            OsConstants.EPERM, OsConstants.EACCES, OsConstants.ENONET -> Timber.d(e)
                            else -> Timber.w(e)
                        }
                        return@let false
                    }
                    protect(fd.int)
                }
            }
            try {
                socket.outputStream.write(if (success) 0 else 1)
            } catch (_: IOException) {
            }        // ignore connection early close
        }
    }

    inner class NullConnectionException : NullPointerException() {
        override fun getLocalizedMessage() =
            "Failed to start VPN service. You might need to reboot your device."
    }

    override val data = BaseService.Data(this)
    override val tag: String get() = "ShadowsocksVpnService"
    override fun createNotification(profileName: String): ServiceNotification =
        ServiceNotification(this, "service-vpn")

    private var conn: ParcelFileDescriptor? = null
    private var worker: ProtectWorker? = null
    private var active = false
    private var metered = false

    @Volatile
    private var underlyingNetwork: Network? = null
        set(value) {
            field = value
            if (active) setUnderlyingNetworks(underlyingNetworks)
        }

    // clearing underlyingNetworks makes Android 9 consider the network to be metered
    private val underlyingNetworks get() = arrayOf(underlyingNetwork)

    override fun onBind(intent: Intent) = when (intent.action) {
        SERVICE_INTERFACE -> super<BaseVpnService>.onBind(intent)
        else -> super<BaseService.Interface>.onBind(intent)
    }

    override fun onRevoke() = stopRunner()

    override fun killProcesses(scope: CoroutineScope) {
        super.killProcesses(scope)
        active = false
        scope.launch { DefaultNetworkListener.stop(this) }
        worker?.shutdown(scope)
        worker = null
        conn?.close()
        conn = null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (prepare(this) != null) Timber.d("VPN prepare proyobano")
        else {
            val data = data
            if (data.state != BaseService.State.Stopped) return Service.START_NOT_STICKY
            val profile = intent?.getParcelableExtra<Profile>("profile")
            this as Context
            if (profile == null) {
                // gracefully shutdown: https://stackoverflow.com/q/47337857/2245107
                data.notification = createNotification("")
                stopRunner(false, "Please select a profile")
                return Service.START_NOT_STICKY
            }
            try {
                data.proxy = ProxyInstance(profile)
            } catch (e: IllegalArgumentException) {
                data.notification = createNotification("")
                stopRunner(false, e.message)
                return Service.START_NOT_STICKY
            }

            if (!data.closeReceiverRegistered) {
                ContextCompat.registerReceiver(
                    this,
                    data.closeReceiver,
                    IntentFilter().apply {
                        addAction(Action.RELOAD)
                        addAction(Intent.ACTION_SHUTDOWN)
                        addAction(Action.CLOSE)
                    },
                    ContextCompat.RECEIVER_NOT_EXPORTED
                )
                data.closeReceiverRegistered = true
            }

            data.notification = createNotification(profile.name)

            data.changeState(BaseService.State.Connecting)
            data.connectingJob = GlobalScope.launch(Dispatchers.Main) {
                try {
                    Executable.killAll()    // clean up old processes
                    preInit()
                    if (profile.route == Acl.CUSTOM_RULES) withContext(Dispatchers.IO) {
                            Acl.customRules
                                .flatten(10, this@VpnService::openConnection)
                                .also { Acl.save(Acl.CUSTOM_RULES, it) }
                        }

                    data.processes = GuardedProcessPool {
                        Timber.w(it)
                        stopRunner(false, it.readableMessage)
                    }
                    startProcesses()

                    data.proxy!!.scheduleUpdate()

                    data.changeState(BaseService.State.Connected)
                } catch (_: CancellationException) {
                    // if the job was cancelled, it is canceller's responsibility to call stopRunner
                } catch (exc: Throwable) {
                    when (exc) {
                        is NullConnectionException,
                        is IOException,
                        is URISyntaxException -> Timber.d(exc)

                        else -> Timber.w(exc)
                    }
                    stopRunner(
                        false,
                        "${getString(R.string.service_failed)}: ${exc.readableMessage}"
                    )
                } finally {
                    data.connectingJob = null
                }
            }
            return Service.START_NOT_STICKY
        }
        stopRunner()
        return Service.START_NOT_STICKY
    }

    private suspend fun preInit() =
        DefaultNetworkListener.start(this) { underlyingNetwork = it }

    private suspend fun rawResolver(query: ByteArray) =
    // no need to listen for network here as this is only used for forwarding local DNS queries.
        // retries should be attempted by client.
        DnsResolverCompat.resolveRaw(
            underlyingNetwork ?: throw IOException("no network"),
            query
        )

    suspend fun openConnection(url: URL) =
        DefaultNetworkListener.get().openConnection(url)

    private suspend fun startProcesses() {
        worker = ProtectWorker().apply { start() }

        val context = if (Core.user.isUserUnlocked) Core.app else Core.deviceStorage
        val configRoot = context.noBackupFilesDir
        data.proxy!!.start(
            this,
            File(Core.deviceStorage.noBackupFilesDir, "stat_main"),
            File(configRoot, BaseService.CONFIG_FILE),
            if (data.proxy?.plugin == null) "tcp_and_udp" else "tcp_only"
        )
        data.localDns = LocalDnsWorker(this::rawResolver).apply { start() }

        sendFd(startVpn())
    }

    override val isVpnService get() = true

    private suspend fun startVpn(): FileDescriptor {
        val profile = data.proxy!!.profile
        val builder = Builder()
            .setConfigureIntent(Core.configureIntent(this))
            .setSession(profile.name)
            .setMtu(VPN_MTU)
            .addAddress(PRIVATE_VLAN4_CLIENT, 30)
            .addDnsServer(PRIVATE_VLAN4_ROUTER)

        if (profile.ipv6) builder.addAddress(PRIVATE_VLAN6_CLIENT, 126)

        if (profile.proxyApps) {
            val me = packageName
            profile.individual.split('\n')
                .filter { it != me }
                .forEach {
                    try {
                        if (profile.bypass) builder.addDisallowedApplication(it)
                        else builder.addAllowedApplication(it)
                    } catch (ex: PackageManager.NameNotFoundException) {
                        Timber.w(ex)
                    }
                }
            if (!profile.bypass) builder.addAllowedApplication(me)
        }

        when (profile.route) {
            Acl.ALL, Acl.BYPASS_CHN, Acl.CUSTOM_RULES -> {
                builder.addRoute("0.0.0.0", 0)
                if (profile.ipv6) builder.addRoute("::", 0)
            }

            else -> {
                resources.getStringArray(R.array.bypass_private_route).forEach {
                    val subnet = Subnet.fromString(it)!!
                    builder.addRoute(subnet.address.hostAddress!!, subnet.prefixSize)
                }
                builder.addRoute(PRIVATE_VLAN4_ROUTER, 32)
                // https://issuetracker.google.com/issues/149636790
                if (profile.ipv6) builder.addRoute("2000::", 3)
            }
        }

        metered = profile.metered
        active = true   // possible race condition here?
        builder.setUnderlyingNetworks(underlyingNetworks)
        builder.setMetered(metered)

        val conn = builder.establish() ?: throw NullConnectionException()
        this.conn = conn

        val cmd = arrayListOf(
            File(applicationInfo.nativeLibraryDir, Executable.TUN2SOCKS).absolutePath,
            "--netif-ipaddr", PRIVATE_VLAN4_ROUTER,
            "--socks-server-addr", "${DataStore.listenAddress}:${DataStore.portProxy}",
            "--tunmtu", VPN_MTU.toString(),
            "--sock-path", "sock_path",
            "--dnsgw", "127.0.0.1:${DataStore.portLocalDns}",
            "--loglevel", "warning"
        )
        if (profile.ipv6) {
            cmd += "--netif-ip6addr"
            cmd += PRIVATE_VLAN6_ROUTER
        }
        cmd += "--enable-udprelay"
        data.processes!!.start(cmd, onRestartCallback = {
            try {
                sendFd(conn.fileDescriptor)
            } catch (e: ErrnoException) {
                stopRunner(false, e.message)
            }
        })
        return conn.fileDescriptor
    }

    private suspend fun sendFd(fd: FileDescriptor) {
        var tries = 0
        val path = File(Core.deviceStorage.noBackupFilesDir, "sock_path").absolutePath
        while (true) try {
            delay(50L shl tries)
            LocalSocket().use { localSocket ->
                localSocket.connect(
                    LocalSocketAddress(
                        path,
                        LocalSocketAddress.Namespace.FILESYSTEM
                    )
                )
                localSocket.setFileDescriptorsForSend(arrayOf(fd))
                localSocket.outputStream.write(42)
            }
            return
        } catch (e: IOException) {
            if (tries > 5) throw e
            tries += 1
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        data.binder.close()
    }
}
