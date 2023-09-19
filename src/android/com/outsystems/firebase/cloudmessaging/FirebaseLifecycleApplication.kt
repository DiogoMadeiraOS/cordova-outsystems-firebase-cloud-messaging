package com.outsystems.firebase.cloudmessaging;

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import com.outsystems.firebase.cloudmessaging.AppForegroundStateManager

class FirebaseLifecycleApplication : Application.ActivityLifecycleCallbacks {
    private var foregroundCount = 0

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        // Not needed, but you can perform actions when an activity is created
        Log.d("OSFCM","OSFCM - onActivityCreated started")

    }

    override fun onActivityStarted(activity: Activity) {
        if (foregroundCount == 0) {
            // App goes from background to foreground
            // You can trigger actions here
            AppForegroundStateManager.setAppInForeground(true)
            Log.d("OSFCM","OSFCM - onActivityStarted started")

        }
        foregroundCount++
    }

    override fun onActivityResumed(activity: Activity) {
        // Not needed, but you can perform actions when an activity is resumed
        Log.d("OSFCM","OSFCM - onActivityResumed started")

    }

    override fun onActivityPaused(activity: Activity) {
        // Not needed, but you can perform actions when an activity is paused
        Log.d("OSFCM","OSFCM - onActivityPaused started")

    }

    override fun onActivityStopped(activity: Activity) {
        foregroundCount--
        if (foregroundCount == 0) {
            // App goes from foreground to background
            // You can trigger actions here
            AppForegroundStateManager.setAppInForeground(false)
            Log.d("OSFCM","OSFCM - onActivityStopped started")

        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        // Not needed, but you can perform actions when an activity's state is saved
    }

    override fun onActivityDestroyed(activity: Activity) {
        // Not needed, but you can perform actions when an activity is destroyed
    }
}
