package com.coeater.android.webrtc.emoji

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
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
    private var viewAdapter: RecyclerView.Adapter<*>
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
    }




}
