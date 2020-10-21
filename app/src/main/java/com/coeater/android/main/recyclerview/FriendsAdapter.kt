package com.coeater.android.main.recyclerview

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.coeater.android.R
import com.coeater.android.model.User
import kotlinx.android.synthetic.main.view_friends_recycler_item.view.*

open class FriendsAdapter(private val context : Context, private val friendsDataset : List<User>) : RecyclerView.Adapter<FriendsAdapter.FriendsViewHolder>() {

    class FriendsViewHolder(val ItemLayout : ConstraintLayout) : RecyclerView.ViewHolder(ItemLayout)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendsViewHolder {
        val itemLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_friends_recycler_item, parent, false) as ConstraintLayout

        return FriendsViewHolder(itemLayout)
    }

    override fun getItemCount(): Int {
        return friendsDataset.size
    }

    override fun onBindViewHolder(holder : FriendsViewHolder, position: Int) {
        holder.ItemLayout.tv_name.text = friendsDataset[position].nickname
        Glide.with(context)
            .load(R.drawable.ic_dummy_profile)
            .apply(RequestOptions.circleCropTransform())
            .into(holder.ItemLayout.iv_profile)
            .clearOnDetach()
    }
}