package com.coeater.android.tutorial

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.adapter.FragmentViewHolder
import com.coeater.android.R
import kotlinx.android.synthetic.main.activity_main.*

class TutorialPageAdapter(val closeClick: () -> Unit, fa: FragmentActivity) : FragmentStateAdapter(fa) {

    class FragmentZero : TutorialFragment() {

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            this.configure(R.drawable.tutorial0, false) {}
        }
    }

    class FragmentOne : TutorialFragment() {

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            this.configure(R.drawable.tutorial1, false) {}
        }
    }

    class FragmentTwo : TutorialFragment() {
        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            this.configure(R.drawable.tutorial2, false) {}
        }
    }

    class FragmentThree(val closeClick: () -> Unit) : TutorialFragment() {

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            this.configure(R.drawable.tutorial3, true) {
                closeClick()
            }
        }

    }

    override fun getItemCount(): Int = 4


    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FragmentZero()
            1 -> FragmentOne()
            2 -> FragmentTwo()
            3 -> FragmentThree(closeClick)
            else -> Fragment()
        }
    }


}