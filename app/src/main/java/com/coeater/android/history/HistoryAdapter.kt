package com.coeater.android.history

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.coeater.android.R
import com.coeater.android.model.DateTime
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
        if(position == 0) itemLayout.background = null
    }

    override fun getItemCount(): Int {
        return historyDataset.size
    }
}