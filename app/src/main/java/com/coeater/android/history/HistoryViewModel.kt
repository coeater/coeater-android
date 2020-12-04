package com.coeater.android.history

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coeater.android.api.HistoryApi
import com.coeater.android.model.DateTime
import com.coeater.android.model.FriendsInfo
import com.coeater.android.model.HTTPResult
import com.coeater.android.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.util.*

class HistoryViewModel (
    private val api: HistoryApi
) : ViewModel() {

    val history: MutableLiveData<FriendsInfo> by lazy {
        MutableLiveData<FriendsInfo>()
    }
    var fromDate : Date = Date(Date().year, Date().month-1, Date().date)
    var toDate : Date = Date()

    fun fetchHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            when(val response = getHistory(DateTime.periodToAPI(fromDate), DateTime.periodToAPI(toDate))) {
                is HTTPResult.Success<FriendsInfo> -> {
                    history.postValue(response.data)
                }
                is Error -> {}
            }
        }
    }

    fun saveHistory(id: Int?) {
        if(id == null) return
        viewModelScope.launch(Dispatchers.IO) {
            when(val response = postHistory(id)) {
                is HTTPResult.Success<User> -> {}
                is Error -> {}
            }
        }
    }

    private suspend fun getHistory(from : String, to : String): HTTPResult<FriendsInfo> {
        return try {
            val response = api.getHistory(from, to)
            HTTPResult.Success(response)
        } catch (e: HttpException) {
            HTTPResult.Error(e)
        } catch (e: Exception) {
            HTTPResult.Error(e)
        }
    }

    private suspend fun postHistory(id: Int): HTTPResult<User> {
        return try {
            val response = api.addHistory(id)
            HTTPResult.Success(response)
        } catch (e: HttpException) {
            putHistory(id)
        } catch (e: Exception) {
            HTTPResult.Error(e)
        }
    }

    private suspend fun putHistory(id: Int): HTTPResult<User> {
        return try {
            val response = api.putHistory(id)
            HTTPResult.Success(response)
        } catch (e: Exception) {
            HTTPResult.Error(e)
        }
    }
}