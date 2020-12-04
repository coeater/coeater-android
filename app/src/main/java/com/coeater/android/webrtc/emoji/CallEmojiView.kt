package com.coeater.android.webrtc.emoji

import android.app.ProgressDialog
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coeater.android.R
import kotlinx.android.synthetic.main.view_call_emoji.view.*
import kotlinx.coroutines.*


interface CallEmojiOutput {
    fun sendEmoji(xPercentage: Double, yPercentage: Double, file: String)
    fun deleteAllEmoji()
}

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

    var output: CallEmojiOutput? = null
    private val lottieSize = pxFromDp(this.context, 300.toFloat())

    init {
        LayoutInflater.from(context).inflate(R.layout.view_call_emoji, this, true)

        viewManager = GridLayoutManager(this.context, 5)
        viewAdapter = CallEmojiSelectAdapter(provideEmojiData())
        ib_emoji_close.setOnClickListener {
            setActivity(false)
        }
        rv_emoji_select.apply {
            setHasFixedSize(true)
            layoutManager = viewManager

            adapter = viewAdapter
        }
        viewAdapter.notifyDataSetChanged()

        layout_emoji_touch.setOnTouchListener { view, motionEvent ->
            if (rv_emoji_select.visibility == View.VISIBLE) {
                val x = motionEvent.x.toInt()
                val y = motionEvent.y.toInt()
                if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                    showEmoji(x, y, viewAdapter.selectedLottieFile)
                    sendEmojiByClient(x, y, viewAdapter.selectedLottieFile)
                }
            }
            return@setOnTouchListener true

        }
        ib_emoji_uneffect.setOnClickListener {
            deleteEmoji()
            output?.deleteAllEmoji()
        }
    }



    private fun pxFromDp(context: Context, dp: Float): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
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

    internal fun setActivity(isActivated: Boolean) {
        if (isActivated) {
            ib_emoji_close.visibility = View.VISIBLE
            ib_emoji_uneffect.visibility = View.VISIBLE
            rv_emoji_select.visibility = View.VISIBLE
        } else {
            ib_emoji_close.visibility = View.INVISIBLE
            ib_emoji_uneffect.visibility = View.INVISIBLE
            rv_emoji_select.visibility = View.INVISIBLE
        }
    }


    /**
     * 서버에서 가져온 이모지 명령을 전송한다.
     */
    internal fun showEmojiByOpponent(xPercentage: Double, yPercentage: Double, file: String) {
        val x = (xPercentage * layout_emoji_touch.width).toInt()
        val y = (yPercentage * layout_emoji_touch.height).toInt()
        showEmoji(x, y, file)
    }

    fun deleteEmoji() {
        GlobalScope.launch(Dispatchers.Main) {
          layout_lottie.visibility = View.GONE
        }
    }

    private fun sendEmojiByClient(x: Int, y: Int, lottieFile: String) {
        val xPercentage = x.toDouble() / layout_emoji_touch.width.toDouble()
        val yPercentage = y.toDouble() / layout_emoji_touch.height.toDouble()
        output?.sendEmoji(xPercentage, yPercentage, lottieFile)
    }
    

    private fun showEmoji(x: Int, y: Int, lottieFile: String) {
        GlobalScope.launch(Dispatchers.Main) {
            layout_lottie.visibility = View.VISIBLE
            val paddingX = -lottieSize / 2 + x
            val paddingY = -lottieSize / 2 + y
            setMargins(layout_lottie, paddingX, paddingY, 0, 0)
            layout_lottie.setAnimation(lottieFile)
            layout_lottie.loop(true);
            layout_lottie.playAnimation()
        }

    }

}
