package com.coeater.android.webrtc.game.model

data class CallGameChoice(val leftImage: String, val rightImage: String, val leftText: String, val rightText: String, val stage: Int)
data class CallGameMatch(val isMatched: Boolean, val choice: CallGameChoice)
data class CallGameResult(val similarity: Int)