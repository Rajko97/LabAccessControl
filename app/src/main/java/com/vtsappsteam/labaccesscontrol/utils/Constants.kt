package com.vtsappsteam.labaccesscontrol.utils

import com.vtsappsteam.labaccesscontrol.R
import java.util.concurrent.TimeUnit

class Constants {
    companion object {
        const val API_ADDRESS: String = "http://192.168.1.102:3000"

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

        const val VTS_STAFF = "vts staff"
        const val NEW_MEMBER = "new member"
        const val SENIOR_MEMBER = "senior member"
        const val RETIRED_MEMBER = "retired member"
        const val COORDINATOR = "coordinator"

        val resourceStringIdsForRanks = mapOf(
            VTS_STAFF to R.string.rank_vts_staff,
            NEW_MEMBER to  R.string.rank_new_member,
            SENIOR_MEMBER to  R.string.rank_senior_member,
            RETIRED_MEMBER to R.string.rank_retired_member,
            COORDINATOR to R.string.rank_coordinator
        )

        //above this point, client is checkedIn
        const val CHECK_IN_WIFI_WIFI_STRENGTH = -50 //dBi 50
        //bellow this point, client is checkedOut
        const val CHECK_OUT_WIFI_STRENGTH = -70 //dBi 70
    }
}
