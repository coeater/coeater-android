package com.coeater.android.webrtc.game

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.coeater.android.R
import com.coeater.android.webrtc.game.model.CallGameResult
import kotlinx.android.synthetic.main.view_game_result.view.*

class CallGameResultView : ConstraintLayout {

    constructor(context: Context?) : super(context) {
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr) {
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.view_game_result, this, true)
    }

    fun configure(result: CallGameResult, clickClose: ()-> Unit, clickShare: ()-> Unit) {
        pb_game_result_similarity.text = result.similarity.toString()
        pb_game_result.max = 100
        pb_game_result.progress = result.similarity
        ib_game_result_close.setOnClickListener {
            clickClose()
        }
    }

}
