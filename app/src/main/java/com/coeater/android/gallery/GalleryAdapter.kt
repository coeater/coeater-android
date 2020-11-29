package com.coeater.android.gallery

import android.content.Context
import android.content.Intent
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.GridView
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.coeater.android.R
import com.coeater.android.model.Profile
import com.coeater.android.model.User
import kotlinx.android.synthetic.main.fragment_gallery.view.*
import kotlinx.android.synthetic.main.view_gallery_image_gridview_item.view.*

class GalleryAdapter(private val context: Context, private val item: List<User>) : BaseAdapter() {
//    enum class type { BLANK, IMAGE }
//    class ViewHolder (type: type, image: ImageView? = null){
//        val type = type
//        var image : ImageView? = image
//    }

    override fun getCount(): Int {
        return item.size
    }

    override fun getItem(position: Int): Any {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
//        var row = convertView
//        val holder: ViewHolder = when(row) {
//            null -> {
//                when(position/(parent as GridView).numColumns) {
//                    0 -> {
//                        row = LayoutInflater.from(context).inflate(R.layout.view_gallery_blank_gridview_item, parent, false)
//                        row.tag = ViewHolder(type.BLANK)
//                        row.tag as ViewHolder
//                    }
//                    else -> {
//                        row = LayoutInflater.from(context).inflate(R.layout.view_gallery_image_gridview_item, parent, false)
//                        row.tag = ViewHolder(type.IMAGE, (row as ConstraintLayout).iv_gallery)
//                        row.tag as ViewHolder
//                    }
//                }
//            }
//            else -> row.tag as ViewHolder
//        }
//        holder.image?.setImageResource(R.drawable.unnamed2)
//        return row!!
        return when(position/(parent as GridView).numColumns) {
            0 -> LayoutInflater.from(context).inflate(R.layout.view_gallery_blank_gridview_item, parent, false)
            else -> {
                val row = LayoutInflater.from(context).inflate(R.layout.view_gallery_image_gridview_item, parent, false) as ConstraintLayout
                Glide.with(context)
                    .load(Profile.getUrl(item[position].profile))
                    .into(row.iv_gallery)
                    .clearOnDetach()
                row.setOnClickListener { openGallery(position) }
                row
            }
        }
    }

    private fun openGallery(position : Int) {
        val intent = Intent(context, GalleryActivity::class.java)
        intent.putExtra("user", item[position])
        intent.putExtra("gallery", item[position])
        context.startActivity(intent)
    }
}