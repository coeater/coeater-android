package com.coeater.android.main.fragment

import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coeater.android.R
import com.coeater.android.api.provideHistoryApi
import com.coeater.android.history.HistoryAdapter
import com.coeater.android.history.HistoryViewModel
import com.coeater.android.history.HistoryViewModelFactory
import com.coeater.android.main.MainActivity
import com.coeater.android.model.DateTime
import kotlinx.android.synthetic.main.fragment_history.*
import java.util.*

class HistoryFragment : Fragment() {

    val viewModel : HistoryViewModel by activityViewModels()

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
        setPeriod()
        viewModel.history.observe(requireActivity(), Observer { history ->
            rv_history.apply {
                adapter = HistoryAdapter(context, history.histories)
                layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            }
        })
    }

    private fun setPeriod() {
        val to = Date()
        viewModel.toDate = to
        val from = Date(to.year, to.month-1, to.date)
        viewModel.fromDate = from
        val sdf = SimpleDateFormat("yy.MM.dd")
        tv_period.text = "${sdf.format(from)} ~ ${sdf.format(to)}"
    }
}