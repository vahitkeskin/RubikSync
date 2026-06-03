package com.vahitkeskin.rubiksync

import android.app.Application
import android.content.pm.ApplicationInfo
import timber.log.Timber

class RubikSyncApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val isDebuggable = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        if (isDebuggable) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
