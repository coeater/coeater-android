package com.coeater.android.apprtc.model

import org.webrtc.SessionDescription


data class OfferSdp(val sdp: SessionDescription) {
    val state = "offer"
}