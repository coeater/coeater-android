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
import com.coeater.android.apprtc.SignalServerRTCClient.SignalingEvents
import com.coeater.android.apprtc.WebSocketChannelClient.WebSocketChannelEvents
import com.coeater.android.apprtc.model.*
import com.google.gson.Gson
import org.webrtc.IceCandidate
import org.webrtc.PeerConnection
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
class WebSocketRTCClient(private val events: SignalingEvents) : SignalServerRTCClient,
    WebSocketChannelEvents {

    private val handler: Handler
    private var wsClient: WebSocketChannelClient? = null
    private var roomId: String = ""

    override fun connectToRoom(roomId: String) {
        handler.post { connectToRoomInternal(roomId) }
    }

    override fun disconnectFromRoom() {
        handler.post {
            disconnectFromRoomInternal()
            handler.looper.quit()
        }
    }

    // Connects to room - function runs on a local looper thread.
    private fun connectToRoomInternal(roomId: String) {
        wsClient = WebSocketChannelClient(handler, this)
        wsClient?.connect(roomId)
        this.roomId = roomId
    }

    // Disconnect from room and send bye messages - runs on a local looper thread.
    private fun disconnectFromRoomInternal() {
        wsClient?.disconnect()
    }

    // Send local offer SDP to the other participant.
    override fun sendOfferSdp(sdp: SessionDescription) {
        handler.post(Runnable {

            val offerSdp = OfferSdp(sdp.description)
            val gson = Gson()
            val json = gson.toJson(offerSdp)
            wsClient?.send("offer", json)
            // TODO: Send Signal
        })
    }

    // Send local answer SDP to the other participant.
    override fun sendAnswerSdp(sdp: SessionDescription) {
        handler.post(Runnable {
            val answerSdp = AnswerSdp(sdp.description)
            val gson = Gson()
            val json = gson.toJson(answerSdp)

            wsClient?.send("answer", json)
        })
    }

    // Send Ice candidate to the other participant.
    override fun sendLocalIceCandidate(candidate: IceCandidate) {
        handler.post(Runnable {
            val signal = SignalIceCandidate(candidate.sdpMLineIndex, candidate.sdpMid, candidate.sdp)
            val gson = Gson()
            val json = gson.toJson(signal)
            wsClient?.send("send iceCandidate", json)
        })
    }

    override fun startGameLikeness() {
        handler.post(Runnable {
            wsClient?.send("start likeness", "")
        })
    }

    override fun sendImageSelectResult(stage: Int, left: Boolean) {
        handler.post(Runnable {
            val gameSelect = GameSelect(stage, left)
            val gson = Gson()
            val json = gson.toJson(gameSelect)
            wsClient?.send("likeness result", json)
        })
    }

    override fun pushVideoTime(videoId: String, current: Float)  {
        handler.post(Runnable {
            val youtubeSyncData = YoutubeSyncData(videoId, current)
            val gson = Gson()
            val json = gson.toJson(youtubeSyncData)
            wsClient?.send("youtube sync push", json)
        })
    }

    override fun responseVideoTime(videoId: String?, current: Float?)  {
        handler.post(Runnable {
            val youtubeSyncData = YoutubeSyncData(videoId, current)
            val gson = Gson()
            val json = gson.toJson(youtubeSyncData)
            wsClient?.send("youtube sync response", json)
        })
    }

    override fun requestVideoTime()  {
        handler.post(Runnable {
            wsClient?.send("youtube sync request", "")
        })
    }




    // --------------------------------------------------------------------
    // WebSocketChannelEvents interface implementation.
    // All events are called by WebSocketChannelClient on a local looper thread
    // (passed to WebSocket client constructor).

    override fun onWebSocketReady(initiator: Boolean) {
        val stunServer = PeerConnection.IceServer
            .builder("stun:stun.l.google.com:19302")
            .createIceServer()
        val turnServer = PeerConnection.IceServer.builder("turn:ec2-3-34-96-48.ap-northeast-2.compute.amazonaws.com:3478")
            .setUsername("test")
            .setPassword("test")
            .createIceServer()
        Log.d(TAG, initiator.toString() + "on WebSocket Ready!")
        val parameter = SignalServerRTCClient.SignalingParameters(listOf(stunServer, turnServer), initiator)
        events.onConnectedToRoom(parameter)
    }

    override fun onWebSocketGetOffer(message: String) {
        Log.d(TAG, message)
        val gson = Gson()
        val offer = gson.fromJson(message, OfferSdp::class.java)
        val sdp = SessionDescription(
            SessionDescription.Type.OFFER, offer.sessionDescription
        )
        events.onRemoteDescription(sdp)
    }

    override fun onWebSocketGetAnswer(message: String) {
        Log.d(TAG, message)
        val gson = Gson()
        val answer = gson.fromJson(message, AnswerSdp::class.java)
        val sdp = SessionDescription(
            SessionDescription.Type.ANSWER, answer.sessionDescription
        )
        events.onRemoteDescription(sdp)
    }

    override fun onWebSocketGetIceCandidate(message: String) {
        Log.d(TAG, message)
        val gson = Gson()
        val signalIceCandidate = gson.fromJson(message, SignalIceCandidate::class.java)
        events.onRemoteIceCandidate(IceCandidate(signalIceCandidate.id, signalIceCandidate.label, signalIceCandidate.sdp))
    }

    override fun onWebSocketClose() {
        events.onChannelClose()
    }

    override fun onWebSocketPlayLikeness(message: String) {
        val gson = Gson()
        val gameInfo = gson.fromJson(message, GameInfo::class.java)
        events.onPlayGameLikeness(gameInfo)
    }

    override fun onWebSocketMatchLikeness(message: String) {
        val gson = Gson()
        val gameMatchResult = gson.fromJson(message, GameMatchResult::class.java)
        events.onPlayGameMatchResult(gameMatchResult)
    }

    override fun onWebSocketEndLikeness(message: String) {
        val gson = Gson()
        val gameFinalResult = gson.fromJson(message, GameFinalResult::class.java)
        events.onPlayGameMatchEnd(gameFinalResult)
    }

    override fun onYoutubeSyncUpdate(message: String) {
        val gson = Gson()
        val youtubeSyncData = gson.fromJson(message, YoutubeSyncData::class.java)
        events.onYoutubeSyncUpdateHandle(youtubeSyncData)
    }

    override fun onYoutubeSyncPull(message: String) {
        events.onYoutubeSyncPullHandle()
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
