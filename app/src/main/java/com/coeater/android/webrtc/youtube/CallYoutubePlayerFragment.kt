package com.coeater.android.webrtc.youtube

import android.os.Bundle
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayerFragment

class CallYoutubePlayerFragment : YouTubePlayerFragment(), YouTubePlayer.OnInitializedListener, YouTubePlayer.PlayerStateChangeListener {
    private var player: YouTubePlayer? = null
    private var videoId: String? = null
    private var ready: Boolean = false
    private var onAd: Boolean = false

    fun newInstance(): CallYoutubePlayerFragment {
        return CallYoutubePlayerFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialize(DeveloperKey.DEVELOPER_KEY, this)
    }

    override fun onDestroy() {
        if (player != null) {
            player!!.release()
        }
        super.onDestroy()
    }

    override fun onInitializationSuccess(
        provider: YouTubePlayer.Provider?,
        player: YouTubePlayer,
        restored: Boolean
    ) {
        this.player = player
        this.player!!.setShowFullscreenButton(false)
        if (!restored && videoId != null) {
            player.cueVideo(videoId)
        }
    }

    override fun onInitializationFailure(
        provider: YouTubePlayer.Provider?,
        result: YouTubeInitializationResult?
    ) {
        player = null
    }

    override fun onAdStarted() {
        onAd = true
        ready = false
    }

    override fun onError(reason: YouTubePlayer.ErrorReason) {
    }

    override fun onVideoStarted() {
        ready = true
    }
    override fun onVideoEnded() {
        ready = true
    }

    override fun onLoading() {
        ready = false
    }

    override fun onLoaded(videoId: String?) {
        ready = true
    }

    public fun setVideoId(videoId: String?) {
        if (videoId != null && videoId != this.videoId) {
            this.videoId = videoId
            if (player != null) {
                player!!.cueVideo(videoId)
            }
        }
    }

    public fun play() {
        if (player != null && ready) {
            player!!.play()
        }
    }

    public fun pause() {
        if (player != null && ready) {
            player!!.pause()
        }
    }

    public fun getCurrentMillis(): Int {
        if (player != null && ready) {
            return player!!.currentTimeMillis
        }
        return -1
    }

    public fun isPlaying(): Boolean {
        if (player != null && ready)
            return player!!.isPlaying
        return false
    }

    public fun getDurationMillis(): Int {
        if (player != null && ready) {
            return player!!.durationMillis
        }
        return -1
    }

    public fun seekRelativeMillis(milliSeconds: Int) {
        if (player != null && ready)
            player!!.seekRelativeMillis(milliSeconds)
    }

    public fun seekToMillis(milliSeconds: Int) {
        if (player != null && ready)
            player!!.seekToMillis(milliSeconds)
    }
}