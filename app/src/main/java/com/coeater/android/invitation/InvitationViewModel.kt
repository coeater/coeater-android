package com.coeater.android.invitation


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coeater.android.api.MatchApi
import com.coeater.android.api.UserManageProvider
import com.coeater.android.model.FriendsInfo
import com.coeater.android.model.RoomResponse
import com.coeater.android.model.HTTPResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException

class InvitationViewModel(
    private val api: MatchApi,
    private val userManageProvider: UserManageProvider
) : ViewModel() {

    val roomCreateSuccess: MutableLiveData<RoomResponse> by lazy {
        MutableLiveData<RoomResponse>()
    }

    fun onCreate() {
        viewModelScope.launch(Dispatchers.IO) {
            val response = createRoom()
            when (response) {
                is HTTPResult.Success<RoomResponse> -> {
                    roomCreateSuccess.postValue(response.data)
                }
                is Error -> {

                    //TODO
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
}
