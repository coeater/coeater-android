package com.coeater.android.splash

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coeater.android.api.AuthApi
import com.coeater.android.api.UserManageProvider
import com.coeater.android.model.Result
import com.coeater.android.model.UserManage
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.iid.FirebaseInstanceId
import java.lang.Error
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException

class SplashViewModel(
    private val api: AuthApi,
    private val userManageProvider: UserManageProvider
) : ViewModel() {

    val isLoginSuccess: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    fun onCreate() {
        viewModelScope.launch(Dispatchers.IO) {
            val instanceId = getInstanceId()
            when (val myInfo = getMyInfo(instanceId)) {
                is Result.Success<UserManage> -> {
                    userManageProvider.updateUserManage(myInfo.data)
                    isLoginSuccess.postValue(true)
                }
                is Error -> {
                    isLoginSuccess.postValue(false)
                }
            }
        }
    }

    private fun getInstanceId(): String {
        return FirebaseInstanceId.getInstance().id
    }

    private suspend fun getMyInfo(uid: String): Result<UserManage> {
        return try {
            val response = api.register(uid, uid)
            Result.Success(response)
        } catch (e: HttpException) {
            login(uid)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    private suspend fun login(uid: String): Result<UserManage> {
        return try {
            val response = api.login(uid)
            Result.Success(response)
        } catch (e: HttpException) {
            Result.Error(e)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
