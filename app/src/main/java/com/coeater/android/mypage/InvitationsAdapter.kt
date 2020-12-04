package com.coeater.android.mypage

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.coeater.android.R
import com.coeater.android.invitation.InvitationViewModel
import com.coeater.android.matching.MatchingActivity
import com.coeater.android.matching.MatchingInput
import com.coeater.android.matching.MatchingMode
import com.coeater.android.matching.MatchingViewModel
import com.coeater.android.model.Profile
import com.coeater.android.model.RoomResponse
import com.coeater.android.model.User
import kotlinx.android.synthetic.main.view_friend_requests_recycler_item.view.*

class InvitationsAdapter(private val viewModel : MatchingViewModel, private val context : Context, private val requestsDataset : List<RoomResponse>) : RecyclerView.Adapter<InvitationsAdapter.RequestsViewHolder>() {

    class RequestsViewHolder(val ItemLayout : ConstraintLayout) : RecyclerView.ViewHolder(ItemLayout)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestsViewHolder {
        val itemLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_invitations_recycler_item, parent, false) as ConstraintLayout

        return RequestsViewHolder(itemLayout)
    }

    override fun onBindViewHolder(holder: RequestsViewHolder, position: Int) {
        holder.ItemLayout.tv_nickname.text = requestsDataset[position].owner?.nickname
        Glide.with(context)
            .load(Profile.getUrl(requestsDataset[position].owner?.profile))
            .error(R.drawable.ic_dummy_circle_crop)
            .apply(RequestOptions.circleCropTransform())
            .into(holder.ItemLayout.iv_profile)
            .clearOnDetach()

        holder.ItemLayout.button_accept.setOnClickListener { acceptRequest(position) }
        holder.ItemLayout.button_close.setOnClickListener { rejectRequest(position) }
    }

    private fun acceptRequest(position: Int) {
        val invitation = requestsDataset[position]
        val intent = Intent(context, MatchingActivity::class.java).apply {
            val output = MatchingInput(
                MatchingMode.FRIEND_INVITEE,
                invitation.id,
                invitation.owner.nickname,
                invitation.owner.profile ?: ""
            )
            this.putExtra(MatchingActivity.MATCH_INPUT, output)
        }
        context.startActivity(intent)
    }

    private fun rejectRequest(position: Int) {
        viewModel.onClickReject(requestsDataset[position].id)
        viewModel.fetchInvitations()
    }

    override fun getItemCount(): Int {
        return requestsDataset.size
    }
}