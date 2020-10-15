package com.coeater.android.splash

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coeater.android.api.AuthApi
import com.coeater.android.api.UserManageProvider
import com.coeater.android.model.Result
import com.coeater.android.model.UserManage
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
            when (val myInfo = getMyInfo()) {
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

    private suspend fun getMyInfo(): Result<UserManage> {
        return try {
            val response = api.register("12345666", "testNickname")
            Result.Success(response)
        } catch (e: HttpException) {
            login()
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    private suspend fun login(): Result<UserManage> {
        return try {
            val response = api.login("12345666")
            Result.Success(response)
        } catch (e: HttpException) {
            Result.Error(e)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
