package com.coeater.android.webrtc.game

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.coeater.android.R
import com.coeater.android.webrtc.game.model.CallGameChoice
import com.coeater.android.webrtc.game.model.CallGameMatch
import kotlinx.android.synthetic.main.view_friend_requests_recycler_item.view.*
import kotlinx.android.synthetic.main.view_game_choice.view.*
import kotlinx.android.synthetic.main.view_game_match.view.*

class CallGameChoiceView : ConstraintLayout {

    constructor(context: Context?) : super(context) {
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr) {
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.view_game_choice, this, true)
    }

    fun configure(choice: CallGameChoice, clickChoice: (Boolean)-> Unit) {
        Glide.with(context)
            .load(choice.leftImage)
            .apply(RequestOptions.circleCropTransform())
            .into(button_left_choice)
            .clearOnDetach()
        Glide.with(context)
            .load(choice.rightImage)
            .apply(RequestOptions.circleCropTransform())
            .into(button_right_choice)
            .clearOnDetach()
        progressbar_game_choice.setProgress(choice.stage, false)
        button_left_choice.setOnClickListener {
            clickChoice(true)
        }
        button_right_choice.setOnClickListener {
            clickChoice(false)
        }

    }

}
