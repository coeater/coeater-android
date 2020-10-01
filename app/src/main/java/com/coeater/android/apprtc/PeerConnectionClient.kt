package com.coeater.android.apprtc

import android.content.Context
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.util.Log
import org.webrtc.*
import org.webrtc.PeerConnection.*
import org.webrtc.PeerConnection.Observer
import org.webrtc.voiceengine.WebRtcAudioManager
import org.webrtc.voiceengine.WebRtcAudioRecord
import org.webrtc.voiceengine.WebRtcAudioRecord.WebRtcAudioRecordErrorCallback
import org.webrtc.voiceengine.WebRtcAudioTrack
import org.webrtc.voiceengine.WebRtcAudioUtils
import java.io.File
import java.io.IOException
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.Executors
import java.util.regex.Pattern
/**
 * Peer connection client implementation.
 *
 *
 * All public methods are routed to local looper thread.
 * All PeerConnectionEvents callbacks are invoked from the same looper thread.
 * This class is a singleton.
 */
class PeerConnectionClient {
    private val pcObserver: PeerConnectionClient.PCObserver =
        PCObserver()
    private val sdpObserver: PeerConnectionClient.SDPObserver =
        SDPObserver()
    private val rootEglBase: EglBase
    private var factory: PeerConnectionFactory? = null
    private var peerConnection: PeerConnection? = null
    var options: PeerConnectionFactory.Options? = null
    private var audioSource: AudioSource? = null
    private var videoSource: VideoSource? = null
    var isVideoCallEnabled = false
        private set
    private var preferIsac = false
    private var preferredVideoCodec: String? = null
    private var videoCapturerStopped = false
    private var isError = false
    private var statsTimer: Timer? = null
    private var localRender: VideoSink? = null
    private var remoteRenders: List<VideoRenderer.Callbacks>? = null
    private var signalingParameters: AppRTCClient.SignalingParameters? = null
    private var videoWidth = 0
    private var videoHeight = 0
    private var videoFps = 0
    private var audioConstraints: MediaConstraints? = null
    private var sdpMediaConstraints: MediaConstraints? = null
    private var peerConnectionParameters: PeerConnectionClient.PeerConnectionParameters? =
        null

    // Queued remote ICE candidates are consumed only after both local and
    // remote descriptions are set. Similarly local ICE candidates are sent to
    // remote peer after both local and remote description are set.
    private var queuedRemoteCandidates: MutableList<IceCandidate>? = null
    private var events: PeerConnectionClient.PeerConnectionEvents? = null
    private var isInitiator = false
    private var localSdp // either offer or answer SDP
            : SessionDescription? = null
    private var mediaStream: MediaStream? = null
    private var videoCapturer: VideoCapturer? = null

    // enableVideo is set to true if video should be rendered and sent.
    private var renderVideo = false
    private var localVideoTrack: VideoTrack? = null
    private var remoteVideoTrack: VideoTrack? = null
    private var localVideoSender: RtpSender? = null

    // enableAudio is set to true if audio should be sent.
    private var enableAudio = false
    private var localAudioTrack: AudioTrack? = null
    private var dataChannel: DataChannel? = null
    private var dataChannelEnabled = false

    /**
     * Peer connection parameters.
     */
    class DataChannelParameters(
        val ordered: Boolean, val maxRetransmitTimeMs: Int, val maxRetransmits: Int,
        val protocol: String, val negotiated: Boolean, val id: Int
    )

    /**
     * Peer connection parameters.
     */
    public class PeerConnectionParameters @JvmOverloads constructor(
        val videoCallEnabled: Boolean,
        val loopback: Boolean,
        val tracing: Boolean,
        val videoWidth: Int,
        val videoHeight: Int,
        val videoFps: Int,
        val videoMaxBitrate: Int,
        val videoCodec: String,
        val videoCodecHwAcceleration: Boolean,
        val videoFlexfecEnabled: Boolean,
        val audioStartBitrate: Int,
        val audioCodec: String,
        val noAudioProcessing: Boolean,
        val aecDump: Boolean,
        val useOpenSLES: Boolean,
        val disableBuiltInAEC: Boolean,
        val disableBuiltInAGC: Boolean,
        val disableBuiltInNS: Boolean,
        val enableLevelControl: Boolean,
        val disableWebRtcAGCAndHPF: Boolean,
        private val dataChannelParameters: PeerConnectionClient.DataChannelParameters? = null
    )

    /**
     * Peer connection events.
     */
    interface PeerConnectionEvents {
        /**
         * Callback fired once local SDP is created and set.
         */
        fun onLocalDescription(sdp: SessionDescription?)

        /**
         * Callback fired once local Ice candidate is generated.
         */
        fun onIceCandidate(candidate: IceCandidate?)

        /**
         * Callback fired once local ICE candidates are removed.
         */
        fun onIceCandidatesRemoved(candidates: Array<IceCandidate?>?)

        /**
         * Callback fired once connection is established (IceConnectionState is
         * CONNECTED).
         */
        fun onIceConnected()

        /**
         * Callback fired once connection is closed (IceConnectionState is
         * DISCONNECTED).
         */
        fun onIceDisconnected()

        /**
         * Callback fired once peer connection is closed.
         */
        fun onPeerConnectionClosed()

        /**
         * Callback fired once peer connection statistics is ready.
         */
        fun onPeerConnectionStatsReady(reports: Array<StatsReport?>?)

        /**
         * Callback fired once peer connection error happened.
         */
        fun onPeerConnectionError(description: String?)
    }

    fun setPeerConnectionFactoryOptions(options: PeerConnectionFactory.Options?) {
        this.options = options
    }

    fun createPeerConnectionFactory(
        context: Context,
        peerConnectionParameters: PeerConnectionClient.PeerConnectionParameters,
        events: PeerConnectionClient.PeerConnectionEvents?
    ) {
        this.peerConnectionParameters = peerConnectionParameters
        this.events = events
        isVideoCallEnabled = peerConnectionParameters.videoCallEnabled
        dataChannelEnabled = dataChannelParameters != null
        // Reset variables to initial states.
        factory = null
        peerConnection = null
        preferIsac = false
        videoCapturerStopped = false
        isError = false
        queuedRemoteCandidates = null
        localSdp = null // either offer or answer SDP
        mediaStream = null
        videoCapturer = null
        renderVideo = true
        localVideoTrack = null
        remoteVideoTrack = null
        localVideoSender = null
        enableAudio = true
        localAudioTrack = null
        statsTimer = Timer()
        PeerConnectionClient.Companion.executor.execute(Runnable {
            createPeerConnectionFactoryInternal(
                context
            )
        })
    }

    fun createPeerConnection(
        localRender: VideoSink?,
        remoteRender: VideoRenderer.Callbacks?, videoCapturer: VideoCapturer?,
        signalingParameters: AppRTCClient.SignalingParameters?
    ) {
        createPeerConnection(
            localRender, listOf(remoteRender), videoCapturer, signalingParameters
        )
    }

    fun createPeerConnection(
        localRender: VideoSink?,
        remoteRenders: List<VideoRenderer.Callbacks>?,
        videoCapturer: VideoCapturer?,
        signalingParameters: SignalingParameters?
    ) {
        if (peerConnectionParameters == null) {
            Log.e(
                PeerConnectionClient.Companion.TAG,
                "Creating peer connection without initializing factory."
            )
            return
        }
        this.localRender = localRender
        this.remoteRenders = remoteRenders
        this.videoCapturer = videoCapturer
        this.signalingParameters = signalingParameters
        PeerConnectionClient.Companion.executor.execute(Runnable {
            try {
                createMediaConstraintsInternal()
                createPeerConnectionInternal()
            } catch (e: Exception) {
                reportError("Failed to create peer connection: " + e.message)
                throw e
            }
        })
    }

    fun close() {
        PeerConnectionClient.Companion.executor.execute(Runnable { closeInternal() })
    }

    private fun createPeerConnectionFactoryInternal(context: Context) {
        isError = false

        // Initialize field trials.
        var fieldTrials = ""
        if (peerConnectionParameters!!.videoFlexfecEnabled) {
            fieldTrials += PeerConnectionClient.Companion.VIDEO_FLEXFEC_FIELDTRIAL
            Log.d(
                PeerConnectionClient.Companion.TAG,
                "Enable FlexFEC field trial."
            )
        }
        fieldTrials += PeerConnectionClient.Companion.VIDEO_VP8_INTEL_HW_ENCODER_FIELDTRIAL
        if (peerConnectionParameters!!.disableWebRtcAGCAndHPF) {
            fieldTrials += PeerConnectionClient.Companion.DISABLE_WEBRTC_AGC_FIELDTRIAL
            Log.d(
                PeerConnectionClient.Companion.TAG,
                "Disable WebRTC AGC field trial."
            )
        }
        fieldTrials += PeerConnectionClient.Companion.VIDEO_FRAME_EMIT_FIELDTRIAL

        // Check preferred video codec.
        preferredVideoCodec =
            PeerConnectionClient.Companion.VIDEO_CODEC_VP8
        if (isVideoCallEnabled && peerConnectionParameters!!.videoCodec != null) {
            when (peerConnectionParameters!!.videoCodec) {
                PeerConnectionClient.Companion.VIDEO_CODEC_VP8 -> preferredVideoCodec =
                    PeerConnectionClient.Companion.VIDEO_CODEC_VP8
                PeerConnectionClient.Companion.VIDEO_CODEC_VP9 -> preferredVideoCodec =
                    PeerConnectionClient.Companion.VIDEO_CODEC_VP9
                PeerConnectionClient.Companion.VIDEO_CODEC_H264_BASELINE -> preferredVideoCodec =
                    PeerConnectionClient.Companion.VIDEO_CODEC_H264
                PeerConnectionClient.Companion.VIDEO_CODEC_H264_HIGH -> {
                    // TODO(magjed): Strip High from SDP when selecting Baseline instead of using field trial.
                    fieldTrials += PeerConnectionClient.Companion.VIDEO_H264_HIGH_PROFILE_FIELDTRIAL
                    preferredVideoCodec =
                        PeerConnectionClient.Companion.VIDEO_CODEC_H264
                }
                else -> preferredVideoCodec =
                    PeerConnectionClient.Companion.VIDEO_CODEC_VP8
            }
        }
        Log.d(
            PeerConnectionClient.Companion.TAG,
            "Preferred video codec: $preferredVideoCodec"
        )

        // Initialize WebRTC
        Log.d(
            PeerConnectionClient.Companion.TAG,
            "Initialize WebRTC. Field trials: " + fieldTrials + " Enable video HW acceleration: "
                    + peerConnectionParameters!!.videoCodecHwAcceleration
        )
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(context)
                .setFieldTrials(fieldTrials)
                .setEnableVideoHwAcceleration(peerConnectionParameters!!.videoCodecHwAcceleration)
                .setEnableInternalTracer(true)
                .createInitializationOptions()
        )
        if (peerConnectionParameters!!.tracing) {
            PeerConnectionFactory.startInternalTracingCapture(
                Environment.getExternalStorageDirectory()
                    .absolutePath + File.separator
                        + "webrtc-trace.txt"
            )
        }

        // Check if ISAC is used by default.
        preferIsac = (peerConnectionParameters!!.audioCodec != null
                && peerConnectionParameters!!.audioCodec == PeerConnectionClient.Companion.AUDIO_CODEC_ISAC)

        // Enable/disable OpenSL ES playback.
        if (!peerConnectionParameters!!.useOpenSLES) {
            Log.d(
                PeerConnectionClient.Companion.TAG,
                "Disable OpenSL ES audio even if device supports it"
            )
            WebRtcAudioManager.setBlacklistDeviceForOpenSLESUsage(true /* enable */)
        } else {
            Log.d(
                PeerConnectionClient.Companion.TAG,
                "Allow OpenSL ES audio if device supports it"
            )
            WebRtcAudioManager.setBlacklistDeviceForOpenSLESUsage(false)
        }
        if (peerConnectionParameters!!.disableBuiltInAEC) {
            Log.d(
                PeerConnectionClient.Companion.TAG,
                "Disable built-in AEC even if device supports it"
            )
            WebRtcAudioUtils.setWebRtcBasedAcousticEchoCanceler(true)
        } else {
            Log.d(
                PeerConnectionClient.Companion.TAG,
                "Enable built-in AEC if device supports it"
            )
            WebRtcAudioUtils.setWebRtcBasedAcousticEchoCanceler(false)
        }
        if (peerConnectionParameters!!.disableBuiltInAGC) {
            Log.d(
                PeerConnectionClient.Companion.TAG,
                "Disable built-in AGC even if device supports it"
            )
            WebRtcAudioUtils.setWebRtcBasedAutomaticGainControl(true)
        } else {
            Log.d(
                PeerConnectionClient.Companion.TAG,
                "Enable built-in AGC if device supports it"
            )
            WebRtcAudioUtils.setWebRtcBasedAutomaticGainControl(false)
        }
        if (peerConnectionParameters!!.disableBuiltInNS) {
            Log.d(
                PeerConnectionClient.Companion.TAG,
                "Disable built-in NS even if device supports it"
            )
            WebRtcAudioUtils.setWebRtcBasedNoiseSuppressor(true)
        } else {
            Log.d(
                PeerConnectionClient.Companion.TAG,
                "Enable built-in NS if device supports it"
            )
            WebRtcAudioUtils.setWebRtcBasedNoiseSuppressor(false)
        }

        // Set audio record error callbacks.
        WebRtcAudioRecord.setErrorCallback(object :
            WebRtcAudioRecordErrorCallback {
            override fun onWebRtcAudioRecordInitError(errorMessage: String) {
                Log.e(
                    PeerConnectionClient.Companion.TAG,
                    "onWebRtcAudioRecordInitError: $errorMessage"
                )
                reportError(errorMessage)
            }

            override fun onWebRtcAudioRecordStartError(
                errorCode: WebRtcAudioRecord.AudioRecordStartErrorCode,
                errorMessage: String
            ) {
                Log.e(
                    PeerConnectionClient.Companion.TAG,
                    "onWebRtcAudioRecordStartError: $errorCode. $errorMessage"
                )
                reportError(errorMessage)
            }

            override fun onWebRtcAudioRecordError(errorMessage: String) {
                Log.e(
                    PeerConnectionClient.Companion.TAG,
                    "onWebRtcAudioRecordError: $errorMessage"
                )
                reportError(errorMessage)
            }
        })
        WebRtcAudioTrack.setErrorCallback(object :
            WebRtcAudioTrack.ErrorCallback {
            override fun onWebRtcAudioTrackInitError(errorMessage: String) {
                Log.e(
                    PeerConnectionClient.Companion.TAG,
                    "onWebRtcAudioTrackInitError: $errorMessage"
                )
                reportError(errorMessage)
            }

            override fun onWebRtcAudioTrackStartError(
                errorCode: WebRtcAudioTrack.AudioTrackStartErrorCode,
                errorMessage: String
            ) {
                Log.e(
                    PeerConnectionClient.Companion.TAG,
                    "onWebRtcAudioTrackStartError: $errorCode. $errorMessage"
                )
                reportError(errorMessage)
            }

            override fun onWebRtcAudioTrackError(errorMessage: String) {
                Log.e(
                    PeerConnectionClient.Companion.TAG,
                    "onWebRtcAudioTrackError: $errorMessage"
                )
                reportError(errorMessage)
            }
        })

        // Create peer connection factory.
        if (options != null) {
            Log.d(
                PeerConnectionClient.Companion.TAG,
                "Factory networkIgnoreMask option: " + options!!.networkIgnoreMask
            )
        }
        val enableH264HighProfile =
            PeerConnectionClient.Companion.VIDEO_CODEC_H264_HIGH == peerConnectionParameters!!.videoCodec
        val encoderFactory: VideoEncoderFactory
        val decoderFactory: VideoDecoderFactory
        if (peerConnectionParameters!!.videoCodecHwAcceleration) {
            encoderFactory = DefaultVideoEncoderFactory(
                rootEglBase.eglBaseContext,
                true /* enableIntelVp8Encoder */,
                enableH264HighProfile
            )
            decoderFactory = DefaultVideoDecoderFactory(rootEglBase.eglBaseContext)
        } else {
            encoderFactory = SoftwareVideoEncoderFactory()
            decoderFactory = SoftwareVideoDecoderFactory()
        }
        factory = PeerConnectionFactory(options, encoderFactory, decoderFactory)
        Log.d(
            PeerConnectionClient.Companion.TAG,
            "Peer connection factory created."
        )
    }

    private fun createMediaConstraintsInternal() {
        // Check if there is a camera on device and disable video call if not.
        if (videoCapturer == null) {
            Log.w(
                PeerConnectionClient.Companion.TAG,
                "No camera on device. Switch to audio only call."
            )
            isVideoCallEnabled = false
        }
        // Create video constraints if video call is enabled.
        if (isVideoCallEnabled) {
            videoWidth = peerConnectionParameters!!.videoWidth
            videoHeight = peerConnectionParameters!!.videoHeight
            videoFps = peerConnectionParameters!!.videoFps

            // If video resolution is not specified, default to HD.
            if (videoWidth == 0 || videoHeight == 0) {
                videoWidth =
                    PeerConnectionClient.Companion.HD_VIDEO_WIDTH
                videoHeight =
                    PeerConnectionClient.Companion.HD_VIDEO_HEIGHT
            }

            // If fps is not specified, default to 30.
            if (videoFps == 0) {
                videoFps = 30
            }
            Logging.d(
                PeerConnectionClient.Companion.TAG,
                "Capturing format: " + videoWidth + "x" + videoHeight + "@" + videoFps
            )
        }

        // Create audio constraints.
        audioConstraints = MediaConstraints()
        // added for audio performance measurements
        if (peerConnectionParameters!!.noAudioProcessing) {
            Log.d(
                PeerConnectionClient.Companion.TAG,
                "Disabling audio processing"
            )
            audioConstraints!!.mandatory.add(
                MediaConstraints.KeyValuePair(
                    PeerConnectionClient.Companion.AUDIO_ECHO_CANCELLATION_CONSTRAINT,
                    "false"
                )
            )
            audioConstraints!!.mandatory.add(
                MediaConstraints.KeyValuePair(
                    PeerConnectionClient.Companion.AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT,
                    "false"
                )
            )
            audioConstraints!!.mandatory.add(
                MediaConstraints.KeyValuePair(
                    PeerConnectionClient.Companion.AUDIO_HIGH_PASS_FILTER_CONSTRAINT,
                    "false"
                )
            )
            audioConstraints!!.mandatory.add(
                MediaConstraints.KeyValuePair(
                    PeerConnectionClient.Companion.AUDIO_NOISE_SUPPRESSION_CONSTRAINT,
                    "false"
                )
            )
        }
        if (peerConnectionParameters!!.enableLevelControl) {
            Log.d(
                PeerConnectionClient.Companion.TAG,
                "Enabling level control."
            )
            audioConstraints!!.mandatory.add(
                MediaConstraints.KeyValuePair(
                    PeerConnectionClient.Companion.AUDIO_LEVEL_CONTROL_CONSTRAINT,
                    "true"
                )
            )
        }
        // Create SDP constraints.
        sdpMediaConstraints = MediaConstraints()
        sdpMediaConstraints!!.mandatory.add(
            MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true")
        )
        if (isVideoCallEnabled || peerConnectionParameters!!.loopback) {
            sdpMediaConstraints!!.mandatory.add(
                MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true")
            )
        } else {
            sdpMediaConstraints!!.mandatory.add(
                MediaConstraints.KeyValuePair("OfferToReceiveVideo", "false")
            )
        }
    }

    private fun createPeerConnectionInternal() {
        if (factory == null || isError) {
            Log.e(
                PeerConnectionClient.Companion.TAG,
                "Peerconnection factory is not created"
            )
            return
        }
        Log.d(
            PeerConnectionClient.Companion.TAG,
            "Create peer connection."
        )
        queuedRemoteCandidates = ArrayList()
        if (isVideoCallEnabled) {
            factory!!.setVideoHwAccelerationOptions(
                rootEglBase.eglBaseContext, rootEglBase.eglBaseContext
            )
        }
        val rtcConfig = RTCConfiguration(signalingParameters.iceServers)
        // TCP candidates are only useful when connecting to a server that supports
        // ICE-TCP.
        rtcConfig.tcpCandidatePolicy = TcpCandidatePolicy.DISABLED
        rtcConfig.bundlePolicy = BundlePolicy.MAXBUNDLE
        rtcConfig.rtcpMuxPolicy = RtcpMuxPolicy.REQUIRE
        rtcConfig.continualGatheringPolicy =
            ContinualGatheringPolicy.GATHER_CONTINUALLY
        // Use ECDSA encryption.
        rtcConfig.keyType = KeyType.ECDSA
        // Enable DTLS for normal calls and disable for loopback calls.
        rtcConfig.enableDtlsSrtp = !peerConnectionParameters!!.loopback
        peerConnection = factory!!.createPeerConnection(rtcConfig, pcObserver)
        if (dataChannelEnabled) {
            val init = DataChannel.Init()
            init.ordered = peerConnectionParameters!!.dataChannelParameters.ordered
            init.negotiated = peerConnectionParameters!!.dataChannelParameters.negotiated
            init.maxRetransmits = peerConnectionParameters!!.dataChannelParameters.maxRetransmits
            init.maxRetransmitTimeMs =
                peerConnectionParameters!!.dataChannelParameters.maxRetransmitTimeMs
            init.id = peerConnectionParameters!!.dataChannelParameters.id
            init.protocol = peerConnectionParameters!!.dataChannelParameters.protocol
            dataChannel = peerConnection!!.createDataChannel("ApprtcDemo data", init)
        }
        isInitiator = false

        // Set INFO libjingle logging.
        // NOTE: this _must_ happen while |factory| is alive!
        Logging.enableLogToDebugOutput(Logging.Severity.LS_INFO)
        mediaStream = factory!!.createLocalMediaStream("ARDAMS")
        if (isVideoCallEnabled) {
            mediaStream.addTrack(createVideoTrack(videoCapturer))
        }
        mediaStream.addTrack(createAudioTrack())
        peerConnection!!.addStream(mediaStream)
        if (isVideoCallEnabled) {
            findVideoSender()
        }
        if (peerConnectionParameters!!.aecDump) {
            try {
                val aecDumpFileDescriptor = ParcelFileDescriptor.open(
                    File(
                        Environment.getExternalStorageDirectory().path
                                + File.separator + "Download/audio.aecdump"
                    ),
                    ParcelFileDescriptor.MODE_READ_WRITE or ParcelFileDescriptor.MODE_CREATE
                            or ParcelFileDescriptor.MODE_TRUNCATE
                )
                factory!!.startAecDump(aecDumpFileDescriptor.fd, -1)
            } catch (e: IOException) {
                Log.e(
                    PeerConnectionClient.Companion.TAG,
                    "Can not open aecdump file",
                    e
                )
            }
        }
        Log.d(
            PeerConnectionClient.Companion.TAG,
            "Peer connection created."
        )
    }

    private fun closeInternal() {
        if (factory != null && peerConnectionParameters!!.aecDump) {
            factory!!.stopAecDump()
        }
        Log.d(
            PeerConnectionClient.Companion.TAG,
            "Closing peer connection."
        )
        statsTimer!!.cancel()
        if (dataChannel != null) {
            dataChannel!!.dispose()
            dataChannel = null
        }
        if (peerConnection != null) {
            peerConnection!!.dispose()
            peerConnection = null
        }
        Log.d(
            PeerConnectionClient.Companion.TAG,
            "Closing audio source."
        )
        if (audioSource != null) {
            audioSource!!.dispose()
            audioSource = null
        }
        Log.d(
            PeerConnectionClient.Companion.TAG,
            "Stopping capture."
        )
        if (videoCapturer != null) {
            try {
                videoCapturer!!.stopCapture()
            } catch (e: InterruptedException) {
                throw RuntimeException(e)
            }
            videoCapturerStopped = true
            videoCapturer!!.dispose()
            videoCapturer = null
        }
        Log.d(
            PeerConnectionClient.Companion.TAG,
            "Closing video source."
        )
        if (videoSource != null) {
            videoSource!!.dispose()
            videoSource = null
        }
        localRender = null
        remoteRenders = null
        Log.d(
            PeerConnectionClient.Companion.TAG,
            "Closing peer connection factory."
        )
        if (factory != null) {
            factory!!.dispose()
            factory = null
        }
        options = null
        rootEglBase.release()
        Log.d(
            PeerConnectionClient.Companion.TAG,
            "Closing peer connection done."
        )
        events!!.onPeerConnectionClosed()
        PeerConnectionFactory.stopInternalTracingCapture()
        PeerConnectionFactory.shutdownInternalTracer()
        events = null
    }

    val isHDVideo: Boolean
        get() = isVideoCallEnabled && videoWidth * videoHeight >= 1280 * 720

    val renderContext: EglBase.Context
        get() = rootEglBase.eglBaseContext

    // TODO(sakal): getStats is deprecated.
    private val stats: Unit
        private get() {
            if (peerConnection == null || isError) {
                return
            }
            val success = peerConnection!!.getStats(
                { reports -> events!!.onPeerConnectionStatsReady(reports) },
                null
            )
            if (!success) {
                Log.e(
                    PeerConnectionClient.Companion.TAG,
                    "getStats() returns false!"
                )
            }
        }

    fun enableStatsEvents(enable: Boolean, periodMs: Int) {
        if (enable) {
            try {
                statsTimer!!.schedule(object : TimerTask() {
                    override fun run() {
                        PeerConnectionClient.Companion.executor.execute(
                            Runnable { stats })
                    }
                }, 0, periodMs.toLong())
            } catch (e: Exception) {
                Log.e(
                    PeerConnectionClient.Companion.TAG,
                    "Can not schedule statistics timer",
                    e
                )
            }
        } else {
            statsTimer!!.cancel()
        }
    }

    fun setAudioEnabled(enable: Boolean) {
        PeerConnectionClient.Companion.executor.execute(Runnable {
            enableAudio = enable
            if (localAudioTrack != null) {
                localAudioTrack!!.setEnabled(enableAudio)
            }
        })
    }

    fun setVideoEnabled(enable: Boolean) {
        PeerConnectionClient.Companion.executor.execute(Runnable {
            renderVideo = enable
            if (localVideoTrack != null) {
                localVideoTrack!!.setEnabled(renderVideo)
            }
            if (remoteVideoTrack != null) {
                remoteVideoTrack!!.setEnabled(renderVideo)
            }
        })
    }

    fun createOffer() {
        PeerConnectionClient.Companion.executor.execute(Runnable {
            if (peerConnection != null && !isError) {
                Log.d(
                    PeerConnectionClient.Companion.TAG,
                    "PC Create OFFER"
                )
                isInitiator = true
                peerConnection!!.createOffer(sdpObserver, sdpMediaConstraints)
            }
        })
    }

    fun createAnswer() {
        PeerConnectionClient.Companion.executor.execute(Runnable {
            if (peerConnection != null && !isError) {
                Log.d(
                    PeerConnectionClient.Companion.TAG,
                    "PC create ANSWER"
                )
                isInitiator = false
                peerConnection!!.createAnswer(sdpObserver, sdpMediaConstraints)
            }
        })
    }

    fun addRemoteIceCandidate(candidate: IceCandidate) {
        PeerConnectionClient.Companion.executor.execute(Runnable {
            if (peerConnection != null && !isError) {
                if (queuedRemoteCandidates != null) {
                    queuedRemoteCandidates!!.add(candidate)
                } else {
                    peerConnection!!.addIceCandidate(candidate)
                }
            }
        })
    }

    fun removeRemoteIceCandidates(candidates: Array<IceCandidate?>?) {
        PeerConnectionClient.Companion.executor.execute(Runnable {
            if (peerConnection == null || isError) {
                return@Runnable
            }
            // Drain the queued remote candidates if there is any so that
            // they are processed in the proper order.
            drainCandidates()
            peerConnection!!.removeIceCandidates(candidates)
        })
    }

    fun setRemoteDescription(sdp: SessionDescription) {
        PeerConnectionClient.Companion.executor.execute(object :
            Runnable {
            override fun run() {
                if (peerConnection == null || isError) {
                    return
                }
                var sdpDescription = sdp.description
                if (preferIsac) {
                    sdpDescription =
                        PeerConnectionClient.Companion.preferCodec(
                            sdpDescription,
                            PeerConnectionClient.Companion.AUDIO_CODEC_ISAC,
                            true
                        )
                }
                if (isVideoCallEnabled) {
                    sdpDescription =
                        PeerConnectionClient.Companion.preferCodec(
                            sdpDescription,
                            preferredVideoCodec,
                            false
                        )
                }
                if (peerConnectionParameters!!.audioStartBitrate > 0) {
                    sdpDescription =
                        PeerConnectionClient.Companion.setStartBitrate(
                            PeerConnectionClient.Companion.AUDIO_CODEC_OPUS,
                            false,
                            sdpDescription,
                            peerConnectionParameters!!.audioStartBitrate
                        )
                }
                Log.d(
                    PeerConnectionClient.Companion.TAG,
                    "Set remote SDP."
                )
                val sdpRemote = SessionDescription(sdp.type, sdpDescription)
                peerConnection!!.setRemoteDescription(sdpObserver, sdpRemote)
            }
        })
    }

    fun stopVideoSource() {
        PeerConnectionClient.Companion.executor.execute(Runnable {
            if (videoCapturer != null && !videoCapturerStopped) {
                Log.d(
                    PeerConnectionClient.Companion.TAG,
                    "Stop video source."
                )
                try {
                    videoCapturer!!.stopCapture()
                } catch (e: InterruptedException) {
                }
                videoCapturerStopped = true
            }
        })
    }

    fun startVideoSource() {
        PeerConnectionClient.Companion.executor.execute(Runnable {
            if (videoCapturer != null && videoCapturerStopped) {
                Log.d(
                    PeerConnectionClient.Companion.TAG,
                    "Restart video source."
                )
                videoCapturer!!.startCapture(videoWidth, videoHeight, videoFps)
                videoCapturerStopped = false
            }
        })
    }

    fun setVideoMaxBitrate(maxBitrateKbps: Int?) {
        PeerConnectionClient.Companion.executor.execute(Runnable {
            if (peerConnection == null || localVideoSender == null || isError) {
                return@Runnable
            }
            Log.d(
                PeerConnectionClient.Companion.TAG,
                "Requested max video bitrate: $maxBitrateKbps"
            )
            if (localVideoSender == null) {
                Log.w(
                    PeerConnectionClient.Companion.TAG,
                    "Sender is not ready."
                )
                return@Runnable
            }
            val parameters = localVideoSender!!.parameters
            if (parameters.encodings.size == 0) {
                Log.w(
                    PeerConnectionClient.Companion.TAG,
                    "RtpParameters are not ready."
                )
                return@Runnable
            }
            for (encoding in parameters.encodings) {
                // Null value means no limit.
                encoding.maxBitrateBps =
                    if (maxBitrateKbps == null) null else maxBitrateKbps * PeerConnectionClient.Companion.BPS_IN_KBPS
            }
            if (!localVideoSender!!.setParameters(parameters)) {
                Log.e(
                    PeerConnectionClient.Companion.TAG,
                    "RtpSender.setParameters failed."
                )
            }
            Log.d(
                PeerConnectionClient.Companion.TAG,
                "Configured max video bitrate to: $maxBitrateKbps"
            )
        })
    }

    private fun reportError(errorMessage: String) {
        Log.e(
            PeerConnectionClient.Companion.TAG,
            "Peerconnection error: $errorMessage"
        )
        PeerConnectionClient.Companion.executor.execute(Runnable {
            if (!isError) {
                events!!.onPeerConnectionError(errorMessage)
                isError = true
            }
        })
    }

    private fun createAudioTrack(): AudioTrack? {
        audioSource = factory!!.createAudioSource(audioConstraints)
        localAudioTrack = factory!!.createAudioTrack(
            PeerConnectionClient.Companion.AUDIO_TRACK_ID,
            audioSource
        )
        localAudioTrack.setEnabled(enableAudio)
        return localAudioTrack
    }

    private fun createVideoTrack(capturer: VideoCapturer?): VideoTrack? {
        videoSource = factory!!.createVideoSource(capturer)
        capturer!!.startCapture(videoWidth, videoHeight, videoFps)
        localVideoTrack = factory!!.createVideoTrack(
            PeerConnectionClient.Companion.VIDEO_TRACK_ID,
            videoSource
        )
        localVideoTrack.setEnabled(renderVideo)
        localVideoTrack.addSink(localRender)
        return localVideoTrack
    }

    private fun findVideoSender() {
        for (sender in peerConnection!!.senders) {
            if (sender.track() != null) {
                val trackType = sender.track()!!.kind()
                if (trackType == PeerConnectionClient.Companion.VIDEO_TRACK_TYPE) {
                    Log.d(
                        PeerConnectionClient.Companion.TAG,
                        "Found video sender."
                    )
                    localVideoSender = sender
                }
            }
        }
    }

    private fun drainCandidates() {
        if (queuedRemoteCandidates != null) {
            Log.d(
                PeerConnectionClient.Companion.TAG,
                "Add " + queuedRemoteCandidates!!.size + " remote candidates"
            )
            for (candidate in queuedRemoteCandidates!!) {
                peerConnection!!.addIceCandidate(candidate)
            }
            queuedRemoteCandidates = null
        }
    }

    private fun switchCameraInternal() {
        if (videoCapturer is CameraVideoCapturer) {
            if (!isVideoCallEnabled || isError) {
                Log.e(
                    PeerConnectionClient.Companion.TAG,
                    "Failed to switch camera. Video: " + isVideoCallEnabled + ". Error : " + isError
                )
                return  // No video is sent or only one camera is available or error happened.
            }
            Log.d(
                PeerConnectionClient.Companion.TAG,
                "Switch camera"
            )
            val cameraVideoCapturer = videoCapturer as CameraVideoCapturer
            cameraVideoCapturer.switchCamera(null)
        } else {
            Log.d(
                PeerConnectionClient.Companion.TAG,
                "Will not switch camera, video caputurer is not a camera"
            )
        }
    }

    fun switchCamera() {
        PeerConnectionClient.Companion.executor.execute(Runnable { switchCameraInternal() })
    }

    fun changeCaptureFormat(width: Int, height: Int, framerate: Int) {
        PeerConnectionClient.Companion.executor.execute(Runnable {
            changeCaptureFormatInternal(
                width,
                height,
                framerate
            )
        })
    }

    private fun changeCaptureFormatInternal(width: Int, height: Int, framerate: Int) {
        if (!isVideoCallEnabled || isError || videoCapturer == null) {
            Log.e(
                PeerConnectionClient.Companion.TAG,
                "Failed to change capture format. Video: " + isVideoCallEnabled + ". Error : " + isError
            )
            return
        }
        Log.d(
            PeerConnectionClient.Companion.TAG,
            "changeCaptureFormat: " + width + "x" + height + "@" + framerate
        )
        videoSource!!.adaptOutputFormat(width, height, framerate)
    }

    // Implementation detail: observe ICE & stream changes and react accordingly.
    private inner class PCObserver : Observer {
        override fun onIceCandidate(candidate: IceCandidate) {
            PeerConnectionClient.Companion.executor.execute(Runnable {
                events!!.onIceCandidate(
                    candidate
                )
            })
        }

        override fun onIceCandidatesRemoved(candidates: Array<IceCandidate>) {
            PeerConnectionClient.Companion.executor.execute(Runnable {
                events!!.onIceCandidatesRemoved(
                    candidates
                )
            })
        }

        override fun onSignalingChange(newState: SignalingState) {
            Log.d(
                PeerConnectionClient.Companion.TAG,
                "SignalingState: $newState"
            )
        }

        override fun onIceConnectionChange(newState: IceConnectionState) {
            PeerConnectionClient.Companion.executor.execute(Runnable {
                Log.d(
                    PeerConnectionClient.Companion.TAG,
                    "IceConnectionState: $newState"
                )
                if (newState == IceConnectionState.CONNECTED) {
                    events!!.onIceConnected()
                } else if (newState == IceConnectionState.DISCONNECTED) {
                    events!!.onIceDisconnected()
                } else if (newState == IceConnectionState.FAILED) {
                    reportError("ICE connection failed.")
                }
            })
        }

        override fun onIceGatheringChange(newState: IceGatheringState) {
            Log.d(
                PeerConnectionClient.Companion.TAG,
                "IceGatheringState: $newState"
            )
        }

        override fun onIceConnectionReceivingChange(receiving: Boolean) {
            Log.d(
                PeerConnectionClient.Companion.TAG,
                "IceConnectionReceiving changed to $receiving"
            )
        }

        override fun onAddStream(stream: MediaStream) {
            PeerConnectionClient.Companion.executor.execute(Runnable {
                if (peerConnection == null || isError) {
                    return@Runnable
                }
                if (stream.audioTracks.size > 1 || stream.videoTracks.size > 1) {
                    reportError("Weird-looking stream: $stream")
                    return@Runnable
                }
                if (stream.videoTracks.size == 1) {
                    remoteVideoTrack = stream.videoTracks[0]
                    remoteVideoTrack.setEnabled(renderVideo)
                    for (remoteRender in remoteRenders!!) {
                        remoteVideoTrack.addRenderer(VideoRenderer(remoteRender))
                    }
                }
            })
        }

        override fun onRemoveStream(stream: MediaStream) {
            PeerConnectionClient.Companion.executor.execute(Runnable { remoteVideoTrack = null })
        }

        override fun onDataChannel(dc: DataChannel) {
            Log.d(
                PeerConnectionClient.Companion.TAG,
                "New Data channel " + dc.label()
            )
            if (!dataChannelEnabled) return
            dc.registerObserver(object : DataChannel.Observer {
                override fun onBufferedAmountChange(previousAmount: Long) {
                    Log.d(
                        PeerConnectionClient.Companion.TAG,
                        "Data channel buffered amount changed: " + dc.label() + ": " + dc.state()
                    )
                }

                override fun onStateChange() {
                    Log.d(
                        PeerConnectionClient.Companion.TAG,
                        "Data channel state changed: " + dc.label() + ": " + dc.state()
                    )
                }

                override fun onMessage(buffer: DataChannel.Buffer) {
                    if (buffer.binary) {
                        Log.d(
                            PeerConnectionClient.Companion.TAG,
                            "Received binary msg over $dc"
                        )
                        return
                    }
                    val data = buffer.data
                    val bytes = ByteArray(data.capacity())
                    data[bytes]
                    val strData =
                        String(bytes, Charset.forName("UTF-8"))
                    Log.d(
                        PeerConnectionClient.Companion.TAG,
                        "Got msg: $strData over $dc"
                    )
                }
            })
        }

        override fun onRenegotiationNeeded() {
            // No need to do anything; AppRTC follows a pre-agreed-upon
            // signaling/negotiation protocol.
        }

        override fun onAddTrack(
            receiver: RtpReceiver,
            mediaStreams: Array<MediaStream>
        ) {
        }
    }

    // Implementation detail: handle offer creation/signaling and answer setting,
    // as well as adding remote ICE candidates once the answer SDP is set.
    private inner class SDPObserver : SdpObserver {
        override fun onCreateSuccess(origSdp: SessionDescription) {
            if (localSdp != null) {
                reportError("Multiple SDP create.")
                return
            }
            var sdpDescription = origSdp.description
            if (preferIsac) {
                sdpDescription =
                    PeerConnectionClient.Companion.preferCodec(
                        sdpDescription,
                        PeerConnectionClient.Companion.AUDIO_CODEC_ISAC,
                        true
                    )
            }
            if (isVideoCallEnabled) {
                sdpDescription =
                    PeerConnectionClient.Companion.preferCodec(
                        sdpDescription,
                        preferredVideoCodec,
                        false
                    )
            }
            val sdp = SessionDescription(origSdp.type, sdpDescription)
            localSdp = sdp
            PeerConnectionClient.Companion.executor.execute(Runnable {
                if (peerConnection != null && !isError) {
                    Log.d(
                        PeerConnectionClient.Companion.TAG,
                        "Set local SDP from " + sdp.type
                    )
                    peerConnection!!.setLocalDescription(sdpObserver, sdp)
                }
            })
        }

        override fun onSetSuccess() {
            PeerConnectionClient.Companion.executor.execute(Runnable {
                if (peerConnection == null || isError) {
                    return@Runnable
                }
                if (isInitiator) {
                    // For offering peer connection we first create offer and set
                    // local SDP, then after receiving answer set remote SDP.
                    if (peerConnection!!.remoteDescription == null) {
                        // We've just set our local SDP so time to send it.
                        Log.d(
                            PeerConnectionClient.Companion.TAG,
                            "Local SDP set succesfully"
                        )
                        events!!.onLocalDescription(localSdp)
                    } else {
                        // We've just set remote description, so drain remote
                        // and send local ICE candidates.
                        Log.d(
                            PeerConnectionClient.Companion.TAG,
                            "Remote SDP set succesfully"
                        )
                        drainCandidates()
                    }
                } else {
                    // For answering peer connection we set remote SDP and then
                    // create answer and set local SDP.
                    if (peerConnection!!.localDescription != null) {
                        // We've just set our local SDP so time to send it, drain
                        // remote and send local ICE candidates.
                        Log.d(
                            PeerConnectionClient.Companion.TAG,
                            "Local SDP set succesfully"
                        )
                        events!!.onLocalDescription(localSdp)
                        drainCandidates()
                    } else {
                        // We've just set remote SDP - do nothing for now -
                        // answer will be created soon.
                        Log.d(
                            PeerConnectionClient.Companion.TAG,
                            "Remote SDP set succesfully"
                        )
                    }
                }
            })
        }

        override fun onCreateFailure(error: String) {
            reportError("createSDP error: $error")
        }

        override fun onSetFailure(error: String) {
            reportError("setSDP error: $error")
        }
    }

    companion object {
        const val VIDEO_TRACK_ID = "ARDAMSv0"
        const val AUDIO_TRACK_ID = "ARDAMSa0"
        const val VIDEO_TRACK_TYPE = "video"
        private const val TAG = "PCRTCClient"
        private const val VIDEO_CODEC_VP8 = "VP8"
        private const val VIDEO_CODEC_VP9 = "VP9"
        private const val VIDEO_CODEC_H264 = "H264"
        private const val VIDEO_CODEC_H264_BASELINE = "H264 Baseline"
        private const val VIDEO_CODEC_H264_HIGH = "H264 High"
        private const val AUDIO_CODEC_OPUS = "opus"
        private const val AUDIO_CODEC_ISAC = "ISAC"
        private const val VIDEO_CODEC_PARAM_START_BITRATE = "x-google-start-bitrate"
        private const val VIDEO_FLEXFEC_FIELDTRIAL =
            "WebRTC-FlexFEC-03-Advertised/Enabled/WebRTC-FlexFEC-03/Enabled/"
        private const val VIDEO_VP8_INTEL_HW_ENCODER_FIELDTRIAL =
            "WebRTC-IntelVP8/Enabled/"
        private const val VIDEO_H264_HIGH_PROFILE_FIELDTRIAL =
            "WebRTC-H264HighProfile/Enabled/"
        private const val DISABLE_WEBRTC_AGC_FIELDTRIAL =
            "WebRTC-Audio-MinimizeResamplingOnMobile/Enabled/"
        private const val VIDEO_FRAME_EMIT_FIELDTRIAL =
            (PeerConnectionFactory.VIDEO_FRAME_EMIT_TRIAL + "/" + PeerConnectionFactory.TRIAL_ENABLED
                    + "/")
        private const val AUDIO_CODEC_PARAM_BITRATE = "maxaveragebitrate"
        private const val AUDIO_ECHO_CANCELLATION_CONSTRAINT = "googEchoCancellation"
        private const val AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT = "googAutoGainControl"
        private const val AUDIO_HIGH_PASS_FILTER_CONSTRAINT = "googHighpassFilter"
        private const val AUDIO_NOISE_SUPPRESSION_CONSTRAINT = "googNoiseSuppression"
        private const val AUDIO_LEVEL_CONTROL_CONSTRAINT = "levelControl"
        private const val DTLS_SRTP_KEY_AGREEMENT_CONSTRAINT = "DtlsSrtpKeyAgreement"
        private const val HD_VIDEO_WIDTH = 1280
        private const val HD_VIDEO_HEIGHT = 720
        private const val BPS_IN_KBPS = 1000

        // Executor thread is started once in private ctor and is used for all
        // peer connection API calls to ensure new peer connection factory is
        // created on the same thread as previously destroyed factory.
        private val executor =
            Executors.newSingleThreadExecutor()

        private fun setStartBitrate(
            codec: String,
            isVideoCodec: Boolean,
            sdpDescription: String,
            bitrateKbps: Int
        ): String {
            val lines =
                sdpDescription.split("\r\n".toRegex()).toTypedArray()
            var rtpmapLineIndex = -1
            var sdpFormatUpdated = false
            var codecRtpMap: String? = null
            // Search for codec rtpmap in format
            // a=rtpmap:<payload type> <encoding name>/<clock rate> [/<encoding parameters>]
            var regex = "^a=rtpmap:(\\d+) $codec(/\\d+)+[\r]?$"
            var codecPattern = Pattern.compile(regex)
            for (i in lines.indices) {
                val codecMatcher = codecPattern.matcher(lines[i])
                if (codecMatcher.matches()) {
                    codecRtpMap = codecMatcher.group(1)
                    rtpmapLineIndex = i
                    break
                }
            }
            if (codecRtpMap == null) {
                Log.w(
                    PeerConnectionClient.Companion.TAG,
                    "No rtpmap for $codec codec"
                )
                return sdpDescription
            }
            Log.d(
                PeerConnectionClient.Companion.TAG,
                "Found " + codec + " rtpmap " + codecRtpMap + " at " + lines[rtpmapLineIndex]
            )

            // Check if a=fmtp string already exist in remote SDP for this codec and
            // update it with new bitrate parameter.
            regex = "^a=fmtp:$codecRtpMap \\w+=\\d+.*[\r]?$"
            codecPattern = Pattern.compile(regex)
            for (i in lines.indices) {
                val codecMatcher = codecPattern.matcher(lines[i])
                if (codecMatcher.matches()) {
                    Log.d(
                        PeerConnectionClient.Companion.TAG,
                        "Found " + codec + " " + lines[i]
                    )
                    if (isVideoCodec) {
                        lines[i] += "; " + PeerConnectionClient.Companion.VIDEO_CODEC_PARAM_START_BITRATE + "=" + bitrateKbps
                    } else {
                        lines[i] += "; " + PeerConnectionClient.Companion.AUDIO_CODEC_PARAM_BITRATE + "=" + bitrateKbps * 1000
                    }
                    Log.d(
                        PeerConnectionClient.Companion.TAG,
                        "Update remote SDP line: " + lines[i]
                    )
                    sdpFormatUpdated = true
                    break
                }
            }
            val newSdpDescription = StringBuilder()
            for (i in lines.indices) {
                newSdpDescription.append(lines[i]).append("\r\n")
                // Append new a=fmtp line if no such line exist for a codec.
                if (!sdpFormatUpdated && i == rtpmapLineIndex) {
                    var bitrateSet: String
                    bitrateSet = if (isVideoCodec) {
                        "a=fmtp:" + codecRtpMap + " " + PeerConnectionClient.Companion.VIDEO_CODEC_PARAM_START_BITRATE + "=" + bitrateKbps
                    } else {
                        ("a=fmtp:" + codecRtpMap + " " + PeerConnectionClient.Companion.AUDIO_CODEC_PARAM_BITRATE + "="
                                + bitrateKbps * 1000)
                    }
                    Log.d(
                        PeerConnectionClient.Companion.TAG,
                        "Add remote SDP line: $bitrateSet"
                    )
                    newSdpDescription.append(bitrateSet).append("\r\n")
                }
            }
            return newSdpDescription.toString()
        }

        /** Returns the line number containing "m=audio|video", or -1 if no such line exists.  */
        private fun findMediaDescriptionLine(
            isAudio: Boolean,
            sdpLines: Array<String>
        ): Int {
            val mediaDescription = if (isAudio) "m=audio " else "m=video "
            for (i in sdpLines.indices) {
                if (sdpLines[i].startsWith(mediaDescription)) {
                    return i
                }
            }
            return -1
        }

        private fun joinString(
            s: Iterable<CharSequence?>,
            delimiter: String,
            delimiterAtEnd: Boolean
        ): String {
            val iter = s.iterator()
            if (!iter.hasNext()) {
                return ""
            }
            val buffer = StringBuilder(iter.next())
            while (iter.hasNext()) {
                buffer.append(delimiter).append(iter.next())
            }
            if (delimiterAtEnd) {
                buffer.append(delimiter)
            }
            return buffer.toString()
        }

        private fun movePayloadTypesToFront(
            preferredPayloadTypes: List<String>,
            mLine: String
        ): String? {
            // The format of the media description line should be: m=<media> <port> <proto> <fmt> ...
            val origLineParts =
                Arrays.asList(*mLine.split(" ".toRegex()).toTypedArray())
            if (origLineParts.size <= 3) {
                Log.e(
                    PeerConnectionClient.Companion.TAG,
                    "Wrong SDP media description format: $mLine"
                )
                return null
            }
            val header: List<String> = origLineParts.subList(0, 3)
            val unpreferredPayloadTypes: MutableList<String> =
                ArrayList(origLineParts.subList(3, origLineParts.size))
            unpreferredPayloadTypes.removeAll(preferredPayloadTypes)
            // Reconstruct the line with |preferredPayloadTypes| moved to the beginning of the payload
            // types.
            val newLineParts: MutableList<String> =
                ArrayList()
            newLineParts.addAll(header)
            newLineParts.addAll(preferredPayloadTypes)
            newLineParts.addAll(unpreferredPayloadTypes)
            return PeerConnectionClient.Companion.joinString(
                newLineParts,
                " ",
                false /* delimiterAtEnd */
            )
        }

        private fun preferCodec(
            sdpDescription: String,
            codec: String,
            isAudio: Boolean
        ): String {
            val lines =
                sdpDescription.split("\r\n".toRegex()).toTypedArray()
            val mLineIndex: Int =
                PeerConnectionClient.Companion.findMediaDescriptionLine(
                    isAudio,
                    lines
                )
            if (mLineIndex == -1) {
                Log.w(
                    PeerConnectionClient.Companion.TAG,
                    "No mediaDescription line, so can't prefer $codec"
                )
                return sdpDescription
            }
            // A list with all the payload types with name |codec|. The payload types are integers in the
            // range 96-127, but they are stored as strings here.
            val codecPayloadTypes: MutableList<String> =
                ArrayList()
            // a=rtpmap:<payload type> <encoding name>/<clock rate> [/<encoding parameters>]
            val codecPattern =
                Pattern.compile("^a=rtpmap:(\\d+) $codec(/\\d+)+[\r]?$")
            for (line in lines) {
                val codecMatcher = codecPattern.matcher(line)
                if (codecMatcher.matches()) {
                    codecPayloadTypes.add(codecMatcher.group(1))
                }
            }
            if (codecPayloadTypes.isEmpty()) {
                Log.w(
                    PeerConnectionClient.Companion.TAG,
                    "No payload types with name $codec"
                )
                return sdpDescription
            }
            val newMLine: String =
                PeerConnectionClient.Companion.movePayloadTypesToFront(
                    codecPayloadTypes,
                    lines[mLineIndex]
                )
                    ?: return sdpDescription
            Log.d(
                PeerConnectionClient.Companion.TAG,
                "Change media description from: " + lines[mLineIndex] + " to " + newMLine
            )
            lines[mLineIndex] = newMLine
            return PeerConnectionClient.Companion.joinString(
                Arrays.asList(
                    *lines
                ), "\r\n", true /* delimiterAtEnd */
            )
        }
    }

    init {
        rootEglBase = EglBase.create()
    }
}