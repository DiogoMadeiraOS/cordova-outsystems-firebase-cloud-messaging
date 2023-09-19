import android.app.Activity
import android.app.Application
import android.os.Bundle

class FirebaseLifecycleApplication : Application.ActivityLifecycleCallbacks {
    private var foregroundCount = 0

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        // Not needed, but you can perform actions when an activity is created
    }

    override fun onActivityStarted(activity: Activity) {
        if (foregroundCount == 0) {
            // App goes from background to foreground
            // You can trigger actions here
            AppForegroundStateManager.setAppInForeground(true)
        }
        foregroundCount++
    }

    override fun onActivityResumed(activity: Activity) {
        // Not needed, but you can perform actions when an activity is resumed
    }

    override fun onActivityPaused(activity: Activity) {
        // Not needed, but you can perform actions when an activity is paused
    }

    override fun onActivityStopped(activity: Activity) {
        foregroundCount--
        if (foregroundCount == 0) {
            // App goes from foreground to background
            // You can trigger actions here
            AppForegroundStateManager.setAppInForeground(false)
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        // Not needed, but you can perform actions when an activity's state is saved
    }

    override fun onActivityDestroyed(activity: Activity) {
        // Not needed, but you can perform actions when an activity is destroyed
    }
}
