package me.offeex.exethirteen

import android.app.Application
import androidx.work.Configuration
import com.github.shadowsocks.Core

class App : Application(), Configuration.Provider by Core {
    override fun onCreate() {
        super.onCreate()
        Core.init(this, MainActivity::class)
    }

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        Core.updateNotificationChannels()
    }
}