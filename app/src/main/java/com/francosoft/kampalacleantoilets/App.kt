package com.francosoft.kampalacleantoilets

import android.app.Application
import com.francosoft.kampalacleantoilets.utilities.helpers.createChannel

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        // Create channel for notifications
        createChannel(this)
    }
}