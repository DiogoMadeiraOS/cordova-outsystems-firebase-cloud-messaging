package org.apache.cordova.firebase

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import org.json.JSONArray
import com.outsystems.firebase.cloudmessaging.OSFirebaseCloudMessaging

class OnNotificationOpenReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "OSFirebaseCloudMessaging"
    }
    val OSFCM = OSFirebaseCloudMessaging()
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("OSFCM", "OnNotificationOpenReceiver onReceive called")
        val pm = context.packageManager

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
