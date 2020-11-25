package com.coeater.android.webrtc.emoji

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.coeater.android.R
import kotlinx.android.synthetic.main.view_call_emoji_item.view.*

class CallEmojiSelectAdapter(private val lottieData: List<String>) :
    RecyclerView.Adapter<CallEmojiSelectAdapter.CallEmojiSelectHolder>() {

    class CallEmojiSelectHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val lottieView by lazy { itemView.iv_emoji_preview }
        private val selectedImageView by lazy { itemView.iv_emoji_selected }

        fun bind(lottieFile: String, isSelected: Boolean) {
            lottieView.setAnimation(lottieFile)
            if (isSelected) {
                selectedImageView.visibility = View.VISIBLE
            } else {
                selectedImageView.visibility = View.GONE
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): CallEmojiSelectAdapter.CallEmojiSelectHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_call_emoji_item, parent, false)
        return CallEmojiSelectHolder(view)
    }

    override fun onBindViewHolder(holder: CallEmojiSelectHolder, position: Int) {
        val fileName = lottieData[position]
        holder.bind(fileName, position == selectedIndex)
        holder.itemView.setOnClickListener {
            itemSelected(position)
        }

    }

    var selectedIndex: Int = 0

    private fun itemSelected(position: Int) {
        if (selectedIndex == position) {
            return
        }
        val previousIndex = selectedIndex
        selectedIndex = position
        notifyItemChanged(previousIndex)
        notifyItemChanged(selectedIndex)
    }

    override fun getItemCount() = lottieData.size


}