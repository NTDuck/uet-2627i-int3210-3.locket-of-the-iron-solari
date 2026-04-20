package com.solari.app

import android.app.Application
import com.solari.app.data.di.AppContainer

class SolariApplication : Application() {
    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(applicationContext)
    }
}
