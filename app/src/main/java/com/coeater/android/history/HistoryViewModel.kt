package com.coeater.android.history

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coeater.android.api.HistoryApi
import com.coeater.android.model.FriendsInfo
import com.coeater.android.model.HTTPResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException

class HistoryViewModel (
    private val api: HistoryApi
) : ViewModel() {

    val history: MutableLiveData<FriendsInfo> by lazy {
        MutableLiveData<FriendsInfo>()
    }

    fun fetchHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            when(val response = getHistory()) {
                is HTTPResult.Success<FriendsInfo> -> {
                    history.postValue(response.data)
                }
                is Error -> {}
            }
        }
    }

    private suspend fun getHistory(): HTTPResult<FriendsInfo> {
        return try {
            val response = api.getHistory()
            HTTPResult.Success(response)
        } catch (e: HttpException) {
            HTTPResult.Error(e)
        } catch (e: Exception) {
            HTTPResult.Error(e)
        }
    }
}