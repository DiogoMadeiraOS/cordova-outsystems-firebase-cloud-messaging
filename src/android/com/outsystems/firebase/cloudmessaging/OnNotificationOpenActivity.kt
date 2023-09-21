package com.outsystems.firebase.cloudmessaging;

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import org.json.JSONArray

class OnNotificationOpenActivity : Activity() {

    companion object {
        private const val TAG = "OSFirebaseCloudMessaging"
    }

    private val osFCM = OSFirebaseCloudMessaging(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("OSFCM", "OSFCM - OnNotificationOpenActivity onCreate called")
        super.onCreate(savedInstanceState)
        processNotification()
        finish()
    }

    override fun onNewIntent(intent: Intent) {
        Log.d("OSFCM", "OSFCM - OnNotificationOpenActivity onNewIntent called with $intent")
        super.onNewIntent(intent)
        processNotification()
        finish()
    }

    private fun processNotification() {
        Log.d("OSFCM","OSFCM - FCM processNotification started")

        val context: Context = applicationContext
        val pm: PackageManager = context.packageManager
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