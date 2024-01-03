package me.offeex.exethirteen.manager

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.lifecycleScope
import me.offeex.exethirteen.database.Profile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.offeex.exethirteen.model.ServerChoice
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.math.roundToInt

object LatencyManager : Manager() {
    val latencies = mutableStateMapOf<ServerChoice, Int>()
//    val lowestLatencyChoice: ServerChoice
//        get() = latencies.minBy { it.value }.key

    private suspend fun updateLatencies() {
        ServerChoice.values().forEach {
            val latency = withContext(Dispatchers.IO) { latency(it.profile) }
            latencies[it] = latency
        }
    }

    private fun latency(profile: Profile): Int {
        var avg = 0
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
        return avg
    }

    override fun init() {
        activity.lifecycleScope.launch { updateLatencies() }
    }

}