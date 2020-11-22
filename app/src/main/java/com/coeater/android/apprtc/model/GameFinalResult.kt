package com.coeater.android.apprtc.model

/**
 * 나와 상대방의 유사도를 전송한다.
 */
data class GameFinalResult(
    val isMatched: Boolean = false,
    val similarity: Int = 0
)
