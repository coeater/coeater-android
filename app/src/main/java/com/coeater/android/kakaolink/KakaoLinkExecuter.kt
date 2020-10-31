package com.coeater.android.kakaolink

import android.content.Context
import android.preference.PreferenceManager

class KakaoLinkExecuter(private val context: Context) {

    companion object {
        private const val KAKAO_ROOM_CODE = "kakao_room_code"
        private const val KAKAO_USER_CODE = "kakao_user_code"
    }

    fun updatedRoomCode(roomCode: String) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
            .putString(KAKAO_ROOM_CODE, roomCode)
            .apply()
    }

    fun deleteRoomCode() {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
            .remove(KAKAO_ROOM_CODE)
            .apply()
    }

    val roomCode: String?
    get() {
        val rawString = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(KAKAO_ROOM_CODE, null)
            ?: return null
        return rawString
    }


    fun updatedUserCode(userCode: String) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
            .putString(KAKAO_USER_CODE, userCode)
            .apply()
    }

    fun deleteUserCode() {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
            .remove(KAKAO_USER_CODE)
            .apply()
    }

    val userCode: String?
        get() {
            val rawString = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KAKAO_USER_CODE, null)
                ?: return null
            return rawString
        }
}
