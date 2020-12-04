package com.coeater.android.apprtc

import com.coeater.android.apprtc.model.GameFinalResult
import com.coeater.android.apprtc.model.GameInfo
import com.coeater.android.apprtc.model.GameMatchResult
import com.coeater.android.apprtc.model.YoutubeSyncData
import org.webrtc.IceCandidate
import org.webrtc.PeerConnection.IceServer
import org.webrtc.SessionDescription

/**
 * AppRTCClient is the interface representing an AppRTC client.
 */
interface SignalServerRTCClient {

    /**
     * Asynchronously connect to an AppRTC room URL using supplied connection
     * parameters. Once connection is established onConnectedToRoom()
     * callback with room parameters is invoked.
     */
    fun connectToRoom(roomId: String)

    /**
     * Send offer SDP to the other participant.
     */
    fun sendOfferSdp(sdp: SessionDescription)

    /**
     * Send answer SDP to the other participant.
     */
    fun sendAnswerSdp(sdp: SessionDescription)

    /**
     * Send Ice candidate to the other participant.
     */
    fun sendLocalIceCandidate(candidate: IceCandidate)

    /**
     * 이구동성 게임을 시작한다.
     */
    fun startGameLikeness()

    /**
     * 이구동성 선택을 한다.
     */
    fun sendImageSelectResult(stage: Int, left: Boolean)

    /**
     * 상대에게 영상을 재생시킨다.
     */
    fun pushVideoTime(videoId: String, current: Float)

    /**
     * 상대의 sync데이터 요청에 응답한다
     */
    fun responseVideoTime(videoId: String?, current: Float?)

    /**
     * 상대의 sync데이터 요청을 한다
     */
    fun requestVideoTime()

    /**
     * Disconnect from room.
     */
    fun disconnectFromRoom()

    /**
     * Struct holding the signaling parameters of an AppRTC room.
     */

    class SignalingParameters(
        val iceServers: List<IceServer>,
        val initiator: Boolean
    )

    /**
     * Callback interface for messages delivered on signaling channel.
     *
     *
     * Methods are guaranteed to be invoked on the UI thread of |activity|.
     */
    interface SignalingEvents {
        /**
         * Callback fired once the room's signaling parameters
         * SignalingParameters are extracted.
         */
        fun onConnectedToRoom(params: SignalingParameters)

        /**
         * Callback fired once remote SDP is received.
         */
        fun onRemoteDescription(sdp: SessionDescription)

        /**
         * Callback fired once remote Ice candidate is received.
         */
        fun onRemoteIceCandidate(candidate: IceCandidate)

        /**
         * Callback fired once remote Ice candidate removals are received.
         */
        fun onRemoteIceCandidatesRemoved(candidates: Array<IceCandidate>)

        /**
         * Callback fired once channel is closed.
         */
        fun onChannelClose()

        /**
         * 누군가 게임 시작을 요청할 때, 정보를 전송한다.
         */
        fun onPlayGameLikeness(gameInfo: GameInfo)

        /**
         * 둘이 결과가 나왔을 때, 정보를 전송한다.
         */
        fun onPlayGameMatchResult(matchResult: GameMatchResult)

        /**
         * 둘이 결과가 나왔을 때, 맨 마지막이면 유사도를 보여 준다.
         */
        fun onPlayGameMatchEnd(matchEnd: GameFinalResult)

        /**
         * 상대로부터 youtube sync 데이터를 받으면, 동기화한다.
         */
        fun onYoutubeSyncUpdateHandle(youtubeSync: YoutubeSyncData)

        /**
         * 상대로부터 youtube sync 데이터를 받으면, 동기화한다.
         */
        fun onYoutubeSyncPullHandle()
    }
}
