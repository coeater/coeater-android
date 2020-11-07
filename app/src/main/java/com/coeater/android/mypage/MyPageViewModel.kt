package com.coeater.android.mypage

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coeater.android.api.UserApi
import com.coeater.android.model.FriendsInfo
import com.coeater.android.model.HTTPResult
import com.coeater.android.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException

class MyPageViewModel(
    private val api: UserApi
) : ViewModel() {
    val requests: MutableLiveData<FriendsInfo> by lazy {
        MutableLiveData<FriendsInfo>()
    }
    var id: Int? = null

    fun fetchRequest() {
        viewModelScope.launch(Dispatchers.IO) {
            when(val response = getRequests()) {
                is HTTPResult.Success<FriendsInfo> -> {
                    requests.postValue(response.data)
                    id = response.data.owner.id
                }
                is Error -> {
                    //TODO error message
                }
            }
        }
    }

    fun accept(id : Int) {
        viewModelScope.launch(Dispatchers.IO) {
            when(val response = acceptFriend(id)) {
                is HTTPResult.Success<User> -> {

                }
                is Error -> {
                    //TODO error message
                }
            }
        }
    }

    fun reject(id : Int) {
        viewModelScope.launch(Dispatchers.IO) {
            when(val response = rejectFriend(id)) {
                is HTTPResult.Success<Unit> -> {

                }
                is Error -> {
                    //TODO error message
                }
            }
        }
    }

    fun changeNickname(nickname : String) {
        viewModelScope.launch(Dispatchers.IO) {
            if(id == null) {
                //TODO error message}
            }
            else when(val response = setNickname(id!!, nickname)) {
                is HTTPResult.Success<User> -> {
                    when(val response = getRequests()) {
                        is HTTPResult.Success<FriendsInfo> -> {
                            requests.postValue(response.data)
                        }
                        is Error -> {}
                    }
                }
                is Error -> {
                    //TODO error message
                }
            }
        }
    }

    private suspend fun getRequests(): HTTPResult<FriendsInfo> {
        return try {
            val response = api.getFriendRequests()
            HTTPResult.Success(response)
        } catch (e: HttpException) {
            HTTPResult.Error(e)
        } catch (e: Exception) {
            HTTPResult.Error(e)
        }
    }

    private suspend fun acceptFriend(id: Int): HTTPResult<User> {
        return try {
            val response = api.inviteFriend(id)
            HTTPResult.Success(response)
        } catch (e: HttpException) {
            HTTPResult.Error(e)
        } catch (e: Exception) {
            HTTPResult.Error(e)
        }
    }

    private suspend fun rejectFriend(id: Int): HTTPResult<Unit> {
        return try {
            val response = api.rejectFriend(id)
            HTTPResult.Success(response)
        } catch (e: HttpException) {
            HTTPResult.Error(e)
        } catch (e: Exception) {
            HTTPResult.Error(e)
        }
    }

    private suspend fun setNickname(id: Int, nickname: String): HTTPResult<User> {
        return try {
            val response = api.setNickname(id, nickname)
            HTTPResult.Success(response)
        } catch (e: HttpException) {
            HTTPResult.Error(e)
        } catch (e: Exception) {
            HTTPResult.Error(e)
        }
    }
}