package com.outsystems.firebase.cloudmessaging;

import android.util.Log
import com.google.firebase.messaging.RemoteMessage
import java.util.ArrayList
import java.util.List
import com.outsystems.firebase.cloudmessaging.OSFirebaseCloudMessageReceiver

 class OSFirebaseCloudMessageReceiverManager {
    private const val TAG = "OSFirebaseCloudMessaging"

    
        
    private val receivers: List<OSFirebaseCloudMessageReceiver> =
        ArrayList<OSFirebaseCloudMessageReceiver>()

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
