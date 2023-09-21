package com.outsystems.firebase.cloudmessaging;

import com.google.firebase.messaging.RemoteMessage
import com.outsystems.firebase.cloudmessaging.OSFirebaseCloudMessageReceiverManager

abstract class OSFirebaseCloudMessageReceiver {
    init {
        Log.d("OSFCM","OSFCM - FCM init Receiver started")

        var receiverManager = OSFirebaseCloudMessageReceiverManager()
        receiverManager.register(this)
    }

    /**
     * Concrete subclasses should override this and return true if they handle the received message.
     *
     * @param remoteMessage
     * @return true if the received message was handled by the receiver so should not be handled by FirebasePlugin.
     */
     
    abstract fun onMessageReceived(remoteMessage: RemoteMessage?): Boolean
     
}
