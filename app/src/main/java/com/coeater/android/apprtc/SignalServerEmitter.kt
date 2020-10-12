package com.coeater.android.apprtc

import org.json.JSONObject
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

interface SignalServerEmitter {

    fun initializeSocket(url: String)

    fun sendOfferAnswerToSocket(sessionDescription: SessionDescription)

    fun sendIceCandidateToSocket(iceCandidate: IceCandidate)

    fun disconnectSocket()
}