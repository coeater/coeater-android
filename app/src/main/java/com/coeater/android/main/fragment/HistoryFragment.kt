package com.coeater.android.main.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.coeater.android.R
import com.coeater.android.api.provideHistoryApi
import com.coeater.android.history.HistoryViewModel
import com.coeater.android.history.HistoryViewModelFactory

class HistoryFragment : Fragment() {

    private val viewModelFactory by lazy {
        HistoryViewModelFactory(
            provideHistoryApi(requireContext())
        )
    }

    lateinit var viewModel: HistoryViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setup()
    }

    private fun setup() {
        viewModel = ViewModelProviders.of(
            this, viewModelFactory
        )[HistoryViewModel::class.java]
    }
}