package com.coeater.android.webrtc.game

import android.animation.Animator
import android.content.Context
import android.util.AttributeSet
import android.util.Log
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

    fun configure(isMatched: Boolean, clickClose: ()-> Unit) {
        if (isMatched) {
            view_match.visibility = View.VISIBLE
            view_not_match.visibility = View.GONE
            av_match.visibility = View.VISIBLE
            av_not_match.visibility = View.GONE
            av_match.removeAllAnimatorListeners()
            av_match.addAnimatorListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator?) {
                }

                override fun onAnimationEnd(animation: Animator?) {
                    clickClose()
                }

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationRepeat(animation: Animator?) {
                }
            })
            av_match.playAnimation()
            view_match.setOnClickListener {
                clickClose()
            }
        } else {
            view_match.visibility = View.GONE
            view_not_match.visibility = View.VISIBLE
            av_match.visibility = View.GONE
            av_not_match.visibility = View.VISIBLE
            av_not_match.removeAllAnimatorListeners()
            av_not_match.addAnimatorListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator?) {
                }

                override fun onAnimationEnd(animation: Animator?) {
                    clickClose()
                }

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationRepeat(animation: Animator?) {
                }
            })
            av_not_match.playAnimation()
            view_not_match.setOnClickListener {
                clickClose()
            }
        }
    }

}
