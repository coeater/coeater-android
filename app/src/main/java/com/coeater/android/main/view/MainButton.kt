package com.coeater.android.main.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.coeater.android.R
import kotlinx.android.synthetic.main.view_main_button.view.*

class MainButton : ConstraintLayout {

    constructor(context: Context?) : super(context) {
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr) {
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.view_main_button, this, true)
    }

    fun configure(title: String, detail: String) {
        tv_title.text = title
        tv_description.text = detail
    }
}
