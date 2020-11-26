package com.coeater.android.webrtc.game

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.coeater.android.apprtc.WebSocketRTCClient
import com.coeater.android.webrtc.game.model.CallGameChoice
import com.coeater.android.webrtc.game.model.CallGameMatch
import com.coeater.android.webrtc.game.model.CallGameResult

interface CallGameInputFromSocket {
    fun showChoice(choice: CallGameChoice)
    fun showMatch(choice: CallGameMatch)
    fun showResult(choice: CallGameResult)
}

interface CallGameInputFromView {
    fun pickChoice(left: Boolean)
}


class CallGameViewModel(private val client: WebSocketRTCClient) : ViewModel(), CallGameInputFromView, CallGameInputFromSocket {


    val choiceData: MutableLiveData<CallGameChoice> by lazy {
        MutableLiveData<CallGameChoice>()
    }


    val matchData: MutableLiveData<CallGameMatch> by lazy {
        MutableLiveData<CallGameMatch>()
    }

    val resultData: MutableLiveData<CallGameResult> by lazy {
        MutableLiveData<CallGameResult>()
    }

    override fun showChoice(choice: CallGameChoice) {
       choiceData.postValue(choice)
    }

    override fun showMatch(match: CallGameMatch) {
        matchData.postValue(match)
    }

    override fun showResult(result: CallGameResult) {
        resultData.postValue(result)
    }

    override fun pickChoice(left: Boolean) {
        val stage = choiceData.value?.stage ?: return
        client?.sendImageSelectResult(stage, left)
    }



}