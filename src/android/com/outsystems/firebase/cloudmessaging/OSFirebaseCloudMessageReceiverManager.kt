package org.apache.cordova.firebase

import android.util.Log
import com.google.firebase.messaging.RemoteMessage
import java.util.ArrayList
import java.util.List

object OSFirebaseCloudMessageReceiverManager{
    private const val TAG = "OSFirebaseCloudMessaging"

    companion object{
        
    private val receivers: List<FirebasePluginMessageReceiver> =
        ArrayList<FirebasePluginMessageReceiver>()

    fun register(receiver: OSFirebaseCloudMessageReceiver?) {
        Log.d(TAG, "FirebasePluginMessageReceiverManager register called")
        receivers.add(receiver)
    }

    fun onMessageReceived(remoteMessage: RemoteMessage?): Boolean {
        Log.d(TAG, "FirebasePluginMessageReceiverManager onMessageReceived called")
        var handled = false
        for (receiver in receivers) {
            val wasHandled: Boolean = receiver.onMessageReceived(remoteMessage)
            if (wasHandled) {
                handled = true
            }
        }
        Log.d(
            TAG,
            "FirebasePluginMessageReceiverManager onMessageReceived handled: " + if (handled) "true" else "false"
        )
        return handled
    }
    }
}
