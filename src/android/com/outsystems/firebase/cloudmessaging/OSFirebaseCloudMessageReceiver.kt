package com.outsystems.firebase.cloudmessaging;

import com.google.firebase.messaging.RemoteMessage

abstract class OSFirebaseCloudMessageReceiver {
    init {
        OSFirebaseCloudMessageReceiverManager.register(this)
    }

    /**
     * Concrete subclasses should override this and return true if they handle the received message.
     *
     * @param remoteMessage
     * @return true if the received message was handled by the receiver so should not be handled by FirebasePlugin.
     */
     companion object{
        abstract fun onMessageReceived(remoteMessage: RemoteMessage?): Boolean
     }
}
