package com.coeater.android.splash

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coeater.android.api.AuthApi
import com.coeater.android.api.UserManageProvider
import com.coeater.android.model.HTTPResult
import com.coeater.android.model.UserManage
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException
import java.io.File

class SplashViewModel(
    private val api: AuthApi,
    private val userManageProvider: UserManageProvider
) : ViewModel() {

    val isLoginSuccess: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }
    val isInitialLogin: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }
    var err: Exception? = null

    fun onCreate() {
        viewModelScope.launch(Dispatchers.IO) {
            val instanceId = getInstanceId()
            when (val myInfo = login(instanceId)) {
                is HTTPResult.Success<UserManage> -> {
                    userManageProvider.updateUserManage(myInfo.data)
                    isLoginSuccess.postValue(true)
                }
                is HTTPResult.Error -> {
                    when (myInfo.exception) {
                        is HttpException -> {
                            isInitialLogin.postValue(true)
                        }
                        is Exception -> {
                            err = myInfo.exception
                            isLoginSuccess.postValue(false)
                        }
                    }
                }
            }
        }
    }

    fun setMyInfo(nickname: String, profile: File?) {
        viewModelScope.launch(Dispatchers.IO) {
            when (val myInfo = register(nickname, profile)) {
                is HTTPResult.Success<UserManage> -> {
                    userManageProvider.updateUserManage(myInfo.data)
                    isLoginSuccess.postValue(true)
                }
                is HTTPResult.Error -> {
                    err = myInfo.exception
                    isLoginSuccess.postValue(false)
                }
            }
        }
    }

    private fun getInstanceId(): String {
        return FirebaseInstanceId.getInstance().id
    }

    private suspend fun register(nickname: String, profile: File?): HTTPResult<UserManage> {
        val requestUid = RequestBody.create("multipart/from-data".toMediaTypeOrNull(), getInstanceId())
        val requestNickname = RequestBody.create("multipart/from-data".toMediaTypeOrNull(), nickname)
        var requestProfile: RequestBody
        var profileBody: MultipartBody.Part?
        if(profile == null) profileBody = null
        else {
            requestProfile = RequestBody.create("multipart/from-data".toMediaTypeOrNull(), profile!!)
            profileBody = MultipartBody.Part.createFormData("profile", profile.name, requestProfile )
        }

        return try {
            val response = api.register(requestUid, requestNickname, profileBody)
            HTTPResult.Success(response)
        } catch (e: HttpException) {
            HTTPResult.Error(e)
        } catch (e: Exception) {
            HTTPResult.Error(e)
        }
    }

    private suspend fun login(uid: String): HTTPResult<UserManage> {
        return try {
            val response = api.login(uid)
            HTTPResult.Success(response)
        } catch (e: HttpException) {
            HTTPResult.Error(e)
        } catch (e: Exception) {
            HTTPResult.Error(e)
        }
    }
}
