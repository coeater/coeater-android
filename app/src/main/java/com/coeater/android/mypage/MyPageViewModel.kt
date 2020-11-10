package com.coeater.android.mypage

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coeater.android.api.UserApi
import com.coeater.android.model.FriendsInfo
import com.coeater.android.model.HTTPResult
import com.coeater.android.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException
import java.io.File

class MyPageViewModel(
    private val api: UserApi
) : ViewModel() {
    val requests: MutableLiveData<FriendsInfo> by lazy {
        MutableLiveData<FriendsInfo>()
    }
    val myInfo: MutableLiveData<User> by lazy {
        MutableLiveData<User>()
    }
    val isEditSuccess: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    fun fetchRequest() {
        viewModelScope.launch(Dispatchers.IO) {
            when(val response = getRequests()) {
                is HTTPResult.Success<FriendsInfo> -> {
                    requests.postValue(response.data)
                    myInfo.postValue(response.data.owner)
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

    fun editProfile(nickname: String, profile: File?) {
        viewModelScope.launch(Dispatchers.IO) {
            when (val myInfo = setProfile(nickname, profile)) {
                is HTTPResult.Success<User> -> {
                    isEditSuccess.postValue(true)
                }
                is HTTPResult.Error -> {
                    isEditSuccess.postValue(false)
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

    private suspend fun setProfile(nickname: String, profile: File?): HTTPResult<User> {
        val requestNickname = RequestBody.create("multipart/from-data".toMediaTypeOrNull(), nickname)
        var requestProfile: RequestBody
        var profileBody: MultipartBody.Part?
        if(profile == null) profileBody = null
        else {
            requestProfile = RequestBody.create("multipart/from-data".toMediaTypeOrNull(), profile!!)
            profileBody = MultipartBody.Part.createFormData("profile", profile.name, requestProfile )
        }

        return try {
            val response = api.setProfile(myInfo.value!!.id, requestNickname, profileBody)
            HTTPResult.Success(response)
        } catch (e: HttpException) {
            HTTPResult.Error(e)
        } catch (e: Exception) {
            HTTPResult.Error(e)
        }
    }
}