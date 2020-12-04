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
import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter

/**
 * WebSocket client implementation.
 *
 *
 * All public methods should be called from a looper executor thread
 * passed in a constructor, otherwise exception will be thrown.
 * All events are dispatched on the same thread.
 */
class WebSocketChannelClient(
    private val handler: Handler,
    private val events: WebSocketChannelEvents
) {
    private var socket: Socket? = null

    companion object {
        private const val baseURL = "http://ec2-52-78-98-130.ap-northeast-2.compute.amazonaws.com:4000"
        private const val TAG = "WSChannelRTCClient"
    }

    /**
     * Callback interface for messages delivered on WebSocket.
     * All events are dispatched from a looper executor thread.
     */
    interface WebSocketChannelEvents {
        fun onWebSocketClose()
        fun onWebSocketReady(initiator: Boolean)
        fun onWebSocketGetOffer(message: String)
        fun onWebSocketGetAnswer(message: String)
        fun onWebSocketGetIceCandidate(message: String)
        /**
         * 사용자에게 선택을 요청한다.
         */
        fun onWebSocketPlayLikeness(message: String)
        /**
         * 사용자에게 선택 결과를 통지한다.
         */
        fun onWebSocketMatchLikeness(message: String)
        /**
         * 사용자에게 선택 결과를 통지한다.
         */
        fun onWebSocketEndLikeness(message: String)
        /**

         * 사용자에게 youtube sync data를 통지한다
         */
        fun onYoutubeSyncUpdate(message: String)
        /**
         * 사용자에게 youtube sync 데이터 요청이 왔을 때.
         */
        fun onYoutubeSyncPull(message: String)

         * 이모지를 전송한다.
         */
        fun onWebSocketSendEmoji(message: String)
        /**
         * 이모지를 지운다.
         */
        fun onWebSocketDeleteEmoji()

    }

    fun connect(roomID: String) {
        checkIfCalledOnValidThread()
        socket = IO.socket(baseURL).apply {
            this
                .on(Socket.EVENT_CONNECT, Emitter.Listener {
                    this.emit("create or join", roomID)
                })
                .on(Socket.EVENT_DISCONNECT, Emitter.Listener {
                })
                .on("log", Emitter.Listener {
                    Log.d(TAG, it[0].toString())
                })
                .on("ready", Emitter.Listener {
                    val initiator = it[0].toString().toBoolean()
                    events.onWebSocketReady(initiator)
                })
                .on("offer", Emitter.Listener {
                    Log.d(TAG, it[0].toString())
                    events.onWebSocketGetOffer(it[0].toString())
                })
                .on("answer", Emitter.Listener {
                    Log.d(TAG, it[0].toString())
                    events.onWebSocketGetAnswer(it[0].toString())
                })
                .on("candidate", Emitter.Listener {
                    Log.d(TAG, it[0].toString())
                    events.onWebSocketGetIceCandidate(it[0].toString())
                })
                .on("play likeness", Emitter.Listener {
                    /**
                     * 사용자에게 선택을 요청한다.
                     */
                    Log.d(TAG, it[0].toString())
                    events.onWebSocketPlayLikeness(it[0].toString())
                })
                .on("match likeness", Emitter.Listener {
                    /**
                     * 사용자에게 선택 결과를 통지한다.
                     */
                    Log.d(TAG, it[0].toString())
                    events.onWebSocketMatchLikeness(it[0].toString())
                })
                .on("end likeness", Emitter.Listener {
                    /**
                     * 사용자에게 최 결과를 통지한다.
                     */
                    events.onWebSocketEndLikeness(it[0].toString())
                    Log.d(TAG, it[0].toString())
                })
                .on("youtube sync update", Emitter.Listener {
                    events.onYoutubeSyncUpdate(it[0].toString())
                    Log.d(TAG, it[0].toString())
                })
                .on("youtube sync pull", Emitter.Listener {
                    events.onYoutubeSyncPull(it[0].toString())
                    Log.d(TAG, it[0].toString())
                })
                .on("emoji", Emitter.Listener {
                    /**
                     * 사용자에게 상대방이 보낸 이모지를 출력하게 한다.
                     */
                    events.onWebSocketSendEmoji(it[0].toString())
                })
                .on("delete emoji", Emitter.Listener {
                    /**
                     * 모든 이모지를 지우게 한다.
                     */
                    events.onWebSocketDeleteEmoji()
                })
            this.connect()
        }
    }

    fun send(event: String, message: String) {
//        checkIfCalledOnValidThread()

        socket?.emit(event, message)
    }

    fun disconnect() {
        checkIfCalledOnValidThread()
        // TODO: send signal to Bye to websocket!
        socket?.disconnect()
        socket = null
    }

    // Helper method for debugging purposes. Ensures that WebSocket method is
    // called on a looper thread.
    private fun checkIfCalledOnValidThread() {
        check(!(Thread.currentThread() !== handler.looper.thread)) { "WebSocket method is not called on valid thread" }
    }
}
