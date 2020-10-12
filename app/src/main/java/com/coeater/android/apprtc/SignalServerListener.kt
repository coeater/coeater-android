package com.coeater.android.apprtc

import org.json.JSONObject
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

public interface SignalServerListener {

    fun onConnected(connectData: Array<Any>)

    fun createMatchingOffer()

    fun createMatchingAnswer()

    fun onOfferReceived(description: SessionDescription)

    fun onAnswerReceived(description: SessionDescription)

    fun onIceCandidateReceived(iceCandidate: IceCandidate)

    fun onTerminate(terminateState: String)

    fun onError(isCritical: Boolean, showMessage: Boolean = false, message: String? = null)


}