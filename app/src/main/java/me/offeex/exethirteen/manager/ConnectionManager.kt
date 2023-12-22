package me.offeex.exethirteen.manager

import android.os.RemoteException
import android.text.format.Formatter
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.preference.PreferenceDataStore
import com.github.shadowsocks.Core
import com.github.shadowsocks.aidl.IShadowsocksService
import com.github.shadowsocks.aidl.ShadowsocksConnection
import com.github.shadowsocks.aidl.TrafficStats
import com.github.shadowsocks.bg.BaseService
import com.github.shadowsocks.database.ProfileManager
import com.github.shadowsocks.preference.DataStore
import com.github.shadowsocks.preference.OnPreferenceDataStoreChangeListener
import com.github.shadowsocks.utils.DirectBoot
import com.github.shadowsocks.utils.Key
import com.github.shadowsocks.utils.StartService
import kotlinx.coroutines.flow.MutableStateFlow
import me.offeex.exethirteen.model.ServerChoice
import timber.log.Timber

object ConnectionManager : ShadowsocksConnection.Callback,
    OnPreferenceDataStoreChangeListener, Manager() {
    private val _choice = mutableStateOf(ServerChoice.DE)
    val choice: State<ServerChoice> get() = _choice

    private val _connected = mutableStateOf(BaseService.State.Idle)
    val connected: State<BaseService.State> get() = _connected

    private val connection = ShadowsocksConnection(true)

    private val _uprate = mutableStateOf("0")
    val upRate: String @Composable get() = _uprate.value
    private val _downrate = MutableStateFlow("0")
    val downRate: String @Composable get() = _downrate.collectAsState().value

    private lateinit var serviceLauncher: ActivityResultLauncher<Void?>

    var bandwidthTimeout: Long
        get() = connection.bandwidthTimeout
        set(value) {
            connection.bandwidthTimeout = value
        }

    private fun changeState(state: BaseService.State, msg: String? = null) {
        if (msg != null) Timber.tag("ChangeState").e("smthing fucked up: $msg")
        if (state != BaseService.State.Connected) resetStats()
        _connected.value = state
    }

    fun switchProfile(choice: ServerChoice) {
        _choice.value = choice
        ProfileManager.createProfile(choice.profile)
        if (choice.profile.id in Core.activeProfileIds && DataStore.directBootAware) {
            DirectBoot.update()
        }
        Core.switchProfile(choice.profile.id)
    }

    fun bindService() = connection.connect(activity, this)
    fun unbindService() = connection.disconnect(activity)

    fun toggle() =
        if (connected.value.canStop) Core.stopService()
        else serviceLauncher.launch(null)

    fun reconnect() {
        if (connected.value.canStop) Core.stopService()
        serviceLauncher.launch(null)
    }


    override fun init() {
        serviceLauncher = activity.registerForActivityResult(StartService()) {
            if (it) Timber.tag("ServiceToggle").d("Pizda provodam")
        }
        bindService()
        DataStore.publicStore.registerChangeListener(this)

        switchProfile(ServerChoice.DE)
    }

    override fun onPreferenceDataStoreChanged(store: PreferenceDataStore, key: String) {
        when (key) {
            Key.serviceMode -> {
                unbindService()
                bindService()
            }
        }
    }

    private fun resetStats() {
        _uprate.value = "0"
        _downrate.value = "0"
    }

    override fun stateChanged(
        state: BaseService.State,
        profileName: String?,
        msg: String?
    ) = changeState(state, msg)

    override fun trafficUpdated(profileId: Long, stats: TrafficStats) {
        _uprate.value = Formatter.formatFileSize(activity, stats.txRate)
        _downrate.value = Formatter.formatFileSize(activity, stats.rxRate)
    }

    override fun trafficPersisted(profileId: Long) = resetStats()

    override fun onServiceConnected(service: IShadowsocksService) =
        changeState(
            try {
                BaseService.State.values()[service.state]
            } catch (_: RemoteException) {
                BaseService.State.Idle
            }
        )

    override fun onServiceDisconnected() =
        changeState(BaseService.State.Idle)

    override fun onBinderDied() {
        bindService()
        unbindService()
    }
}