package com.coeater.android.main.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.coeater.android.R
import com.coeater.android.webrtc.CallActivity
import kotlinx.android.synthetic.main.fragment_oneonone.*

class OneOnOneFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_oneonone, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setup()
    }

    fun setup() {
        main_button_invite_friend.configure("Invite Friend", "Invite your friend with code")
        main_button_search_friend.configure("Look for Co-eater", "Find another Co-eater to eat with you")
        main_button_invite_friend.setOnClickListener {
            val intent = Intent(getActivity(), CallActivity::class.java);
           startActivity(intent)
        }
    }
}
