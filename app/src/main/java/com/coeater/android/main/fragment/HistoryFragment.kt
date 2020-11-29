package com.coeater.android.main.fragment

import android.app.DatePickerDialog
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coeater.android.R
import com.coeater.android.history.HistoryAdapter
import com.coeater.android.history.HistoryViewModel
import kotlinx.android.synthetic.main.fragment_history.*
import java.util.*

class HistoryFragment : Fragment() {

    val viewModel : HistoryViewModel by activityViewModels()

    enum class EditPeriod {
        FROM, TO
    }

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
        tv_period_from.setOnClickListener {
            openDatePickerDialog(viewModel.fromDate, EditPeriod.FROM)
        }
        tv_period_to.setOnClickListener {
            openDatePickerDialog(viewModel.toDate, EditPeriod.TO)
        }
    }

    private fun openDatePickerDialog(date: Date, type: EditPeriod) {
        val calendar = Calendar.getInstance()
        calendar.time = date
        val datePickerListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, monthOfYear)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            when(type) {
                EditPeriod.FROM -> setPeriod(calendar.time, viewModel.toDate)
                EditPeriod.TO -> setPeriod(viewModel.fromDate, calendar.time)
            }
        }

        val dpd = DatePickerDialog(requireContext(), R.style.MySpinnerDatePickerStyle, datePickerListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        when(type) {
            EditPeriod.FROM -> dpd.datePicker.maxDate = viewModel.toDate.time
            EditPeriod.TO -> dpd.datePicker.minDate = viewModel.fromDate.time
        }
        dpd.show()
    }

    private fun setPeriod(from: Date = Date(Date().year, Date().month-1, Date().date), to: Date = Date()) {
        viewModel.toDate = to
        viewModel.fromDate = from
        val sdf = SimpleDateFormat("yy.MM.dd")
        tv_period_from.text = sdf.format(from)
        tv_period_to.text = sdf.format(to)
    }
}