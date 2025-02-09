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

import android.os.Build
import android.os.SystemClock
import android.system.ErrnoException
import android.system.Os
import android.system.OsConstants
import androidx.annotation.MainThread
import me.offeex.exethirteen.Core
import me.offeex.exethirteen.utils.Commandline
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.io.InputStream
import kotlin.concurrent.thread

class GuardedProcessPool(private val onFatal: suspend (IOException) -> Unit) : CoroutineScope {
    private inner class Guard(private val cmd: List<String>) {
        private lateinit var process: Process

        private fun streamLogger(input: InputStream, logger: (String) -> Unit) = try {
            input.bufferedReader().forEachLine(logger)
        } catch (_: IOException) { }    // ignore

        fun start() {
//            fun File?.eblanishe() = this?.list()?.joinToString(" ")
//            val libss = File(cmd[0])
//            Timber.tag("GuardProcessPool").d("muha-cc parent: ${libss.parentFile.eblanishe()}")
//            Timber.tag("GuardProcessPool").d("muha-cc parent 2: ${libss.parentFile?.parentFile.eblanishe()}")
//            Timber.tag("GuardProcessPool").d("muha-cc parent 3: ${libss.parentFile?.parentFile?.parentFile.eblanishe()}")
//            Timber.tag("GuardProcessPool").d("muha-cc parent 4: ${libss.parentFile?.parentFile?.parentFile?.parentFile.eblanishe()}")
//            Timber.tag("GuardProcessPool").d("muha-galichina: ${cmd.joinToString(" || ")}")
            process = ProcessBuilder(cmd).directory(Core.deviceStorage.noBackupFilesDir).start()
        }

        suspend fun looper(onRestartCallback: (suspend () -> Unit)?) {
            var running = true
            val cmdName = File(cmd.first()).nameWithoutExtension
            val exitChannel = Channel<Int>()
            try {
                while (true) {
                    thread(name = "stderr-$cmdName") {
                        streamLogger(process.errorStream) { Timber.tag(cmdName).e(it) }
                    }
                    thread(name = "stdout-$cmdName") {
                        streamLogger(process.inputStream) { Timber.tag(cmdName).v(it) }
                        // this thread also acts as a daemon thread for waitFor
                        runBlocking { exitChannel.send(process.waitFor()) }
                    }
                    val startTime = SystemClock.elapsedRealtime()
                    val exitCode = exitChannel.receive()
                    running = false
                    when {
                        SystemClock.elapsedRealtime() - startTime < 1000 -> throw IOException(
                                "$cmdName exits too fast (exit code: $exitCode)")
                        exitCode == 128 + OsConstants.SIGKILL -> Timber.w("$cmdName was killed")
                        else -> Timber.w(IOException("$cmdName unexpectedly exits with code $exitCode"))
                    }
                    Timber.i("restart process: ${Commandline.toString(cmd)} (last exit code: $exitCode)")
                    start()
//                    running = true
                    onRestartCallback?.invoke()
                }
            } catch (e: IOException) {
                Timber.w("error occurred. stop guard: ${Commandline.toString(cmd)}")
                GlobalScope.launch(Dispatchers.Main) { onFatal(e) }
            } finally {
                if (running) withContext(NonCancellable) {  // clean-up cannot be cancelled
                    process.destroy()                       // kill the process
                    if (withTimeoutOrNull(1000) { exitChannel.receive() } != null) return@withContext
                    process.destroyForcibly()           // Force to kill the process if it's still alive
                    exitChannel.receive()
                }                                           // otherwise process already exited, nothing to be done
            }
        }
    }

    override val coroutineContext = Dispatchers.Main.immediate + Job()

    @MainThread
    fun start(cmd: List<String>, onRestartCallback: (suspend () -> Unit)? = null) {
        Timber.i("start process: ${Commandline.toString(cmd)}")
        Guard(cmd).apply {
            start() // if start fails, IOException will be thrown directly
            launch { looper(onRestartCallback) }
        }
    }

    @MainThread
    fun close(scope: CoroutineScope) {
        cancel()
        coroutineContext[Job]!!.also { job -> scope.launch { job.join() } }
    }
}
