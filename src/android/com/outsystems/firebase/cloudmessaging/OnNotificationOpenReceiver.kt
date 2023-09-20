package com.outsystems.firebase.cloudmessaging;

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import org.json.JSONArray

class OnNotificationOpenReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "OSFirebaseCloudMessaging"
    }

    private val osFCM = OSFirebaseCloudMessaging()

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "OnNotificationOpenReceiver onReceive called")
        val pm = context.packageManager

        val launchIntent = pm.getLaunchIntentForPackage(context.packageName)
        launchIntent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)

        val data = intent.extras ?: Bundle()
        data.putBoolean("tap", true)

        val jsonArray = JSONArray()

        jsonArray.put(1 as Any)
        jsonArray.put("Okuvisir" as Any)
        jsonArray.put("PAYD" as Any)
        jsonArray.put("Services" as Any)
        jsonArray.put("Services" as Any)

        osFCM.sendLocalNotification(jsonArray)

        launchIntent?.putExtras(data)
        context.startActivity(launchIntent)
    }
}