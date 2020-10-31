package com.coeater.android.friends

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coeater.android.api.UserApi
import com.coeater.android.model.HTTPResult
import com.coeater.android.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException

class AddFriendViewModel (
    private val api: UserApi
) : ViewModel() {

    val invitee: MutableLiveData<User> by lazy {
        MutableLiveData<User>()
    }

    fun invite(code : String) {
        viewModelScope.launch(Dispatchers.IO) {
            when (val response = inviteFriend(code)) {
                is HTTPResult.Success<User> -> {
                    invitee.postValue(response.data)
                }
                is Error -> {
                    //TODO error message
                }
            }
        }
    }

    private suspend fun inviteFriend(code : String): HTTPResult<User> {
        return try {
            val response = api.inviteFriend(code)
            HTTPResult.Success(response)
        } catch (e: HttpException) {
            HTTPResult.Error(e)
        } catch (e: Exception) {
            HTTPResult.Error(e)
        }
    }
}