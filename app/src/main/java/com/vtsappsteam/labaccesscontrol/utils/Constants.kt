package com.vtsappsteam.labaccesscontrol.utils

import java.util.concurrent.TimeUnit

class Constants {
    companion object {
        private const val API_ADDRESS: String = "http://192.168.137.1:3000"

        @JvmField val API_ROUTE_LOGIN = loginRoute()
        private fun loginRoute(): String {
            return "${this.API_ADDRESS}/users/login"
        }

        @JvmField val API_ROUTE_UPDATE_FIREBASE_TOKEN = updateTokenRoute()
        private fun updateTokenRoute() : String {
            return "${this.API_ADDRESS}/users/updateFirebaseToken"
        }

        const val PREF_NAME = "app-settings"

        @JvmField val DIALOG_MINIMUM_CLOSE_TIME = dialogMinimumCloseTime()
        private fun dialogMinimumCloseTime() : Long{
            return TimeUnit.MILLISECONDS.toMillis(1500)
        }
    }
}
