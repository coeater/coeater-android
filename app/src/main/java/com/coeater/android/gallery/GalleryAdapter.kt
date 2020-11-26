package com.coeater.android.gallery

import android.content.Context
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.GridView
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.coeater.android.R
import kotlinx.android.synthetic.main.fragment_gallery.view.*
import kotlinx.android.synthetic.main.view_gallery_image_gridview_item.view.*

class GalleryAdapter(private val context: Context, private val testSize: Int) : BaseAdapter() {
    enum class type { BLANK, IMAGE }
    class ViewHolder (type: type, image: ImageView? = null){
        val type = type
        var image : ImageView? = image
    }

    override fun getCount(): Int {
        return testSize
    }

    override fun getItem(position: Int): Any {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var row = convertView
        val holder: ViewHolder = when(row) {
            null -> {
                when(position/(parent as GridView).numColumns) {
                    0 -> {
                        row = LayoutInflater.from(context).inflate(R.layout.view_gallery_blank_gridview_item, parent, false)
                        row.tag = ViewHolder(type.BLANK)
                        row.tag as ViewHolder
                    }
                    else -> {
                        row = LayoutInflater.from(context).inflate(R.layout.view_gallery_image_gridview_item, parent, false)
                        row.tag = ViewHolder(type.IMAGE, (row as ConstraintLayout).iv_gallery)
                        row.tag as ViewHolder
                    }
                }
            }
            else -> row.tag as ViewHolder
        }
        holder.image?.setImageResource(R.drawable.unnamed2)
        return row!!
    }
}