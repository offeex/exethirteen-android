package me.offeex.exethirteen.manager

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import me.offeex.exethirteen.database.Profile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.offeex.exethirteen.model.ServerChoice
import timber.log.Timber
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import kotlin.math.roundToInt

object LatencyManager : Manager() {
    val latencies = mutableStateMapOf<ServerChoice, Int?>()
//    val lowestLatencyChoice: ServerChoice
//        get() = latencies.minBy { it.value }.key

    private fun updateLatencies() {
        ServerChoice.values().forEach {
            latencies[it] = 0
            activity.lifecycleScope.launch(Dispatchers.IO) {
                latencies[it] = latency(it.profile)
            }
        }
    }

    private fun latency(profile: Profile): Int {
        var avg = -1
        try {
            val process = Runtime.getRuntime().exec("/system/bin/ping -c 1 ${profile.host}")
            BufferedReader(InputStreamReader(process.inputStream)).forEachLine {
                // it: "PING 194.194.194.194 (194.194.194.194) 56(84) bytes of data."
                // it: "rtt min/avg/max/mdev = 326.137/326.137/326.137/0.000 ms"
                if (it.contains("rtt ")) avg = Regex("\\d+\\.\\d+")
                    .findAll(it)
                    .toList()[1]
                    .value
                    .toFloat()
                    .roundToInt()
            }
        } catch (e: IOException) {
            Timber.e(e)
            avg = -2
        }
        return avg
    }

    override fun init() {
        updateLatencies()
    }

}