package me.offeex.exethirteen

import com.github.shadowsocks.bg.BaseService
import kotlinx.coroutines.flow.MutableStateFlow

object States {
    val connected = MutableStateFlow(BaseService.State.Idle)
}