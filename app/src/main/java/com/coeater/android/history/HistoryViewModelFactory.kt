package com.coeater.android.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.coeater.android.api.HistoryApi

class HistoryViewModelFactory (
    private val api: HistoryApi
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return HistoryViewModel(api) as T
    }
}