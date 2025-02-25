package com.outsystems.firebase.cloudmessaging;

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.res.Resources
import android.content.Context
import android.content.ContentResolver
import android.content.Intent
import android.os.Build
import android.util.Log
import android.text.TextUtils
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.media.AudioAttributes
import android.net.Uri
import android.graphics.Color
import android.app.Activity
import android.app.Application
import android.os.Bundle

import androidx.core.app.NotificationCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.core.content.PermissionChecker.PermissionResult

import com.outsystems.osnotificationpermissions.OSNotificationPermissions
import com.outsystems.plugins.firebasemessaging.controller.*
import com.outsystems.plugins.firebasemessaging.model.FirebaseMessagingError
import com.outsystems.plugins.firebasemessaging.model.database.DatabaseManager
import com.outsystems.plugins.firebasemessaging.model.database.DatabaseManagerInterface
import com.outsystems.plugins.oscordova.CordovaImplementation

import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlin.collections.*
import com.outsystems.firebase.cloudmessaging.OSFirebaseCloudMessageReceiverManager


import me.leolin.shortcutbadger.ShortcutBadger

import java.util.Random


import org.apache.cordova.CallbackContext
import org.apache.cordova.CordovaInterface
import org.apache.cordova.CordovaWebView

import org.json.JSONArray

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class OSFirebaseCMService : FirebaseMessagingService() {
    
    private val CMT_DATA_MESSAGE_TYPE_KEY = "trigger_type"
    private val CMT_DATA_MESSAGE_CUSTOM_TEXT_KEY = "custom_text"
    private val RESULTS_CMT_DATA_MESSAGE_TYPE = "RESULTS"
    private val CMT_SUPPORTED_DATA_MESSAGE_TYPES = listOf(RESULTS_CMT_DATA_MESSAGE_TYPE)
    private val receiverManager = OSFirebaseCloudMessageReceiverManager()
    private val osFCM = OSFirebaseCloudMessaging()
    

    companion object {
        private const val TAG = "OSFirebaseCMService"
        private const val KEY = "badge"
    }
    
    private fun getStringResource(name: String): String {
        val resourceId = resources.getIdentifier(name, "string", packageName)
        return getString(resourceId)
    }
    
    override fun onNewToken(token: String) {
        Log.d("OSFCM", "Refreshed token: $token")
        osFCM.sendToken(token)
    }
        
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("OSFCM","OSFCM - onMessageReceived started")
        Log.d("OSFCM", "Message received from: ${remoteMessage.from}")
        Log.d("OSFCM", "Notification message: ${remoteMessage.notification?.body}")

        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]
        Log.d("OSFCM", "FirebasePluginMessagingService onMessageReceived called")

        // Pass the message to the receiver manager so any registered receivers can decide to handle it
        val wasHandled: Boolean =
            receiverManager.onMessageReceived(remoteMessage)
        if (wasHandled) {
            Log.d("OSFCM", "Message was handled by a registered receiver")

            // Don't process the message in this method.
            return
        }

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        var title: String
        var text: String
        var id: String
        var sound: String? = null
        var lights: String? = null
        val data: MutableMap<String, String> = mutableMapOf()

        remoteMessage.getData().forEach { (key, value) ->
            if (value != null) {
                data[key] = value
            }
}

        val notification = remoteMessage.notification
        if (notification != null) {
            title = notification.getTitle().toString()
            text = notification.getBody().toString()
            id = notification.toString()
        } else {
            title = data["title"].toString()
            text = data["text"].toString()
            id = data["id"].toString()
            sound = data["sound"]
            lights =
                data["lights"] //String containing hex ARGB color, miliseconds on, miliseconds off, example: '#FFFF00FF,1000,3000'
            if (TextUtils.isEmpty(text)) {
                text = data["body"].toString()
            }
            if (TextUtils.isEmpty(text)) {
                text = data["message"].toString()
            }
        }
        if (TextUtils.isEmpty(id)) {
            val rand = Random()
            val n: Int = rand.nextInt(50) + 1
            id = Integer.toString(n)
        }
        var badge = data["badge"]
        if (isCMTNDataMessage(remoteMessage)) {
            data.put("provider", "CMT")
            //Turn this data messages to notifications only if the app is not in the foreground
            if (osFCM.isInBackground()) { 
                val messageContents: HashMap<String, String>? = handleCMTDataMessage(remoteMessage)
                if (messageContents != null) {
                    Log.d("OSFCM", "Data Message received from CMT")
                    title = "YAY"//messageContents.get("title").toString()
                    text = "Finally working :)"//messageContents.get("text").toString()
                }
            }
        }
        Log.d("OSFCM", "From: " + remoteMessage.getFrom())
        Log.d("OSFCM", "Notification Message id: $id")
        Log.d("OSFCM", "Notification Message Title: $title")
        Log.d("OSFCM", "Notification Message Body/Text: $text")
        Log.d("OSFCM", "Notification Message Sound: $sound")
        Log.d("OSFCM", "Notification Message Lights: $lights")
        Log.d("OSFCM", "Notification Badge: $badge")
        if (badge != null && !badge.isEmpty()) {
            setBadgeNumber(badge) //setBadgeNumber(badge)
        }

        // TODO: Add option to developer to configure if show notification when app on foreground
        if (!TextUtils.isEmpty(text) || !TextUtils.isEmpty(title) || !data.isEmpty()) {
            val showNotification =
                (osFCM.isInBackground()/* || !OSFirebaseCloudMessaging.hasNotificationsCallback() */) && (!TextUtils.isEmpty(
                    text
                ) || !TextUtils.isEmpty(title))
            Log.d("OSFCM", "showNotification: " + if (showNotification) "true" else "false")
            
            var jsonArray = JSONArray()

            jsonArray.put(getCurrentBadgeNumber(applicationContext) as Any)
            jsonArray.put(title as Any)
            jsonArray.put(text as Any)
            jsonArray.put("Services")
            jsonArray.put("Services")

            osFCM.sendLocalNotification(jsonArray)
        }
    }



    private fun setBadgeNumber(badge: String) {
        try {
            var count = 0
            var badgeString = badge

            val applicationContext = applicationContext
            if (isPositiveInteger(badgeString)) {
                count = Integer.parseInt(badgeString)
                applyBadgeCount(applicationContext, count)
            } else {
                if (badgeString.startsWith("++") || badgeString.startsWith("--")) {
                    var delta = 0
                    val currentBadgeNumber = getCurrentBadgeNumber(applicationContext)
                    val toIncrement = badgeString.startsWith("++")
                    badgeString = badgeString.substring(2)
                    if (badgeString.isEmpty()) {
                        delta = 1
                    } else {
                        delta = Integer.parseInt(badge)
                    }
                    count = if (toIncrement) currentBadgeNumber + delta else currentBadgeNumber - delta
                    if (count < 0) {
                        count = 0
                    }
                    applyBadgeCount(applicationContext, count)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, e.localizedMessage, e)
        }
    }
    

    private fun getCurrentBadgeNumber(context: Context): Int {
        val settings = context.getSharedPreferences(KEY, Context.MODE_PRIVATE)
        val currentBadgeNumber = settings.getInt(KEY, 0)
        Log.d("OSFCM", "Current badge count: $currentBadgeNumber")
        return currentBadgeNumber
    }
    
    private fun applyBadgeCount(context: Context, count: Int) {
        Log.d("OSFCM", "Applying badge count: $count")
        ShortcutBadger.applyCount(context, count)
        val editor = context.getSharedPreferences(KEY, Context.MODE_PRIVATE).edit()
        editor.putInt(KEY, count)
        editor.apply()
    }

    // Defaults radix = 10
    private fun isPositiveInteger(s: String?): Boolean {
        if (s == null || s.isEmpty()) {
            return false
        }
        for (i in 0 until s.length) {
            if (Character.digit(s[i], 10) < 0) {
                return false
            }
        }
        return true
    }

    private fun isCMTNDataMessage(remoteMessage: RemoteMessage): Boolean {
        return remoteMessage.data.containsKey(CMT_DATA_MESSAGE_TYPE_KEY)
        }
    
        
        private fun handleCMTDataMessage(remoteMessage: RemoteMessage): HashMap<String, String>? {
            Log.d("OSFCM","OSFCM - handleCMTDataMessage started")
    
        val triggerType = remoteMessage.data[CMT_DATA_MESSAGE_TYPE_KEY]
        if (CMT_SUPPORTED_DATA_MESSAGE_TYPES.contains(triggerType)) {
            val text = remoteMessage.data[CMT_DATA_MESSAGE_CUSTOM_TEXT_KEY]
            if (text != null && text.isEmpty()) {
                Log.d("OSFCM", "Expected CMT Data Message properties are empty")
                return null
            }
            var title: String? = null
            if (RESULTS_CMT_DATA_MESSAGE_TYPE == triggerType) {
                title = "New trip"  // getStringResource("new_trip_results_notification_title")
            }
            val properties: HashMap<String, String> = HashMap<String, String>()
            properties.put("text", text.toString())
            properties.put("title",title.toString())
            return properties
        } else {
            Log.d("OSFCM", "Unsupported CMT Data Message Trigger Type: $triggerType")
            return null
        }
    }

    }
