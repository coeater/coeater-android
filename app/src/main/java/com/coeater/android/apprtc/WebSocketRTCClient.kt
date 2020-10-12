/*
 *  Copyright 2014 The WebRTC Project Authors. All rights reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */
package com.coeater.android.apprtc

import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import com.coeater.android.apprtc.AppRTCClient.SignalingEvents
import com.coeater.android.apprtc.WebSocketChannelClient.WebSocketChannelEvents
import com.coeater.android.apprtc.model.AnswerSdp
import com.coeater.android.apprtc.model.OfferSdp
import com.coeater.android.apprtc.model.SignalIceCandidate
import com.google.gson.Gson
import org.json.JSONException
import org.json.JSONObject
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription


/**
 * Negotiates signaling for chatting with https://appr.tc "rooms".
 * Uses the client<->server specifics of the apprtc AppEngine webapp.
 *
 *
 * To use: create an instance of this object (registering a message handler) and
 * call connectToRoom().  Once room connection is established
 * onConnectedToRoom() callback with room parameters is invoked.
 * Messages to other party (with local Ice candidates and answer SDP) can
 * be sent after WebSocket connection is established.
 */
class WebSocketRTCClient(private val events: SignalingEvents) : AppRTCClient,
    WebSocketChannelEvents {


    private val handler: Handler
    private var wsClient: WebSocketChannelClient? = null

    override fun connectToRoom() {
        handler.post { connectToRoomInternal() }
    }

    override fun disconnectFromRoom() {
        handler.post {
            disconnectFromRoomInternal()
            handler.looper.quit()
        }
    }

    // Connects to room - function runs on a local looper thread.
    private fun connectToRoomInternal() {
        wsClient = WebSocketChannelClient(handler, this)
        wsClient?.connect()
    }

    // Disconnect from room and send bye messages - runs on a local looper thread.
    private fun disconnectFromRoomInternal() {
        wsClient?.disconnect()
    }



    // Send local offer SDP to the other participant.
    override fun sendOfferSdp(sdp: SessionDescription) {
        handler.post(Runnable {

            val offerSdp = OfferSdp(sdp)
            wsClient?.send(offerSdp)
            // TODO: Send Signal
        })
    }

    // Send local answer SDP to the other participant.
    override fun sendAnswerSdp(sdp: SessionDescription) {
        handler.post(Runnable {
            val answerSdp = AnswerSdp(sdp)
            wsClient?.send(answerSdp)
        })
    }

    // Send Ice candidate to the other participant.
    override fun sendLocalIceCandidate(candidate: IceCandidate) {
        handler.post(Runnable {
            val signal = SignalIceCandidate(candidate.sdpMLineIndex, candidate.sdpMid, candidate.sdp)

        })
    }

    // Send removed Ice candidates to the other participant.
    override fun sendLocalIceCandidateRemovals(candidates: Array<IceCandidate>) {
        // TODO: Check really need?
//        handler.post(Runnable {
//            val json = JSONObject()
//            jsonPut(
//                json,
//                "type",
//                "remove-candidates"
//            )
//            val jsonArray = JSONArray()
//            for (candidate in candidates) {
//                jsonArray.put(toJsonCandidate(candidate))
//            }
//            jsonPut(
//                json,
//                "candidates",
//                jsonArray
//            )
//            if (initiator) {
//                // Call initiator sends ice candidates to GAE server.
//                if (roomState != ConnectionState.CONNECTED) {
//                    reportError("Sending ICE candidate removals in non connected state.")
//                    return@Runnable
//                }
//            } else {
//                // Call receiver sends ice candidates to websocket server.
//                wsClient!!.send(json.toString())
//            }
//        })
    }

    // --------------------------------------------------------------------
    // WebSocketChannelEvents interface implementation.
    // All events are called by WebSocketChannelClient on a local looper thread
    // (passed to WebSocket client constructor).
    override fun onWebSocketMessage(msg: String?) {
        try {
            var json = JSONObject(msg)
            val msgText = json.getString("msg")
            if (msgText.length > 0) {
                json = JSONObject(msgText)
                val type = json.optString("type")
                if (type == "candidate") {
                    val gson = Gson()
                    val signalIceCandidate = gson.fromJson("value", SignalIceCandidate::class.java)
                    events.onRemoteIceCandidate(signalIceCandidate)


                } else if (type == "remove-candidates") {
//                    val candidateArray = json.getJSONArray("candidates")
//                    val candidates =
//                        mutableListOf<IceCandidate>()
//                    for (i in 0 until candidateArray.length()) {
//                        candidates.add(toJavaCandidate(candidateArray.getJSONObject(i)))
//                    }
//                    events.onRemoteIceCandidatesRemoved(candidates.toTypedArray())
                } else if (type == "answer") {
                    val gson = Gson()
                    val answerSdp = gson.fromJson("value", AnswerSdp::class.java)
                    events.onRemoteDescription(answerSdp.sdp)
                } else if (type == "offer") {
                    val gson = Gson()
                    val offerSdp = gson.fromJson("value", OfferSdp::class.java)
                    events.onRemoteDescription(offerSdp)

//                    events.onRemoteDescription(sdp)
//                    if (!initiator) {
//                        val sdp = SessionDescription(
//                            SessionDescription.Type.fromCanonicalForm(type), json.getString("sdp")
//                        )
//                        events.onRemoteDescription(sdp)
//                    } else {
//                        reportError("Received offer for call receiver: $msg")
//                    }
                } else if (type == "bye") {
                    events.onChannelClose()
                }
            }
        } catch (e: JSONException) {
       }
    }

    override fun onWebSocketClose() {
        events.onChannelClose()
    }



    companion object {
        private const val TAG = "WSRTCClient"
    }

    init {
        val handlerThread =
            HandlerThread(TAG)
        handlerThread.start()
        handler = Handler(handlerThread.looper)
    }
}