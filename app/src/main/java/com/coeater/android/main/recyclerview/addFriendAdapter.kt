package com.coeater.android.main.recyclerview

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.coeater.android.R
import com.coeater.android.friends.AddFriendActivity
import com.coeater.android.invitation.InvitationViewModel
import com.coeater.android.model.User
import kotlinx.android.synthetic.main.view_friends_add_recycler_item.view.*

class AddFriendAdapter(private val viewModel : InvitationViewModel, private val context : Context, private val friendsDataset : List<User>) : FriendsAdapter(viewModel, context, friendsDataset) {

    companion object ViewType {
        const val DEFAULT = 0;
        const val ADD = 1;
    }

    class AddFriendsViewHolder(val ItemLayout : ConstraintLayout) : RecyclerView.ViewHolder(ItemLayout)

    override fun getItemCount(): Int {
        return super.getItemCount()+1
    }

    override fun getItemViewType(position: Int): Int {
        return when(position) {
            0 -> ADD;
            else -> DEFAULT;
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            ADD -> {
                val itemLayout = LayoutInflater.from(parent.context)
                    .inflate(R.layout.view_friends_add_recycler_item, parent, false) as ConstraintLayout

                AddFriendsViewHolder(itemLayout)
            }
            else -> super.onCreateViewHolder(parent, viewType)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is AddFriendsViewHolder -> {
                holder.ItemLayout.iv_add.setOnClickListener { startAddFriendActivity() }
            }
            else -> super.onBindViewHolder(holder, position-1)
        }
    }

    private fun startAddFriendActivity() {
        val intent : Intent = Intent(context, AddFriendActivity::class.java)
        context.startActivity(intent)
    }
}