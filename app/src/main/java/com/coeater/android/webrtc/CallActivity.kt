package com.coeater.android.webrtc

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.coeater.android.apprtc.AppRTCClient
import com.coeater.android.apprtc.PeerConnectionClient
import java.security.SecureRandom
import java.util.*
import org.webrtc.*
import org.webrtc.RendererCommon.ScalingType

import com.coeater.android.R

class CallActivity : Activity(), AppRTCClient.SignalingEvents,
    PeerConnectionClient.PeerConnectionEvents {

    private val remoteProxyRenderer: ProxyRenderer = ProxyRenderer()
    private val localProxyVideoSink: ProxyVideoSink = ProxyVideoSink()
    private val remoteRenderers: MutableList<VideoRenderer.Callbacks> =
        ArrayList<VideoRenderer.Callbacks>()
    private var peerConnectionClient: PeerConnectionClient = PeerConnectionClient()
    private var appRtcClient: AppRTCClient? = null
    private var signalingParameters: AppRTCClient.SignalingParameters? = null
    private var pipRenderer: SurfaceViewRenderer = findViewById(R.id.pip_video_view)
    private var fullscreenRenderer: SurfaceViewRenderer? = null
    private var logToast: Toast? = null
    private var activityRunning = false
    private var roomConnectionParameters: AppRTCClient.RoomConnectionParameters? = null
    private var peerConnectionParameters: PeerConnectionClient.PeerConnectionParameters? = null
    private var iceConnected = false
    private var isError = false
    private var callStartedTimeMs: Long = 0
    private var micEnabled = true
    private var isSwappedFeeds = false

    // Control buttons for limited UI
    private var disconnectButton: ImageButton = findViewById(R.id.button_call_disconnect)
    private var cameraSwitchButton: ImageButton = findViewById(R.id.button_call_switch_camera)
    private var toggleMuteButton: ImageButton = findViewById(R.id.button_call_toggle_mic)
    public override fun onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)
        iceConnected = false
        signalingParameters = null

        // Add buttons click events.
        disconnectButton.setOnClickListener(View.OnClickListener { onCallHangUp() })
        cameraSwitchButton.setOnClickListener(View.OnClickListener { onCameraSwitch() })
        toggleMuteButton.setOnClickListener(View.OnClickListener {
            val enabled = onToggleMic()
            toggleMuteButton.setAlpha(if (enabled) 1.0f else 0.3f)
        })

        // Swap feeds on pip view click.
        pipRenderer.setOnClickListener(View.OnClickListener { setSwappedFeeds(!isSwappedFeeds) })
        remoteRenderers.add(remoteProxyRenderer)

        // Create video renderers.
        pipRenderer.init(peerConnectionClient.getRenderContext(), null)
        pipRenderer.setScalingType(ScalingType.SCALE_ASPECT_FIT)
        fullscreenRenderer?.init(peerConnectionClient.getRenderContext(), null)
        fullscreenRenderer?.setScalingType(ScalingType.SCALE_ASPECT_FILL)
        pipRenderer.setZOrderMediaOverlay(true)
        pipRenderer.setEnableHardwareScaler(true /* enabled */)
        fullscreenRenderer?.setEnableHardwareScaler(true /* enabled */)
        // Start with local feed in fullscreen and swap it to the pip when the call is connected.
        setSwappedFeeds(true /* isSwappedFeeds */)

        // Generate a random room ID with 7 uppercase letters and digits
        val randomRoomID = randomString(7, UPPER_ALPHA_DIGITS)
        // Show the random room ID so that another client can join from https://appr.tc
        val roomIdTextView = findViewById<TextView>(R.id.roomID)
        roomIdTextView.text = randomRoomID + ""
        Log.d(
            TAG,
            randomRoomID + ""
        )

        // Connect video call to the random room
        connectVideoCall(randomRoomID)
    }

    // Create a random string
    private fun randomString(length: Int, characterSet: String): String {
        val sb =
            StringBuilder() // consider using StringBuffer if needed
        for (i in 0 until length) {
            val randomInt = SecureRandom().nextInt(characterSet.length)
            sb.append(characterSet.substring(randomInt, randomInt + 1))
        }
        return sb.toString()
    }

    // Join video call with randomly generated roomId
    private fun connectVideoCall(roomId: String) {
        val roomUri = Uri.parse(APPRTC_URL)
        val videoWidth = 0
        val videoHeight = 0
        peerConnectionParameters = PeerConnectionClient.PeerConnectionParameters(
            true,
            false,
            false,
            videoWidth,
            videoHeight,
            0, 1700,
            "VP8",
            true,
            false, getString(R.string.pref_startaudiobitratevalue_default).toInt(),
            getString(R.string.pref_audiocodec_default),
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            null
        )

        // Create connection client. Use the standard WebSocketRTCClient.
        // DirectRTCClient could be used for point-to-point connection
        appRtcClient = WebSocketRTCClient(this)
        // Create connection parameters.
        roomConnectionParameters = AppRTCClient.RoomConnectionParameters(
            roomUri.toString(),
            roomId,
            false,
            null
        )
        peerConnectionClient.createPeerConnectionFactory(
            applicationContext, peerConnectionParameters, this@CallActivity
        )
        startCall()
    }

    fun onCallHangUp() {
        disconnect()
    }

    fun onCameraSwitch() {
        if (peerConnectionClient != null) {
            peerConnectionClient.switchCamera()
        }
    }

    fun onToggleMic(): Boolean {
        if (peerConnectionClient != null) {
            micEnabled = !micEnabled
            peerConnectionClient.setAudioEnabled(micEnabled)
        }
        return micEnabled
    }

    private fun startCall() {
        if (appRtcClient == null) {
            Log.e(
                TAG,
                "AppRTC client is not allocated for a call."
            )
            return
        }
        callStartedTimeMs = System.currentTimeMillis()

        // Start room connection.
        logAndToast(getString(R.string.connecting_to, roomConnectionParameters.roomUrl))
        appRtcClient.connectToRoom(roomConnectionParameters)
    }

    @UiThread
    private fun callConnected() {
        val delta = System.currentTimeMillis() - callStartedTimeMs
        Log.i(TAG, "Call connected: delay=" + delta + "ms")
        if (peerConnectionClient == null || isError) {
            Log.w(
                TAG,
                "Call is connected in closed or error state"
            )
            return
        }
        // Enable statistics callback.
        peerConnectionClient.enableStatsEvents(true, STAT_CALLBACK_PERIOD)
        setSwappedFeeds(false /* isSwappedFeeds */)
    }

    // Disconnect from remote resources, dispose of local resources, and exit.
    private fun disconnect() {
        activityRunning = false
        remoteProxyRenderer.setTarget(null)
        localProxyVideoSink.setTarget(null)
        if (appRtcClient != null) {
            appRtcClient.disconnectFromRoom()
            appRtcClient = null
        }
        if (pipRenderer != null) {
            pipRenderer!!.release()
            pipRenderer = null
        }
        if (fullscreenRenderer != null) {
            fullscreenRenderer!!.release()
            fullscreenRenderer = null
        }
        if (peerConnectionClient != null) {
            peerConnectionClient.close()
            peerConnectionClient = null
        }
        if (iceConnected && !isError) {
            setResult(RESULT_OK)
        } else {
            setResult(RESULT_CANCELED)
        }
        finish()
    }

    private fun disconnectWithErrorMessage(errorMessage: String) {
        if (!activityRunning) {
            Log.e(TAG, "Critical error: $errorMessage")
            disconnect()
        } else {
            AlertDialog.Builder(this)
                .setTitle(getText(R.string.channel_error_title))
                .setMessage(errorMessage)
                .setCancelable(false)
                .setNeutralButton(R.string.ok,
                    DialogInterface.OnClickListener { dialog, id ->
                        dialog.cancel()
                        disconnect()
                    })
                .create()
                .show()
        }
    }

    // Log |msg| and Toast about it.
    private fun logAndToast(msg: String) {
        Log.d(TAG, msg)
        if (logToast != null) {
            logToast!!.cancel()
        }
        logToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT)
        logToast.show()
    }

    private fun reportError(description: String) {
        runOnUiThread {
            if (!isError) {
                isError = true
                disconnectWithErrorMessage(description)
            }
        }
    }

    // Create VideoCapturer
    private fun createVideoCapturer(): VideoCapturer? {
        val videoCapturer: VideoCapturer?
        Logging.d(TAG, "Creating capturer using camera2 API.")
        videoCapturer = createCameraCapturer(Camera2Enumerator(this))
        if (videoCapturer == null) {
            reportError("Failed to open camera")
            return null
        }
        return videoCapturer
    }

    // Create VideoCapturer from camera
    private fun createCameraCapturer(enumerator: CameraEnumerator): VideoCapturer? {
        val deviceNames = enumerator.deviceNames

        // First, try to find front facing camera
        Logging.d(TAG, "Looking for front facing cameras.")
        for (deviceName in deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                Logging.d(
                    TAG,
                    "Creating front facing camera capturer."
                )
                val videoCapturer: VideoCapturer? = enumerator.createCapturer(deviceName, null)
                if (videoCapturer != null) {
                    return videoCapturer
                }
            }
        }

        // Front facing camera not found, try something else
        Logging.d(TAG, "Looking for other cameras.")
        for (deviceName in deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                Logging.d(TAG, "Creating other camera capturer.")
                val videoCapturer: VideoCapturer? = enumerator.createCapturer(deviceName, null)
                if (videoCapturer != null) {
                    return videoCapturer
                }
            }
        }
        return null
    }

    private fun setSwappedFeeds(isSwappedFeeds: Boolean) {
        Logging.d(TAG, "setSwappedFeeds: $isSwappedFeeds")
        this.isSwappedFeeds = isSwappedFeeds
        localProxyVideoSink.setTarget(if (isSwappedFeeds) fullscreenRenderer else pipRenderer)
        remoteProxyRenderer.setTarget(if (isSwappedFeeds) pipRenderer else fullscreenRenderer)
        fullscreenRenderer!!.setMirror(isSwappedFeeds)
        pipRenderer!!.setMirror(!isSwappedFeeds)
    }

    // -----Implementation of AppRTCClient.AppRTCSignalingEvents ---------------
    // All callbacks are invoked from websocket signaling looper thread and
    // are routed to UI thread.
    private fun onConnectedToRoomInternal(params: AppRTCClient.SignalingParameters) {
        val delta = System.currentTimeMillis() - callStartedTimeMs
        signalingParameters = params
        logAndToast("Creating peer connection, delay=" + delta + "ms")
        var videoCapturer: VideoCapturer? = null
        if (peerConnectionParameters.videoCallEnabled) {
            videoCapturer = createVideoCapturer()
        }
        peerConnectionClient.createPeerConnection(
            localProxyVideoSink, remoteRenderers, videoCapturer, signalingParameters
        )
        if (signalingParameters.initiator) {
            logAndToast("Creating OFFER...")
            // Create offer. Offer SDP will be sent to answering client in
            // PeerConnectionEvents.onLocalDescription event.
            peerConnectionClient.createOffer()
        } else {
            if (params.offerSdp != null) {
                peerConnectionClient.setRemoteDescription(params.offerSdp)
                logAndToast("Creating ANSWER...")
                // Create answer. Answer SDP will be sent to offering client in
                // PeerConnectionEvents.onLocalDescription event.
                peerConnectionClient.createAnswer()
            }
            if (params.iceCandidates != null) {
                // Add remote ICE candidates from room.
                for (iceCandidate in params.iceCandidates) {
                    peerConnectionClient.addRemoteIceCandidate(iceCandidate)
                }
            }
        }
    }

    fun onConnectedToRoom(params: AppRTCClient.SignalingParameters) {
        runOnUiThread { onConnectedToRoomInternal(params) }
    }

    fun onRemoteDescription(sdp: SessionDescription) {
        val delta = System.currentTimeMillis() - callStartedTimeMs
        runOnUiThread(Runnable {
            if (peerConnectionClient == null) {
                Log.e(
                    TAG,
                    "Received remote SDP for non-initilized peer connection."
                )
                return@Runnable
            }
            logAndToast("Received remote " + sdp.type + ", delay=" + delta + "ms")
            peerConnectionClient.setRemoteDescription(sdp)
            if (!signalingParameters.initiator) {
                logAndToast("Creating ANSWER...")
                // Create answer. Answer SDP will be sent to offering client in
                // PeerConnectionEvents.onLocalDescription event.
                peerConnectionClient.createAnswer()
            }
        })
    }

    fun onRemoteIceCandidate(candidate: IceCandidate?) {
        runOnUiThread(Runnable {
            if (peerConnectionClient == null) {
                Log.e(
                    TAG,
                    "Received ICE candidate for a non-initialized peer connection."
                )
                return@Runnable
            }
            peerConnectionClient.addRemoteIceCandidate(candidate)
        })
    }

    fun onRemoteIceCandidatesRemoved(candidates: Array<IceCandidate?>?) {
        runOnUiThread(Runnable {
            if (peerConnectionClient == null) {
                Log.e(
                    TAG,
                    "Received ICE candidate removals for a non-initialized peer connection."
                )
                return@Runnable
            }
            peerConnectionClient.removeRemoteIceCandidates(candidates)
        })
    }

    fun onChannelClose() {
        runOnUiThread {
            logAndToast("Remote end hung up; dropping PeerConnection")
            disconnect()
        }
    }

    fun onChannelError(description: String) {
        reportError(description)
    }

    // -----Implementation of PeerConnectionClient.PeerConnectionEvents.---------
    // Send local peer connection SDP and ICE candidates to remote party.
    // All callbacks are invoked from peer connection client looper thread and
    // are routed to UI thread.
    fun onLocalDescription(sdp: SessionDescription) {
        val delta = System.currentTimeMillis() - callStartedTimeMs
        runOnUiThread {
            if (appRtcClient != null) {
                logAndToast("Sending " + sdp.type + ", delay=" + delta + "ms")
                if (signalingParameters.initiator) {
                    appRtcClient.sendOfferSdp(sdp)
                } else {
                    appRtcClient.sendAnswerSdp(sdp)
                }
            }
            if (peerConnectionParameters.videoMaxBitrate > 0) {
                Log.d(
                    TAG,
                    "Set video maximum bitrate: " + peerConnectionParameters.videoMaxBitrate
                )
                peerConnectionClient.setVideoMaxBitrate(peerConnectionParameters.videoMaxBitrate)
            }
        }
    }

    fun onIceCandidate(candidate: IceCandidate?) {
        runOnUiThread {
            if (appRtcClient != null) {
                appRtcClient.sendLocalIceCandidate(candidate)
            }
        }
    }

    fun onIceCandidatesRemoved(candidates: Array<IceCandidate?>?) {
        runOnUiThread {
            if (appRtcClient != null) {
                appRtcClient.sendLocalIceCandidateRemovals(candidates)
            }
        }
    }

    fun onIceConnected() {
        val delta = System.currentTimeMillis() - callStartedTimeMs
        runOnUiThread {
            logAndToast("ICE connected, delay=" + delta + "ms")
            iceConnected = true
            callConnected()
        }
    }

    fun onIceDisconnected() {
        runOnUiThread {
            logAndToast("ICE disconnected")
            iceConnected = false
            disconnect()
        }
    }

    fun onPeerConnectionClosed() {}
    fun onPeerConnectionStatsReady(reports: Array<StatsReport?>?) {}
    fun onPeerConnectionError(description: String) {
        reportError(description)
    }

    // Activity interfaces
    public override fun onStop() {
        super.onStop()
        activityRunning = false
        if (peerConnectionClient != null) {
            peerConnectionClient.stopVideoSource()
        }
    }

    public override fun onStart() {
        super.onStart()
        activityRunning = true
        // Video is not paused for screencapture. See onPause.
        if (peerConnectionClient != null) {
            peerConnectionClient.startVideoSource()
        }
    }

    override fun onDestroy() {
        Thread.setDefaultUncaughtExceptionHandler(null)
        disconnect()
        if (logToast != null) {
            logToast!!.cancel()
        }
        activityRunning = false
        super.onDestroy()
    }

    private class ProxyRenderer : Callbacks {
        private var target: Callbacks? = null

        @Synchronized
        fun renderFrame(frame: VideoRenderer.I420Frame?) {
            if (target == null) {
                Logging.d(
                    TAG,
                    "Dropping frame in proxy because target is null."
                )
                VideoRenderer.renderFrameDone(frame)
                return
            }
            target.renderFrame(frame)
        }

        @Synchronized
        fun setTarget(target: Callbacks?) {
            this.target = target
        }
    }

    private class ProxyVideoSink : VideoSink {
        private var target: VideoSink? = null

        @Synchronized
        override fun onFrame(frame: VideoFrame) {
            if (target == null) {
                Logging.d(
                    TAG,
                    "Dropping frame in proxy because target is null."
                )
                return
            }
            target!!.onFrame(frame)
        }

        @Synchronized
        fun setTarget(target: VideoSink?) {
            this.target = target
        }
    }

    companion object {
        private const val TAG = "CallActivity"
        private const val APPRTC_URL = "https://appr.tc"
        private const val UPPER_ALPHA_DIGITS = "ACEFGHJKLMNPQRUVWXY123456789"

        // Peer connection statistics callback period in ms.
        private const val STAT_CALLBACK_PERIOD = 1000
    }
}
