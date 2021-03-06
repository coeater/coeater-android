package com.coeater.android.main.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.coeater.android.R
import kotlinx.android.synthetic.main.view_main_button.view.*

class SearchButton : ConstraintLayout {

    constructor(context: Context?) : super(context) {
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr) {
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.view_search_button, this, true)
    }

    fun configure(icon: Int?, title: String, detail: String) {
        if (icon != null) iv_icon.setImageResource(icon)
        tv_title.text = title
        tv_description.text = detail
    }
}
