package com.coeater.android.apprtc.model

/**
 * 나와 상대방의 일치 여부를 전송한다.
 */
data class GameMatchResult(
    val isMatched: Boolean = false,
    val nextInfo: GameInfo = GameInfo()
)
