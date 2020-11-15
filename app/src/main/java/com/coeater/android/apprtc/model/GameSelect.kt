package com.coeater.android.apprtc.model

/**
 * 내 선택 결과를 전송한다.
 */
data class GameSelect(
    val stage: Int = 0,
    val isLeft: Boolean = false
)
