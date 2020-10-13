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

import android.nfc.Tag
import android.os.Handler
import android.util.Log
import com.coeater.android.apprtc.model.SignalServerMessage
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

    private var roomID: String? = null
    private var clientID: String? = null

    // Do not remove this member variable. If this is removed, the observer gets garbage collected and
    // this causes test breakages.


    /**
     * Callback interface for messages delivered on WebSocket.
     * All events are dispatched from a looper executor thread.
     */
    interface WebSocketChannelEvents {
        fun onWebSocketMessage(message: String?)
        fun onWebSocketClose()
        fun onWebSocketReady(initiator: Boolean)
        fun onWebSocketGetOffer(message: String)
        fun onWebSocketGetAnswer(message: String)
    }

    fun connect(roomID: String) {
        checkIfCalledOnValidThread()
        socket = IO.socket("http://0d06eb411056.ngrok.io").apply {
            this.on(Socket.EVENT_CONNECT, Emitter.Listener {
                Log.d(TAG, "HELLO")
                this.emit("create or join", roomID)
//            socket?.emit("foo", "hi")
//            socket?.disconnect()
            })
                .on(Socket.EVENT_DISCONNECT, Emitter.Listener {
//            socket?.emit("foo", "hi")
//            socket?.disconnect()
            })
                .on("log", Emitter.Listener {
                    Log.d(TAG, it.toString())
                })
                .on("ready", Emitter.Listener{

                    val initiator = it[0].toString().toBoolean()
                    events.onWebSocketReady(initiator)
                })

                .on("offer", Emitter.Listener{
                    Log.d(TAG, it[0].toString())
                    events.onWebSocketGetOffer(it[0].toString())
                })
                .on("answer", Emitter.Listener{
                    Log.d(TAG, it[0].toString())
                    events.onWebSocketGetAnswer(it[0].toString())
                })
            this.connect()
        }


    }

    fun register(roomID: String, clientID: String) {
        checkIfCalledOnValidThread()
        this.roomID = roomID
        this.clientID = clientID

        // TODO : implement register code!
    }

    fun send(event: String, message: String) {
//        var message = message
        checkIfCalledOnValidThread()

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


    companion object {
        private const val TAG = "WSChannelRTCClient"
        private const val CLOSE_TIMEOUT = 1000
    }

}