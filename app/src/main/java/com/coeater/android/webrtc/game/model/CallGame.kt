package com.coeater.android.webrtc.game.model

sealed class CallGameStatus

data class CallGameChoice(val leftImage: String, val rightImage: String, val stage: Int): CallGameStatus()
data class CallGameMatch(val isMatched: Boolean): CallGameStatus()
data class CallGameResult(val similarity: Int): CallGameStatus()
