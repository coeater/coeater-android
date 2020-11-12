package com.coeater.android.mypage

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.coeater.android.R
import com.coeater.android.model.User
import kotlinx.android.synthetic.main.view_friend_requests_recycler_item.view.*

class RequestsAdapter(private val viewModel : MyPageViewModel, private val context : Context, private val requestsDataset : List<User>) : RecyclerView.Adapter<RequestsAdapter.RequestsViewHolder>() {

    class RequestsViewHolder(val ItemLayout : ConstraintLayout) : RecyclerView.ViewHolder(ItemLayout)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestsViewHolder {
        val itemLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_friend_requests_recycler_item, parent, false) as ConstraintLayout

        return RequestsViewHolder(itemLayout)
    }

    override fun onBindViewHolder(holder: RequestsViewHolder, position: Int) {
        holder.ItemLayout.tv_nickname.text = requestsDataset[position].nickname
        holder.ItemLayout.tv_code.text = "Code : " + requestsDataset[position].code
        Glide.with(context)
            .load(R.drawable.ic_dummy_profile)
            .apply(RequestOptions.circleCropTransform())
            .into(holder.ItemLayout.iv_profile)
            .clearOnDetach()

        holder.ItemLayout.button_accept.setOnClickListener { acceptRequest(position) }
        holder.ItemLayout.button_close.setOnClickListener { rejectRequest(position) }
    }

    private fun acceptRequest(position: Int) {
        viewModel.accept(requestsDataset[position].id)
        viewModel.fetchRequest()
    }

    private fun rejectRequest(position: Int) {
        viewModel.reject(requestsDataset[position].id)
        viewModel.fetchRequest()
    }

    override fun getItemCount(): Int {
        return requestsDataset.size
    }
}