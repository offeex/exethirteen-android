package me.offeex.exethirteen.manager

import android.os.RemoteException
import android.text.format.Formatter
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceDataStore
import me.offeex.exethirteen.Core
import me.offeex.exethirteen.aidl.ShadowsocksConnection
import me.offeex.exethirteen.aidl.TrafficStats
import me.offeex.exethirteen.bg.BaseService
import me.offeex.exethirteen.preference.DataStore
import me.offeex.exethirteen.preference.OnPreferenceDataStoreChangeListener
import me.offeex.exethirteen.utils.Key
import me.offeex.exethirteen.utils.StartService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import me.offeex.exethirteen.aidl.IShadowsocksService
import me.offeex.exethirteen.model.ServerChoice
import timber.log.Timber

object ConnectionManager : ShadowsocksConnection.Callback,
    OnPreferenceDataStoreChangeListener, Manager() {
    private var _choice by mutableStateOf(ServerChoice.UK)
    val choice: ServerChoice get() = _choice

    private var _connected by mutableStateOf(BaseService.State.Idle)
    val connected: BaseService.State get() = _connected

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
        _connected = state
    }

    fun switchProfile(choice: ServerChoice) {
        _choice = choice
        Core.currentProfile = choice.profile
    }

    fun bindService() = connection.connect(activity, this)
    fun unbindService() = connection.disconnect(activity)

    fun toggle() =
        if (connected.canStop) Core.stopService()
        else serviceLauncher.launch(null)

    fun reconnect() {
        unbindService()
        if (connected == BaseService.State.Stopped) serviceLauncher.launch(null)
        else Core.reloadService()
        bindService()
    }


    override fun init() {
        serviceLauncher = activity.registerForActivityResult(StartService()) {
            if (it) Timber.tag("ServiceToggle").d("Pizda provodam")
        }
        bindService()
        DataStore.publicStore.registerChangeListener(this)

//        switchProfile(LatencyManager.lowestLatencyChoice)
        switchProfile(ServerChoice.values()[0])
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

    override fun trafficUpdated(profileId: String, stats: TrafficStats) {
        _uprate.value = Formatter.formatFileSize(activity, stats.txRate)
        _downrate.value = Formatter.formatFileSize(activity, stats.rxRate)
    }

    override fun trafficPersisted(profileId: String) = resetStats()

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
        unbindService()
    }
}