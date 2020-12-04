package com.coeater.android.matching

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
import okhttp3.internal.wait
import retrofit2.HttpException

class MatchingViewModel(
    private val api: MatchApi,
    private val userManageProvider: UserManageProvider
) : ViewModel() {

    val matched: MutableLiveData<RoomResponse> by lazy {
        MutableLiveData<RoomResponse>()
    }

    val notMatched: MutableLiveData<RoomResponse> by lazy {
        MutableLiveData<RoomResponse>()
    }

    val matchError: MutableLiveData<Unit> by lazy {
        MutableLiveData<Unit>()
    }

    val matchRejected: MutableLiveData<Unit> by lazy {
        MutableLiveData<Unit>()
    }

    val invitations: MutableLiveData<List<RoomResponse>> by lazy {
        MutableLiveData<List<RoomResponse>>()
    }

    fun onCreate() {
        viewModelScope.launch(Dispatchers.IO) {
        }
    }

    fun onClickAccept(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val response = acceptInvitation(id)
            when (response) {
                is HTTPResult.Success<RoomResponse> -> {
                    matched.postValue(response.data)
                }
                is HTTPResult.Error -> {
                    matchError.postValue(Unit)
                }
            }
        }
    }

    fun onClickReject(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val response = discardRoom(id)
            when (response) {
                is HTTPResult.Success<RoomResponse> -> {
                    matchRejected.postValue(Unit)
                }
                is HTTPResult.Error -> {
                    matchRejected.postValue(Unit)
                }
            }
        }
    }

    fun waitToBeGetAccept(id: Int) {
        var trigger: Boolean = true
        viewModelScope.launch(Dispatchers.IO) {
            while (trigger) {
                delay(1000)
                val response = getRoom(id)
                when (response) {
                    is HTTPResult.Success<RoomResponse> -> {
                        val accepted: AcceptedState = response.data.accepted
                        val checked: Boolean = response.data.checked
                        if (accepted == AcceptedState.ACCEPTED) {
                            trigger = false
                            acceptInvitation(id)
                            waitToBeMatched(id)
                        }
                        else if (accepted == AcceptedState.DECLINE) {
                            trigger = false
                            notMatched.postValue(response.data)
                        }
                    }
                    is HTTPResult.Error -> {
                        trigger = false
                        matchRejected.postValue(Unit)
                    }
                }
            }
        }
    }

    fun waitToBeMatched(id: Int) {
        var trigger: Boolean = true
        viewModelScope.launch(Dispatchers.IO) {
            while (trigger) {
                delay(1000)
                val response = getRoom(id)
                when (response) {
                    is HTTPResult.Success<RoomResponse> -> {
                        val accepted: AcceptedState = response.data.accepted
                        val checked: Boolean = response.data.checked
                        if (accepted == AcceptedState.ACCEPTED && checked) {
                            trigger = false
                            matched.postValue(response.data)
                        }
                        else if (accepted == AcceptedState.DECLINE) {
                            trigger = false
                            notMatched.postValue(response.data)
                        }
                    }
                    is HTTPResult.Error -> {
                        trigger = false
                        matchRejected.postValue(Unit)
                    }
                }
            }
        }
    }

    fun fetchInvitations(){
        viewModelScope.launch(Dispatchers.IO) {
            val response = getInvitations()
            when (response) {
                is HTTPResult.Success<List<RoomResponse>> -> {
                    invitations.postValue(response.data)
                }
                is Error -> {
                    // TODO
                }
            }
        }
    }

    private suspend fun getInvitations(): HTTPResult<List<RoomResponse>> {
        return try {
            val response = api.getInvitations()
            HTTPResult.Success(response)
        } catch (e: HttpException) {
            HTTPResult.Error(e)
        } catch (e: Exception) {
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

    private suspend fun discardRoom(id: Int): HTTPResult<RoomResponse> {
        return try {
            val response = api.rejectInvitation(id)
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
