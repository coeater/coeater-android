package com.coeater.android.join

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coeater.android.api.MatchApi
import com.coeater.android.api.UserManageProvider
import com.coeater.android.model.HTTPResult
import com.coeater.android.model.RoomResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException

class JoinViewModel(
    private val api: MatchApi,
    private val userManageProvider: UserManageProvider
) : ViewModel() {

    val roomCreateSuccess: MutableLiveData<RoomResponse> by lazy {
        MutableLiveData<RoomResponse>()
    }

    val roomCreateFail: MutableLiveData<Unit> by lazy {
        MutableLiveData<Unit>()
    }

    fun onCreate() {
        viewModelScope.launch(Dispatchers.IO) {
        }
    }

    fun invitation(code: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val response = getRoom(code)
            when (response) {
                is HTTPResult.Success<RoomResponse> -> {
                    val result = accept(response.data.id)
                    when (result) {
                        is HTTPResult.Success<RoomResponse> -> {
                            roomCreateSuccess.postValue(result.data)
                        }
                    }
                }
                is HTTPResult.Error -> {
                    roomCreateFail.postValue(Unit)
                }
            }
        }
    }

    private suspend fun accept(id: Int): HTTPResult<RoomResponse> {
        return try {
            val response = api.acceptInvitation(id)
            HTTPResult.Success(response)
        } catch (e: HttpException) {
            HTTPResult.Error(e)
            // 룸이 존재하지 않는 것
        } catch (e: Exception) {
            // 네트워크가 불안정함
            HTTPResult.Error(e)
        }
    }
    private suspend fun getRoom(code: String): HTTPResult<RoomResponse> {
        return try {
            val response = api.joinRoom(code)
            HTTPResult.Success(response)
        } catch (e: HttpException) {
            HTTPResult.Error(e)
            // 룸이 존재하지 않는 것
        } catch (e: Exception) {
            // 네트워크가 불안정함
            HTTPResult.Error(e)
        }
    }
}
