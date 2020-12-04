package com.coeater.android.history

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.coeater.android.R
import com.coeater.android.model.DateTime
import com.coeater.android.model.Profile
import com.coeater.android.model.User
import kotlinx.android.synthetic.main.view_histoy_recycler_item.view.*

class HistoryAdapter(private val context: Context, private val historyDataset: List<User>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {



    class HistoryViewHolder(val ItemLayout : ConstraintLayout) : RecyclerView.ViewHolder(ItemLayout)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_histoy_recycler_item, parent, false) as ConstraintLayout

        return HistoryViewHolder(itemLayout)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val itemLayout = (holder as HistoryViewHolder).ItemLayout
        itemLayout.tv_nickname.text = historyDataset[position].nickname
        itemLayout.tv_history.text = DateTime.getAgo(historyDataset[position].created)
        Glide.with(context)
            .load(Profile.getUrl(historyDataset[position].profile))
            .error(R.drawable.ic_dummy_circle_crop)
            .apply(RequestOptions.circleCropTransform())
            .into(holder.ItemLayout.iv_profile)
            .clearOnDetach()
        if(position == 0) itemLayout.bg_line.background = null
    }

    override fun getItemCount(): Int {
        return historyDataset.size
    }
}