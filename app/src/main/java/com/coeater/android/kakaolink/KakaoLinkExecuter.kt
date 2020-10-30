package com.coeater.android.kakaolink

import android.content.Context
import android.preference.PreferenceManager

class KakaoLinkExecuter(private val context: Context) {

    companion object {
        private const val KAKAO_ROOM_CODE = "kakao_link"
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
}
