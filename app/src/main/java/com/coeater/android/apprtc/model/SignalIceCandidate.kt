package com.coeater.android.apprtc.model

import org.webrtc.SessionDescription


data class SignalIceCandidate(val label: Int, val id: String, val sdp: String) {
    val state = "candidate"
}