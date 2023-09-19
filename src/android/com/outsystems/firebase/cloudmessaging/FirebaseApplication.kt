package com.outsystems.firebase.cloudmessaging;

import android.app.Application
import com.outsystems.firebase.cloudmessaging.AppForegroundStateManager

class FirebaseApplication : Application() {

    private var foregroundCount = 0

    override fun onCreate() {
        super.onCreate()

        // Register the AppLifecycleObserver
        registerActivityLifecycleCallbacks(FirebaseLifecycleApplication())
    }

    override fun onPause() {
        foregroundCount--
        if (foregroundCount == 0) {
            // App goes from foreground to background
            // You can trigger actions here
            AppForegroundStateManager.setAppInForeground(false)
        }
    }
    override fun onResume() {
        if (foregroundCount == 0) {
            // App goes from background to foreground
            // You can trigger actions here
            AppForegroundStateManager.setAppInForeground(true)
        }
        foregroundCount++
    }


}
