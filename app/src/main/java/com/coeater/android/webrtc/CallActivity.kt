package com.coeater.android.webrtc
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.PopupMenu
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.coeater.android.R
import com.coeater.android.api.provideHistoryApi
import com.coeater.android.apprtc.PeerConnectionClient
import com.coeater.android.apprtc.PeerConnectionClient.PeerConnectionEvents
import com.coeater.android.apprtc.PeerConnectionClient.PeerConnectionParameters
import com.coeater.android.apprtc.SignalServerRTCClient
import com.coeater.android.apprtc.SignalServerRTCClient.SignalingEvents
import com.coeater.android.apprtc.SignalServerRTCClient.SignalingParameters
import com.coeater.android.apprtc.WebSocketRTCClient
import com.coeater.android.history.HistoryViewModel
import com.coeater.android.history.HistoryViewModelFactory
import com.coeater.android.model.Profile
import com.coeater.android.apprtc.model.GameFinalResult
import com.coeater.android.apprtc.model.GameInfo
import com.coeater.android.apprtc.model.GameMatchResult
import com.coeater.android.model.RoomResponse
import com.coeater.android.webrtc.game.CallGameInputFromSocket
import com.coeater.android.webrtc.game.model.CallGameChoice
import com.coeater.android.webrtc.game.model.CallGameMatch
import com.coeater.android.webrtc.game.model.CallGameResult
import java.util.*
import kotlinx.android.synthetic.main.activity_call.*
import org.webrtc.*
import org.webrtc.RendererCommon.ScalingType

/**
 * Activity for peer connection call setup, call waiting
 * and call view.
 */
class CallActivity : AppCompatActivity(), SignalingEvents, PeerConnectionEvents {

    companion object {
        const val ROOM_CODE = "ROOM_CODE"
        const val IS_INVITER = "IS_INVITER"
        const val ROOM_RESPONSE = "ROOM_RESPONSE"
        private const val TAG = "CallActivity"
        private const val STAT_CALLBACK_PERIOD = 1000
    }

    private val localProxyVideoSink =
        ProxyVideoSink()
    private val remoteProxyVideoSink = ProxyVideoSink()
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
    private var disconnectButton: RelativeLayout? = null
    private var cameraSwitchButton: RelativeLayout? = null
//    private var toggleMuteButton: ImageButton? = null
    private var gameButton: RelativeLayout? = null

    /**
     * 소켓 결과를 통지한다.
     */
    private var callGameInputFromSocket: CallGameInputFromSocket? = null



    private val historyViewModelFactory by lazy {
        HistoryViewModelFactory(
            provideHistoryApi(this)
        )
    }
    private lateinit var historyViewModel: HistoryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)

        iceConnected = false
        signalingParameters = null

        // Create UI controls.
        pipRenderer = findViewById(R.id.pip_video_view)
        fullscreenRenderer = findViewById(R.id.fullscreen_video_view)
        disconnectButton = findViewById(R.id.button_exit)
        cameraSwitchButton = findViewById(R.id.button_change)
//        toggleMuteButton = findViewById(R.id.button_call_toggle_mic)
        gameButton = findViewById(R.id.button_games)

        // Add buttons click events.
        disconnectButton?.setOnClickListener(View.OnClickListener { onCallHangUp() })
        cameraSwitchButton?.setOnClickListener(View.OnClickListener { onCameraSwitch() })
//        toggleMuteButton?.setOnClickListener(View.OnClickListener {
//            val enabled = onToggleMic()
//            toggleMuteButton?.setAlpha(if (enabled) 1.0f else 0.3f)
//        })
        gameButton?.setOnClickListener{
            val popupMenu: PopupMenu = PopupMenu(this, gameButton)
            popupMenu.menuInflater.inflate(R.menu.menu_call_games,popupMenu.menu)
            popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                when(item.itemId) {
                    R.id.menu_game_likeness -> startGameLikeness()
                    R.id.menu_game_subtitles ->
                        Toast.makeText(this@CallActivity, "You Clicked : " + item.title, Toast.LENGTH_SHORT).show()
                }
                true
            })
            popupMenu.show()
        }

        // Swap feeds on pip view click.
        pipRenderer?.setOnClickListener(View.OnClickListener { setSwappedFeeds(!isSwappedFeeds) })

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
        setSwappedFeeds(false /* isSwappedFeeds */)

        val room_code = intent.extras?.getString(ROOM_CODE) ?: ""
        connectVideoCall(room_code)
        setupOtherInfo()
    }

    /**
     * 통화하는 상대방의 정보를 보여 준다.
     */
    private fun setupOtherInfo() {
        val is_inviter = intent.extras?.getBoolean(IS_INVITER) ?: false
        val room_response = intent.extras?.getParcelable<RoomResponse>(ROOM_RESPONSE) ?: return
        historyViewModel = ViewModelProviders.of(
            this, historyViewModelFactory)[HistoryViewModel::class.java]

        if (is_inviter) {
            tv_name.text = room_response.target?.nickname ?: ""
            Glide.with(this)
                .load(Profile.getUrl(room_response.target?.profile))
                .error(R.drawable.ic_dummy_circle_crop)
                .apply(RequestOptions.circleCropTransform())
                .into(iv_profile)
                .clearOnDetach()
            historyViewModel.saveHistory(room_response.target?.id)
        } else {
            tv_name.text = room_response.owner.nickname
            Glide.with(this)
                .load(Profile.getUrl(room_response.owner.profile))
                .error(R.drawable.ic_dummy_circle_crop)
                .apply(RequestOptions.circleCropTransform())
                .into(iv_profile)
                .clearOnDetach()
            historyViewModel.saveHistory(room_response.owner.id)
        }

    }

    // Join video call with randomly generated roomId
    private fun connectVideoCall(roomId: String) {
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
        val client = WebSocketRTCClient(this)
        signalServerRtcClient = client
        // Create connection parameters.
        peerConnectionClient?.createPeerConnectionFactory(
            applicationContext, peerConnectionParameters, this@CallActivity
        )
        startCall(roomId)
        setupGame(client)
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
        fullscreenRenderer?.visibility = View.VISIBLE
    }

    // Disconnect from remote resources, dispose of local resources, and exit.
    private fun disconnect() {
        activityRunning = false
        localProxyVideoSink.setTarget(null)
        remoteProxyVideoSink.setTarget(null)
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
//        if (logToast != null) {
//            logToast!!.cancel()
//        }
//        logToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT)
//        logToast?.show()
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
        remoteProxyVideoSink.setTarget(if (isSwappedFeeds) pipRenderer else fullscreenRenderer)
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
            localProxyVideoSink!!, remoteProxyVideoSink!!, videoCapturer, signalingParameters!!
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

    /************* Game Contents *************/
    private var dataChannel: DataChannel? = null

    private fun startGameLikeness(){
        signalServerRtcClient?.startGameLikeness()
    }


    override fun onPlayGameLikeness(gameInfo: GameInfo) {
        val gameChoice = CallGameChoice(gameInfo.imageLeft, gameInfo.imageRight, gameInfo.itemLeft, gameInfo.itemRight, gameInfo.stage, gameInfo.totalStage)
        callGameInputFromSocket?.showChoice(gameChoice)
    }

    override fun onPlayGameMatchResult(matchResult: GameMatchResult) {
        val gameChoice = CallGameChoice(matchResult.nextInfo.imageLeft, matchResult.nextInfo.imageRight, matchResult.nextInfo.itemLeft, matchResult.nextInfo.itemRight, matchResult.nextInfo.stage, matchResult.nextInfo.totalStage)
        val gameMatch = CallGameMatch(matchResult.isMatched, gameChoice)
        callGameInputFromSocket?.showMatch(gameMatch)
    }

    override fun onPlayGameMatchEnd(matchEnd: GameFinalResult) {
        val gameResult = CallGameResult(matchEnd.isMatched, matchEnd.similarity)
        callGameInputFromSocket?.showResult(gameResult)
    }

    private fun setupGame(client: WebSocketRTCClient) {
        val input = call_game_view.configure(this, client)
        callGameInputFromSocket = input
    }
}
