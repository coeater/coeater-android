package com.coeater.android.webrtc.game

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.coeater.android.R
import com.coeater.android.apprtc.WebSocketRTCClient
import com.coeater.android.webrtc.CallActivity
import com.coeater.android.webrtc.game.model.CallGameChoice
import com.coeater.android.webrtc.game.model.CallGameMatch
import com.coeater.android.webrtc.game.model.CallGameResult
import kotlinx.android.synthetic.main.view_game.view.*

class CallGameView : ConstraintLayout {

    constructor(context: Context?) : super(context) {
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr) {
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.view_game, this, true)
        view_choice.visibility = View.GONE
        call_game_view_match.visibility = View.GONE
        view_result.visibility = View.GONE

    }



    private lateinit var viewModel: CallGameViewModel


    fun configure(activity: CallActivity, client: WebSocketRTCClient) : CallGameInputFromSocket {
        val viewModelFactory = CallGameViewModelFactory(client)

        viewModel = ViewModelProviders.of(
            activity, viewModelFactory
        )[CallGameViewModel::class.java]
        viewModel
        viewModel.choiceData.observe(activity,  Observer<CallGameChoice> {  choice ->
            view_choice.visibility = View.VISIBLE
            call_game_view_match.visibility = View.GONE
            view_result.visibility = View.GONE
            view_choice.configure(choice) {
                viewModel.pickChoice(it)
            }
        })
        viewModel.matchData.observe(activity,  Observer<CallGameMatch> {  match ->
            view_choice.visibility = View.GONE
            call_game_view_match.visibility = View.VISIBLE
            view_result.visibility = View.GONE
            call_game_view_match.configure(match) {
                call_game_view_match.visibility = View.GONE
            }
        })
        viewModel.resultData.observe(activity,  Observer<CallGameResult> {  result ->
            view_choice.visibility = View.GONE
            call_game_view_match.visibility = View.GONE
            view_result.visibility = View.VISIBLE
            view_result.configure(result, {
                view_result.visibility = View.GONE
            }, {

            })
        })
        return viewModel
    }



}
