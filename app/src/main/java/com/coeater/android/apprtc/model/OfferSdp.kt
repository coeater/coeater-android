package com.coeater.android.apprtc.model

import org.webrtc.SessionDescription


data class OfferSdp(val sessionDescription: String, val room: String) {

}