package com.coeater.android.main.recyclerview

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.coeater.android.R
import com.coeater.android.invitation.InvitationViewModel
import com.coeater.android.matching.MatchingActivity
import com.coeater.android.matching.MatchingInput
import com.coeater.android.matching.MatchingMode
import com.coeater.android.model.AcceptedState
import com.coeater.android.model.Profile
import com.coeater.android.model.RoomResponse
import com.coeater.android.model.User
import kotlinx.android.synthetic.main.view_friends_recycler_item.view.*

open class FriendsAdapter(private val viewModel : InvitationViewModel, private val context : Context, private val friendsDataset : List<User>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class FriendsViewHolder(val ItemLayout : ConstraintLayout) : RecyclerView.ViewHolder(ItemLayout)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_friends_recycler_item, parent, false) as ConstraintLayout

        return FriendsViewHolder(itemLayout)
    }

    override fun getItemCount(): Int {
        return friendsDataset.size
    }

    override fun onBindViewHolder(holder : RecyclerView.ViewHolder, position: Int) {
        val friend = friendsDataset[position]
        (holder as FriendsViewHolder).ItemLayout.tv_name.text = friend.nickname
        Glide.with(context)
            .load(Profile.getUrl(friend.profile))
            .error(R.drawable.ic_dummy_circle_crop)
            .apply(RequestOptions.circleCropTransform())
            .into(holder.ItemLayout.iv_profile)
            .clearOnDetach()

        holder.ItemLayout.iv_profile.setOnClickListener{ friend?.let {
            AlertDialog.Builder(context)
                .setMessage("Would you like to invite ${friend.nickname}?")
                .setPositiveButton("Yes",
                    DialogInterface.OnClickListener { dialog, id ->
                        sendInvitation(dialog, friend)
                    })
                .setNegativeButton("No",
                    DialogInterface.OnClickListener { dialog, id ->
                        dialog.dismiss()
                    })
                .create()
                .show()
        } }
    }

    private var roomId: Int? = null
    private fun sendInvitation(dialog: DialogInterface, friend: User) {
        viewModel.onCreate(friend.id)
        viewModel.roomCreateSuccess.observe(context as FragmentActivity, Observer<RoomResponse> {
            roomId = it.id
            if (it.accepted == AcceptedState.NOTCHECK) {
                val intent = Intent(context, MatchingActivity::class.java).apply {
                    val output = MatchingInput(
                        MatchingMode.FRIEND_INVITER,
                        it.id ?: 0,
                        friend.nickname,
                        friend.profile ?: ""
                    )
                    this.putExtra(MatchingActivity.MATCH_INPUT, output)
                }
                context.startActivity(intent)
            }
        })

        viewModel.expiredMatch.observe(context as FragmentActivity, Observer<Unit> {
            Log.i("어댑터", "expired")
            dialog.dismiss()
        })
    }
}