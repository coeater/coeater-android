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
import androidx.annotation.UiThread
import com.coeater.android.R
import com.coeater.android.apprtc.PeerConnectionClient
import com.coeater.android.apprtc.PeerConnectionClient.PeerConnectionEvents
import com.coeater.android.apprtc.PeerConnectionClient.PeerConnectionParameters
import com.coeater.android.apprtc.SignalServerRTCClient
import com.coeater.android.apprtc.SignalServerRTCClient.SignalingEvents
import com.coeater.android.apprtc.SignalServerRTCClient.SignalingParameters
import com.coeater.android.apprtc.WebSocketRTCClient
import org.webrtc.*
import org.webrtc.RendererCommon.ScalingType
import org.webrtc.VideoRenderer.I420Frame
import java.util.*

/**
 * Activity for peer connection call setup, call waiting
 * and call view.
 */
class CallActivity : Activity(), SignalingEvents, PeerConnectionEvents {
    private val remoteProxyRenderer =
        ProxyRenderer()
    private val localProxyVideoSink =
        ProxyVideoSink()
    private val remoteRenderers: MutableList<VideoRenderer.Callbacks> =
        ArrayList()
    private var peerConnectionClient: PeerConnectionClient? = null
    private var signalServerRtcClient: SignalServerRTCClient? = null
    private var signalingParameters: SignalingParameters? = null
    private var pipRenderer: SurfaceViewRenderer? = null
    private var fullscreenRenderer: SurfaceViewRenderer? = null
    private var logToast: Toast? = null
    private var activityRunning = false
    private var peerConnectionParameters: PeerConnectionParameters? = null
    private var iceConnected = false
    private var isError = false
    private var callStartedTimeMs: Long = 0
    private var micEnabled = true
    private var isSwappedFeeds = false

    // Control buttons for limited UI
    private var disconnectButton: ImageButton? = null
    private var cameraSwitchButton: ImageButton? = null
    private var toggleMuteButton: ImageButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)
        val intent = intent

        val url =
            intent.extras.getString("url") // intent.getStringExtra("name") 라고해도됨

        iceConnected = false
        signalingParameters = null

        // Create UI controls.
        pipRenderer = findViewById(R.id.pip_video_view)
        fullscreenRenderer = findViewById(R.id.fullscreen_video_view)
        disconnectButton = findViewById(R.id.button_call_disconnect)
        cameraSwitchButton = findViewById(R.id.button_call_switch_camera)
        toggleMuteButton = findViewById(R.id.button_call_toggle_mic)

        // Add buttons click events.
        disconnectButton?.setOnClickListener(View.OnClickListener { onCallHangUp() })
        cameraSwitchButton?.setOnClickListener(View.OnClickListener { onCameraSwitch() })
        toggleMuteButton?.setOnClickListener(View.OnClickListener {
            val enabled = onToggleMic()
            toggleMuteButton?.setAlpha(if (enabled) 1.0f else 0.3f)
        })

        // Swap feeds on pip view click.
        pipRenderer?.setOnClickListener(View.OnClickListener { setSwappedFeeds(!isSwappedFeeds) })
        remoteRenderers.add(remoteProxyRenderer)

        // Create peer connection client.
        peerConnectionClient = PeerConnectionClient()

        // Create video renderers.
        pipRenderer?.init(peerConnectionClient?.getRenderContext(), null)
        pipRenderer?.setScalingType(ScalingType.SCALE_ASPECT_FIT)
        fullscreenRenderer?.init(peerConnectionClient?.getRenderContext(), null)
        fullscreenRenderer?.setScalingType(ScalingType.SCALE_ASPECT_FILL)
        pipRenderer?.setZOrderMediaOverlay(true)
        pipRenderer?.setEnableHardwareScaler(true /* enabled */)
        fullscreenRenderer?.setEnableHardwareScaler(true /* enabled */)
        // Start with local feed in fullscreen and swap it to the pip when the call is connected.
        setSwappedFeeds(true /* isSwappedFeeds */)

        // Generate a random room ID with 7 uppercase letters and digits
        val randomRoomID = url
        // Show the random room ID so that another client can join from https://appr.tc
        val roomIdTextView = findViewById<TextView>(R.id.roomID)
        roomIdTextView.text = getString(R.string.room_id_caption).toString() + randomRoomID
        Log.d(
            TAG,
            getString(R.string.room_id_caption).toString() + randomRoomID
        )

        // Connect video call to the random room
        connectVideoCall(randomRoomID)
    }

    // Join video call with randomly generated roomId
    private fun connectVideoCall(roomId: String) {
        val roomUri = Uri.parse(APPRTC_URL)
        val videoWidth = 0
        val videoHeight = 0
        peerConnectionParameters = PeerConnectionParameters(
            true,
            false,
            false,
            videoWidth,
            videoHeight,
            0, getString(R.string.pref_maxvideobitratevalue_default).toInt(),
            getString(R.string.pref_videocodec_default),
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
        signalServerRtcClient = WebSocketRTCClient(this)
        // Create connection parameters.
        peerConnectionClient?.createPeerConnectionFactory(
            applicationContext, peerConnectionParameters, this@CallActivity
        )
        startCall(roomId)
    }

    fun onCallHangUp() {
        disconnect()
    }

    fun onCameraSwitch() {
        if (peerConnectionClient != null) {
            peerConnectionClient?.switchCamera()
        }
    }

    fun onToggleMic(): Boolean {
        if (peerConnectionClient != null) {
            micEnabled = !micEnabled
            peerConnectionClient?.setAudioEnabled(micEnabled)
        }
        return micEnabled
    }

    private fun startCall(roomId: String) {
        if (signalServerRtcClient == null) {
            Log.e(
                TAG,
                "AppRTC client is not allocated for a call."
            )
            return
        }
        callStartedTimeMs = System.currentTimeMillis()

        // Start room connection.
        signalServerRtcClient?.connectToRoom(roomId)
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
        peerConnectionClient?.enableStatsEvents(true, STAT_CALLBACK_PERIOD)
        setSwappedFeeds(false /* isSwappedFeeds */)
    }

    // Disconnect from remote resources, dispose of local resources, and exit.
    private fun disconnect() {
        activityRunning = false
        remoteProxyRenderer.setTarget(null)
        localProxyVideoSink.setTarget(null)
        if (signalServerRtcClient != null) {
            signalServerRtcClient?.disconnectFromRoom()
            signalServerRtcClient = null
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
            peerConnectionClient?.close()
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
        logToast?.show()
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
    private fun onConnectedToRoomInternal(params: SignalingParameters) {
        signalingParameters = params
        var videoCapturer: VideoCapturer? = null
        if (peerConnectionParameters?.videoCallEnabled == true) {
            videoCapturer = createVideoCapturer()
        }
        peerConnectionClient?.createPeerConnection(
            localProxyVideoSink!!, remoteRenderers!!, videoCapturer, signalingParameters!!
        )
        if (signalingParameters?.initiator == true) {
            logAndToast("Creating OFFER...")
            // Create offer. Offer SDP will be sent to answering client in
            // PeerConnectionEvents.onLocalDescription event.
            peerConnectionClient?.createOffer()
        } else {
            logAndToast("Creating ANSWER...")
        }
    }

    override fun onConnectedToRoom(params: SignalingParameters) {
        runOnUiThread { onConnectedToRoomInternal(params) }
    }

    override fun onRemoteDescription(sdp: SessionDescription) {
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
            peerConnectionClient?.setRemoteDescription(sdp)
            if (signalingParameters?.initiator == false) {
                logAndToast("Creating ANSWER...")
                // Create answer. Answer SDP will be sent to offering client in
                // PeerConnectionEvents.onLocalDescription event.
                peerConnectionClient?.createAnswer()
            }
        })
    }

    override fun onRemoteIceCandidate(candidate: IceCandidate) {
        runOnUiThread(Runnable {
            if (peerConnectionClient == null) {
                Log.e(
                    TAG,
                    "Received ICE candidate for a non-initialized peer connection."
                )
                return@Runnable
            }
            peerConnectionClient?.addRemoteIceCandidate(candidate)
        })
    }

    override fun onRemoteIceCandidatesRemoved(candidates: Array<IceCandidate>) {
        runOnUiThread(Runnable {
            if (peerConnectionClient == null) {
                Log.e(
                    TAG,
                    "Received ICE candidate removals for a non-initialized peer connection."
                )
                return@Runnable
            }
            peerConnectionClient?.removeRemoteIceCandidates(candidates)
        })
    }

    override fun onChannelClose() {
        runOnUiThread {
            logAndToast("Remote end hung up; dropping PeerConnection")
            disconnect()
        }
    }

    // -----Implementation of PeerConnectionClient.PeerConnectionEvents.---------
    // Send local peer connection SDP and ICE candidates to remote party.
    // All callbacks are invoked from peer connection client looper thread and
    // are routed to UI thread.
   override fun onLocalDescription(sdp: SessionDescription) {
        val delta = System.currentTimeMillis() - callStartedTimeMs
        runOnUiThread {
            if (signalServerRtcClient != null) {
                logAndToast("Sending " + sdp.type + ", delay=" + delta + "ms")
                if (signalingParameters?.initiator == true) {
                    signalServerRtcClient?.sendOfferSdp(sdp)
                } else {
                    signalServerRtcClient?.sendAnswerSdp(sdp)
                }
            }
            if (peerConnectionParameters?.videoMaxBitrate ?: 0 > 0) {
                Log.d(
                    TAG,
                    "Set video maximum bitrate: " + peerConnectionParameters?.videoMaxBitrate
                )
                peerConnectionClient?.setVideoMaxBitrate(peerConnectionParameters?.videoMaxBitrate)
            }
        }
    }

   override fun onIceCandidate(candidate: IceCandidate) {
        runOnUiThread {
            if (signalServerRtcClient != null) {
                signalServerRtcClient?.sendLocalIceCandidate(candidate)
            }
        }
    }

   override fun onIceConnected() {
        val delta = System.currentTimeMillis() - callStartedTimeMs
        runOnUiThread {
            logAndToast("ICE connected, delay=" + delta + "ms")
            iceConnected = true
            callConnected()
        }
    }

   override fun onIceDisconnected() {
        runOnUiThread {
            logAndToast("ICE disconnected")
            iceConnected = false
            disconnect()
        }
    }

   override fun onPeerConnectionClosed() {}
   override fun onPeerConnectionStatsReady(reports: Array<StatsReport?>?) {}
   override fun onPeerConnectionError(description: String) {
        reportError(description)
    }

    // Activity interfaces
    public override fun onStop() {
        super.onStop()
        activityRunning = false
        if (peerConnectionClient != null) {
            peerConnectionClient?.stopVideoSource()
        }
    }

    public override fun onStart() {
        super.onStart()
        activityRunning = true
        // Video is not paused for screencapture. See onPause.
        if (peerConnectionClient != null) {
            peerConnectionClient?.startVideoSource()
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

    private class ProxyRenderer : VideoRenderer.Callbacks {
        private var target: VideoRenderer.Callbacks? = null

        @Synchronized
        override fun renderFrame(frame: I420Frame) {
            if (target == null) {
                Logging.d(
                    TAG,
                    "Dropping frame in proxy because target is null."
                )
                VideoRenderer.renderFrameDone(frame)
                return
            }
            target!!.renderFrame(frame)
        }

        @Synchronized
        fun setTarget(target: VideoRenderer.Callbacks?) {
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
