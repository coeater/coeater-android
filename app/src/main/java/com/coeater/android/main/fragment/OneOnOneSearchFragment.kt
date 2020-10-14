package com.coeater.android.main.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.coeater.android.R
import kotlinx.android.synthetic.main.fragment_oneonone_search.*

class OneOnOneSearchFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_oneonone_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setup()
    }

    fun setup() {
        search_button_menu.configure(R.drawable.food_bank_24_px, "Menu Preference", "Doesn't care")
        search_button_mood.configure(R.drawable.mood_24_px, "Mood Preference", "Chatty")
        search_button_time.configure(R.drawable.access_time_24_px, "Time Preference", "20 ~ 30 min")
    }
}
