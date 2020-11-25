package com.coeater.android.webrtc.emoji

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coeater.android.R
import kotlinx.android.synthetic.main.view_call_emoji.view.*


class CallEmojiView : ConstraintLayout {

    constructor(context: Context?) : super(context) {
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr) {
    }

    private var viewAdapter: CallEmojiSelectAdapter
    private var viewManager: RecyclerView.LayoutManager

    init {
        LayoutInflater.from(context).inflate(R.layout.view_call_emoji, this, true)

        viewManager = GridLayoutManager(this.context, 5)
        viewAdapter = CallEmojiSelectAdapter(provideEmojiData())

        rv_emoji_select.apply {
            setHasFixedSize(true)
            layoutManager = viewManager

            adapter = viewAdapter
        }
        viewAdapter.notifyDataSetChanged()

        layout_emoji_touch.setOnTouchListener { view, motionEvent ->

            val x = motionEvent.x.toInt()
            val y = motionEvent.y.toInt()
            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                showEmoji(x, y)
            }

            return@setOnTouchListener true

        }
    }


    private fun setMargins(
        view: View,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ) {
        if (view.getLayoutParams() is MarginLayoutParams) {
            val p = view.getLayoutParams() as MarginLayoutParams
            p.setMargins(left, top, right, bottom)
            view.requestLayout()
        }
    }

    private fun showEmoji(x: Int, y: Int) {

        val lottieFile = viewAdapter.selectedLottieFile
        val paddingX = -layout_lottie.width / 2 + x
        val paddingY = -layout_lottie.height / 2 + y
        setMargins(layout_lottie, paddingX, paddingY, 0, 0)

        layout_lottie.setAnimation(lottieFile)
        layout_lottie.playAnimation()
    }

}
