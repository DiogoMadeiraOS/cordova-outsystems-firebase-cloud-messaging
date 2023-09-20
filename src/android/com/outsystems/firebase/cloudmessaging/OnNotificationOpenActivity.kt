package org.apache.cordova.firebase

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import org.json.JSONArray
import com.outsystems.firebase.cloudmessaging.OSFirebaseCloudMessaging

class OnNotificationOpenActivity : Activity() {

    companion object {
        private const val TAG = "OSFirebaseCloudMessaging"
    }
    val OSFCM = OSFirebaseCloudMessaging()
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("OSFCM", "OnNotificationOpenActivity onCreate called")
        super.onCreate(savedInstanceState)
        processNotification()
        finish()
    }

    override fun onNewIntent(intent: Intent) {
        Log.d("OSFCM", "OnNotificationOpenActivity onNewIntent called")
        super.onNewIntent(intent)
        processNotification()
        finish()
    }

    private fun processNotification() {
        val context: Context = applicationContext
        val pm: PackageManager = context.packageManager
        val launchIntent = pm.getLaunchIntentForPackage(context.packageName)
        launchIntent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)

        val data = intent.extras ?: Bundle()
        data.putBoolean("tap", true)

        val badge = 1
        val title = "Okuvisir"
        val text = ""

        val jsonArray = JSONArray()

            jsonArray.put(badge as Any)
            jsonArray.put(title as Any)
            jsonArray.put(text as Any)

        OSFCM.sendLocalNotification(jsonArray)

        launchIntent?.putExtras(data)
        context.startActivity(launchIntent)
    }
}
