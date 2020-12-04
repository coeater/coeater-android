package com.coeater.android.webrtc.youtube

import android.content.Context
import android.os.Handler
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.coeater.android.R
import com.coeater.android.model.DateTime
import com.coeater.android.model.YoutubeItem
import com.coeater.android.webrtc.YoutubeHandlerEvent
import kotlinx.android.synthetic.main.view_youtube_recycler_item.view.*

class CallYoutubeSearchAdapter (private val context: Context, private val resultDataset: List<YoutubeItem>, private val youtubeHandler: Handler) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    class YoutubeSearchResultViewHolder(val ItemLayout : ConstraintLayout) : RecyclerView.ViewHolder(ItemLayout)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_youtube_recycler_item, parent, false) as ConstraintLayout

        return YoutubeSearchResultViewHolder(itemLayout)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val itemLayout = (holder as YoutubeSearchResultViewHolder).ItemLayout
        val title:String = resultDataset[position].snippet.title
        val channelTitle:String = resultDataset[position].snippet.channelTitle
        val dateAgo:String = DateTime.getAgo(resultDataset[position].snippet.publishedAt)
        val videoId:String = resultDataset[position].id.videoId

        if (title.length >34)
            itemLayout.tv_youtube_title.text = title.substring(0..34)+"..."
        else
            itemLayout.tv_youtube_title.text = title
        if (channelTitle.length > 17)
            itemLayout.tv_youtube_subtitle.text = channelTitle.substring(0..17)+"..., "+dateAgo
        else
            itemLayout.tv_youtube_subtitle.text = "$channelTitle, $dateAgo"

        itemLayout.setOnClickListener {
            youtubeHandler.obtainMessage(YoutubeHandlerEvent.SET_VIDEO_ID.value, videoId).sendToTarget()
        }

        Glide.with(itemLayout)
            .load(resultDataset[position].snippet.thumbnails.default.url)
            .into(itemLayout.iv_youtube_thumbnail)
            .clearOnDetach()
    }

    override fun getItemCount(): Int {
        return resultDataset.size
    }
}