package com.coeater.android.invitation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coeater.android.api.MatchApi
import com.coeater.android.api.UserManageProvider
import com.coeater.android.model.AcceptedState
import com.coeater.android.model.HTTPResult
import com.coeater.android.model.RoomResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.HttpException

class InvitationViewModel(
    private val api: MatchApi,
    private val userManageProvider: UserManageProvider
) : ViewModel() {

    val roomCreateSuccess: MutableLiveData<RoomResponse> by lazy {
        MutableLiveData<RoomResponse>()
    }

    fun onCreate(id: Int?) {
        viewModelScope.launch(Dispatchers.IO) {
            val response = createRoom(id)
            when (response) {
                is HTTPResult.Success<RoomResponse> -> {
                    roomCreateSuccess.postValue(response.data)
                }
                is Error -> {

                    // TODO
                }
            }
        }
    }

    fun onStart(id: Int) {
        var trigger: Boolean = true
        viewModelScope.launch(Dispatchers.IO) {
            while (trigger) {
                delay(1000)
                val response = getRoom(id)
                when (response) {
                    is HTTPResult.Success<RoomResponse> -> {
                        val accepted: AcceptedState = response.data.accepted
                        if (accepted != AcceptedState.NOTCHECK) {
                            trigger = false
                            roomCreateSuccess.postValue(response.data)
                        }
                    }

                    is Error -> {
                        // TODO
                    }
                }
            }
        }
    }

    fun onAccept(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val response = acceptInvitation(id)
            when (response) {
                is HTTPResult.Success<RoomResponse> -> {
                    roomCreateSuccess.postValue(response.data)
                }
                is Error -> {

                    // TODO
                }
            }
        }
    }

    private suspend fun createRoom(id: Int? = null): HTTPResult<RoomResponse> {
        return try {
            val response = api.createRoom(id)
            HTTPResult.Success(response)
        } catch (e: HttpException) {
            HTTPResult.Error(e)
            // 룸이 존재하지 않는 것
        } catch (e: Exception) {
            // 네트워크가 불안정함
            HTTPResult.Error(e)
        }
    }

    private suspend fun getRoom(id: Int): HTTPResult<RoomResponse> {
        return try {
            val response = api.getRoom(id)
            HTTPResult.Success(response)
        } catch (e: HttpException) {
            HTTPResult.Error(e)
            // 룸이 존재하지 않는 것
        } catch (e: Exception) {
            // 네트워크가 불안정함
            HTTPResult.Error(e)
        }
    }

    private suspend fun acceptInvitation(id: Int): HTTPResult<RoomResponse> {
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
}
