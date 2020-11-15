package com.coeater.android.webrtc.game

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.coeater.android.R
import com.coeater.android.webrtc.game.model.CallGameMatch
import kotlinx.android.synthetic.main.view_game_match.view.*

class CallGameMatchView : ConstraintLayout {

    constructor(context: Context?) : super(context) {
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr) {
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.view_game_match, this, true)
    }

    fun configure(match: CallGameMatch, clickClose: ()-> Unit) {
        if (match.isMatched) {
            view_match.visibility = View.VISIBLE
            iv_match.visibility = View.VISIBLE
            view_not_match.visibility = View.GONE
            iv_not_match.visibility = View.GONE
            view_match.setOnClickListener {
                clickClose()
            }
        } else {
            view_match.visibility = View.GONE
            iv_match.visibility = View.GONE
            view_not_match.visibility = View.VISIBLE
            iv_not_match.visibility = View.VISIBLE
            view_not_match.setOnClickListener {
                clickClose()
            }
        }
    }

}
