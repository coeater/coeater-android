package com.coeater.android.api

import android.content.Context
import android.preference.PreferenceManager
import com.coeater.android.model.UserManage
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

class UserManageProvider(private val context: Context) {

    companion object {
        private const val KEY_AUTH_TOKEN = "auth_token"
    }

    fun updateUserManage(userManage: UserManage) {
        val json = Gson().toJson(userManage)
        PreferenceManager.getDefaultSharedPreferences(context).edit()
            .putString(KEY_AUTH_TOKEN, json)
            .apply()
    }

    val userManage: UserManage?
        get() {
            val rawString = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_AUTH_TOKEN, null)
                ?: return null
            return try {
                Gson().fromJson(rawString, UserManage::class.java)
            } catch (e: JsonSyntaxException) {
                null
            }
        }

    val token: String? get() = userManage?.jwt
}
