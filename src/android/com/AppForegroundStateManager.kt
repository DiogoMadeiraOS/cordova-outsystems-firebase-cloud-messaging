package com.outsystems.firebase.cloudmessaging;

object AppForegroundStateManager {
    private var isAppInForeground = false

    fun isAppInForeground(): Boolean {
        return isAppInForeground
    }

    fun setAppInForeground(isForeground: Boolean) {
        isAppInForeground = isForeground
    }
}
