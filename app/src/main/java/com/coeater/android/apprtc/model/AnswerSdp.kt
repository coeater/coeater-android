
package com.coeater.android.apprtc.model

import org.webrtc.SessionDescription


data class AnswerSdp(val sdp: SessionDescription) {
    val state = "answer"
}