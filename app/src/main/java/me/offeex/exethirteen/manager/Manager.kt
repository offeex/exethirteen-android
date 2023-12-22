package me.offeex.exethirteen.manager

import androidx.activity.ComponentActivity

abstract class Manager {
    internal lateinit var activity: ComponentActivity

    fun init(activity: ComponentActivity) {
        this.activity = activity
        init()
    }

    protected open fun init() {}
}