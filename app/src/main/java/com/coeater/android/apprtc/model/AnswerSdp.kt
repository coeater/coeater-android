
package com.coeater.android.apprtc.model

import org.webrtc.SessionDescription


data class AnswerSdp(val sessionDescription: String, val room: String) {
    val state = "answer"
}