package com.coeater.android.tutorial

import android.content.Context
import android.preference.PreferenceManager

class TutorialCacher(private val context: Context) {

    companion object {
        private const val TUTORIAL_OPEN = "TUTORIAL"
    }

    fun updateOpen() {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
            .putBoolean(TUTORIAL_OPEN, true)
            .apply()
    }

    val shouldOpenTutorial: Boolean
        get() {
            return !(PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(TUTORIAL_OPEN, false))
        }
}
