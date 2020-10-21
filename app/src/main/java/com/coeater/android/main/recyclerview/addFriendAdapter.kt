package com.coeater.android.main.recyclerview

import android.content.Context
import android.util.TypedValue
import androidx.constraintlayout.widget.ConstraintLayout
import com.coeater.android.R
import com.coeater.android.model.User
import kotlinx.android.synthetic.main.view_friends_recycler_item.view.*

class addFriendAdapter(private val context : Context, private val friendsDataset : List<User>) : FriendsAdapter(context, friendsDataset) {
    override fun getItemCount(): Int {
        return super.getItemCount()+1
    }

    override fun onBindViewHolder(holder: FriendsViewHolder, position: Int) {
        if(position == 0) {
            holder.ItemLayout.iv_profile.setImageResource(R.drawable.add_circle_outline_24_px)
            holder.ItemLayout.iv_profile.layoutParams.width =
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40F, context.resources.displayMetrics).toInt()
            holder.ItemLayout.iv_profile.layoutParams.height =
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40F, context.resources.displayMetrics).toInt()
            val layoutParams : ConstraintLayout.LayoutParams = holder.ItemLayout.iv_profile.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.topMargin = 0;
            holder.ItemLayout.iv_profile.layoutParams = layoutParams
            holder.ItemLayout.tv_name.text = "Add friend"
        }
        else super.onBindViewHolder(holder, position-1)
    }
}