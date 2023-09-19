package com.outsystems.firebase.cloudmessaging;

import android.app.Application

class FirebaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Register the AppLifecycleObserver
        registerActivityLifecycleCallbacks(FirebaseLifecycleApplication())
    }
}
