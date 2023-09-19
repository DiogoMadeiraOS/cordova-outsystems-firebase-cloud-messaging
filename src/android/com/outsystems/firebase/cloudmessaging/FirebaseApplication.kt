package com.outsystems.firebase.cloudmessaging;

import android.app.Application
import android.util.Log

class FirebaseApplication : Application() {

    private var foregroundCount = 0

    override fun onCreate() {
        super.onCreate()
        Log.d("OSFCM","OSFCM - Application.onCreate started")

        // Register the AppLifecycleObserver
        registerActivityLifecycleCallbacks(FirebaseLifecycleApplication())
    }

}
