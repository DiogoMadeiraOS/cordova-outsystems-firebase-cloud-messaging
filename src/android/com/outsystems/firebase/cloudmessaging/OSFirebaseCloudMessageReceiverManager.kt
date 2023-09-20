package com.outsystems.firebase.cloudmessaging;

import android.util.Log
import com.google.firebase.messaging.RemoteMessage
import java.util.ArrayList
import java.util.List
import com.outsystems.firebase.cloudmessaging.OSFirebaseCloudMessageReceiver

 class OSFirebaseCloudMessageReceiverManager {
    companion object{
    private const val TAG = "OSFirebaseCloudMessaging"
    }

    

    private val receivers: ArrayList<OSFirebaseCloudMessageReceiver> =
        ArrayList<OSFirebaseCloudMessageReceiver>()

    fun register(receiver: OSFirebaseCloudMessageReceiver) {
        Log.d("OSFCM", "FirebasePluginMessageReceiverManager register called")
        receivers.add(receiver)
    }
    
      fun onMessageReceived(remoteMessage: RemoteMessage?): Boolean {
          Log.d("OSFCM", "FirebasePluginMessageReceiverManager onMessageReceived called")
          Log.d("OSFCM","OSFCM - ready started")

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