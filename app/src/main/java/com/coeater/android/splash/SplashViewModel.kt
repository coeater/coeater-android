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
                        is Exception -> { isLoginSuccess.postValue(false) }
                    }
                }
            }
        }
    }

    fun setMyInfo(nickname: String, profile: File? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            when (val myInfo = register(nickname, profile)) {
                is HTTPResult.Success<UserManage> -> {
                    userManageProvider.updateUserManage(myInfo.data)
                    isLoginSuccess.postValue(true)
                }
                is HTTPResult.Error -> {
                    isLoginSuccess.postValue(false)
                }
            }
        }
    }

    private fun getInstanceId(): String {
        return FirebaseInstanceId.getInstance().id
    }

    private suspend fun register(nickname: String, profile: File? = null): HTTPResult<UserManage> {
        return try {
            val response = api.register(getInstanceId(), nickname, profile)
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
