package com.outsystems.firebase.cloudmessaging;

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.text.TextUtils
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
import com.outsystems.firebase.cloudmessaging.AppForegroundStateManager

import java.util.Random


import org.apache.cordova.CallbackContext
import org.apache.cordova.CordovaInterface
import org.apache.cordova.CordovaWebView


import org.json.JSONArray

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;





class OSFirebaseCloudMessaging : CordovaImplementation() {

    override var callbackContext: CallbackContext? = null
    private lateinit var notificationManager : FirebaseNotificationManagerInterface
    private lateinit var messagingManager : FirebaseMessagingManagerInterface
    private lateinit var controller : FirebaseMessagingController
    private lateinit var databaseManager: DatabaseManagerInterface

    private var deviceReady: Boolean = false
    private val eventQueue: MutableList<String> = mutableListOf()
    private var notificationPermission = OSNotificationPermissions()
    private var receiverManager = OSFirebaseCloudMessageReceiverManager()
    
    
    private val CMT_DATA_MESSAGE_TYPE_KEY = "trigger_type"
    private val CMT_DATA_MESSAGE_CUSTOM_TEXT_KEY = "custom_text"
    private val RESULTS_CMT_DATA_MESSAGE_TYPE = "RESULTS"
    private val CMT_SUPPORTED_DATA_MESSAGE_TYPES = listOf(RESULTS_CMT_DATA_MESSAGE_TYPE)
    

    companion object {
        private const val CHANNEL_NAME_KEY = "default_notification_channel_name"
        private const val CHANNEL_DESCRIPTION_KEY = "default_notification_channel_description"
        private const val ERROR_FORMAT_PREFIX = "OS-PLUG-FCMS-"
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 123123
        private const val NOTIFICATION_PERMISSION_SEND_LOCAL_REQUEST_CODE = 987987
        private const val TAG = "OSFirebaseCloudMessaging"
    }
    
    override fun initialize(cordova: CordovaInterface, webView: CordovaWebView) {
        super.initialize(cordova, webView)
        databaseManager = DatabaseManager.getInstance(getActivity())
        notificationManager = FirebaseNotificationManager(getActivity(), databaseManager)
        messagingManager = FirebaseMessagingManager()
        controller = FirebaseMessagingController(controllerDelegate, messagingManager, notificationManager)

        setupChannelNameAndDescription()

        val intent = getActivity().intent
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val extras = intent.extras
        val extrasSize = extras?.size() ?: 0
        if(extrasSize > 0) {
            val scheme = extras?.getString(FirebaseMessagingOnActionClickActivity.ACTION_DEEP_LINK_SCHEME)
            if (scheme.isNullOrEmpty()) {
                FirebaseMessagingOnClickActivity.notifyClickNotification(intent)
            }
            else {
                FirebaseMessagingOnActionClickActivity.notifyClickAction(intent)
            }
        }
    }

    private val controllerDelegate = object: FirebaseMessagingInterface {
        override fun callback(result: String) {
            sendPluginResult(result)
        }
        override fun callbackNotifyApp(event: String, result: String) {
            val js = "cordova.plugins.OSFirebaseCloudMessaging.fireEvent(" +
                    "\"" + event + "\"," + result + ");"
            if(deviceReady) {
                triggerEvent(js)
            }
            else {
                eventQueue.add(js)
            }
        }
        override fun callbackSuccess() {
            sendPluginResult(true)
        }
        override fun callbackBadgeNumber(number: Int) {
            //Does nothing on android
        }
        override fun callbackError(error: FirebaseMessagingError) {
            sendPluginResult(null, Pair(formatErrorCode(error.code), error.description))
        }
    }

    private fun ready() {
        deviceReady = true
        eventQueue.forEach { event ->
            triggerEvent(event)
        }
        eventQueue.clear()

        if(Build.VERSION.SDK_INT >= 33 &&
            !notificationPermission.hasNotificationPermission(this)) {

            notificationPermission.requestNotificationPermission(
                this,
                NOTIFICATION_PERMISSION_SEND_LOCAL_REQUEST_CODE)
        }

    }

    override fun execute(action: String, args: JSONArray, callbackContext: CallbackContext): Boolean {
        this.callbackContext = callbackContext
        val result = runBlocking {
            when (action) {
                "ready" -> {
                    ready()
                }
                "getToken" -> {
                    controller.getToken()
                }
                "subscribe" -> {
                    args.getString(0)?.let { topic ->
                        controller.subscribe(topic)
                    }
                }
                "unsubscribe" -> {
                    args.getString(0)?.let { topic ->
                        controller.unsubscribe(topic)
                    }
                }
                "registerDevice" -> {
                    registerWithPermission()
                }
                "unregisterDevice" -> {
                    controller.unregisterDevice()
                }
                "clearNotifications" -> {
                    clearNotifications()
                }
                "sendLocalNotification" -> {
                    sendLocalNotification(args)
                }
                "setBadge" -> {
                    setBadgeNumber()
                }
                "getBadge" -> {
                    getBadgeNumber()
                }
                "getPendingNotifications" -> {
                    args.getBoolean(0).let { clearFromDatabase ->
                        controller.getPendingNotifications(clearFromDatabase)
                    }
                }
                else -> false
            }
            true
        }
        return result
    }

    override fun onRequestPermissionResult(requestCode: Int,
                                           permissions: Array<String>,
                                           grantResults: IntArray) {
        when(requestCode) {
            NOTIFICATION_PERMISSION_REQUEST_CODE -> {
                CoroutineScope(IO).launch {
                    controller.registerDevice()
                }
            }
        }
    }

    override fun areGooglePlayServicesAvailable(): Boolean {
        // Not used in this project.
        return false
    }

    private fun getBadgeNumber() {
        controller.getBadgeNumber()
    }

    private suspend fun registerWithPermission() {
        val hasPermission = notificationPermission.hasNotificationPermission(this)
        if(Build.VERSION.SDK_INT < 33 || hasPermission) {
            controller.registerDevice()
        }
        else {
            notificationPermission
                .requestNotificationPermission(this, NOTIFICATION_PERMISSION_REQUEST_CODE)
        }
    }

    @Override
    fun onMessageReceived(remoteMessage: RemoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]
        Log.d(TAG, "FirebasePluginMessagingService onMessageReceived called")

        // Pass the message to the receiver manager so any registered receivers can decide to handle it
        val wasHandled: Boolean =
            receiverManager.onMessageReceived(remoteMessage)
        if (wasHandled) {
            Log.d(TAG, "Message was handled by a registered receiver")

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
        var data: mutableMapOf<String, String> = remoteMessage.getData().filterValues { it != null }.mapValues { it.value!! }

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
        val badge = data["badge"]
        if (isCMTNDataMessage(remoteMessage)) {
            data.put("provider", "CMT")
            //Turn this data messages to notifications only if the app is not in the foreground
            if (AppForegroundStateManager.isAppInForeground()) {
                val messageContents: HashMap<String, String>? = handleCMTDataMessage(remoteMessage)
                if (messageContents != null) {
                    Log.d(TAG, "Data Message received from CMT")
                    title = messageContents.get("title").toString()
                    text = messageContents.get("text").toString()
                }
            }
        }
        Log.d(TAG, "From: " + remoteMessage.getFrom())
        Log.d(TAG, "Notification Message id: $id")
        Log.d(TAG, "Notification Message Title: $title")
        Log.d(TAG, "Notification Message Body/Text: $text")
        Log.d(TAG, "Notification Message Sound: $sound")
        Log.d(TAG, "Notification Message Lights: $lights")
        Log.d(TAG, "Notification Badge: $badge")
        if (badge != null && !badge.isEmpty()) {
            setBadgeNumber() //setBadgeNumber(badge)
        }

        // TODO: Add option to developer to configure if show notification when app on foreground
        if (!TextUtils.isEmpty(text) || !TextUtils.isEmpty(title) || !data.isEmpty()) {
            val showNotification =
                (AppForegroundStateManager.isAppInForeground()/* || !OSFirebaseCloudMessaging.hasNotificationsCallback() */) && (!TextUtils.isEmpty(
                    text
                ) || !TextUtils.isEmpty(title))
            Log.d(TAG, "showNotification: " + if (showNotification) "true" else "false")

            val jsonArray = JSONArray()

            jsonArray.put(badge as Any)
            jsonArray.put(title as Any)
            jsonArray.put(text as Any)
            
            sendLocalNotification( jsonArray)
        }
    }

    private fun sendLocalNotification(args : JSONArray) {
        val badge = args.get(0).toString().toInt()
        val title = args.get(1).toString()
        val text = args.get(2).toString()
        controller.sendLocalNotification(badge, title, text, null, CHANNEL_NAME_KEY, CHANNEL_DESCRIPTION_KEY)
    }

    private fun clearNotifications() {
        controller.clearNotifications()
    }

    private fun setBadgeNumber() {
        controller.setBadgeNumber()
    }

    private fun setupChannelNameAndDescription(){
        val channelName = getActivity().getString(getStringResourceId("default_notification_channel_name"))
        val channelDescription = getActivity().getString(getStringResourceId("default_notification_channel_description"))

        if(!channelName.isNullOrEmpty()){
            val editorName = getActivity().getSharedPreferences(CHANNEL_NAME_KEY, Context.MODE_PRIVATE).edit()
            editorName.putString(CHANNEL_NAME_KEY, channelName)
            editorName.apply()
        }
        if(!channelDescription.isNullOrEmpty()){
            val editorDescription = getActivity().getSharedPreferences(CHANNEL_DESCRIPTION_KEY, Context.MODE_PRIVATE).edit()
            editorDescription.putString(CHANNEL_DESCRIPTION_KEY, channelDescription)
            editorDescription.apply()
        }
    }

    private fun getStringResourceId(typeAndName: String): Int {
        return getActivity().resources.getIdentifier(typeAndName, "string", getActivity().packageName)
    }

    private fun formatErrorCode(code: Int): String {
        return ERROR_FORMAT_PREFIX + code.toString().padStart(4, '0')
    }

    private fun isCMTNDataMessage(remoteMessage: RemoteMessage): Boolean {
    return remoteMessage.data.containsKey(CMT_DATA_MESSAGE_TYPE_KEY)
    }

    
    private fun handleCMTDataMessage(remoteMessage: RemoteMessage): HashMap<String, String>? {
    val triggerType = remoteMessage.data[CMT_DATA_MESSAGE_TYPE_KEY]
    if (CMT_SUPPORTED_DATA_MESSAGE_TYPES.contains(triggerType)) {
        val text = remoteMessage.data[CMT_DATA_MESSAGE_CUSTOM_TEXT_KEY]
        if (text != null && text.isEmpty()) {
            Log.d(TAG, "Expected CMT Data Message properties are empty")
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
        Log.d(TAG, "Unsupported CMT Data Message Trigger Type: $triggerType")
        return null
    }
}
}
