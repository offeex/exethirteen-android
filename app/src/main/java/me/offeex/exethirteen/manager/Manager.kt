package me.offeex.exethirteen.manager

import androidx.appcompat.app.AppCompatActivity

abstract class Manager {
    internal lateinit var activity: AppCompatActivity

    fun init(activity: AppCompatActivity) {
        this.activity = activity
        init()
    }

    protected open fun init() {}
}