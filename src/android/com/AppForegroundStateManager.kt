object AppForegroundStateManager {
    private var isAppInForeground = false

    fun isAppInForeground(): Boolean {
        return isAppInForeground
    }

    fun setAppInForeground(isForeground: Boolean) {
        isAppInForeground = isForeground
    }
}
