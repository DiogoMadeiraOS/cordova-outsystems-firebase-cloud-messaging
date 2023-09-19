package com.outsystems.firebase.cloudmessaging;

import android.app.Application

class FirebaseApplication : Application() {

    private var foregroundCount = 0

    override fun onCreate() {
        super.onCreate()

        // Register the AppLifecycleObserver
        registerActivityLifecycleCallbacks(FirebaseLifecycleApplication())
    }

}
