package com.coeater.android.webrtc.game

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coeater.android.api.UserApi
import com.coeater.android.model.FriendsInfo
import com.coeater.android.model.HTTPResult
import com.coeater.android.model.User
import com.coeater.android.webrtc.game.model.CallGameChoice
import com.coeater.android.webrtc.game.model.CallGameMatch
import com.coeater.android.webrtc.game.model.CallGameResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException

interface CallGameInputFromSocket {
    fun showChoice(choice: CallGameChoice)
    fun showMatch(choice: CallGameMatch)
    fun showResult(choice: CallGameResult)
}

interface CallGameInputFromView {
    fun pickChoice(left: Boolean)
}

interface CallGameOutput {
    fun sendChoice(left: Boolean)
}

class CallGameViewModel() : ViewModel(), CallGameInputFromView, CallGameInputFromSocket {


    val choiceData: MutableLiveData<CallGameChoice> by lazy {
        MutableLiveData<CallGameChoice>()
    }


    val matchData: MutableLiveData<CallGameMatch> by lazy {
        MutableLiveData<CallGameMatch>()
    }

    val resultData: MutableLiveData<CallGameResult> by lazy {
        MutableLiveData<CallGameResult>()
    }

    val output: CallGameOutput? = null

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
        output?.sendChoice(left)
    }



}