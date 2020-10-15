package com.coeater.android.splash

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coeater.android.api.AuthApi
import com.coeater.android.model.UserManage
import java.lang.Error
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException

sealed class Result<out R> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
}

class SplashViewModel(private val api: AuthApi) : ViewModel() {

    // Create a LiveData with a String
    val isLoginSuccess: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    // ViewModel의 생성과 함께 API 호출
    init {
    }

    fun onCreate() {
        viewModelScope.launch(Dispatchers.IO) {
           val myInfo = getMyInfo()
            when (myInfo) {
                is Result.Success<UserManage> -> {
                    myInfo.data
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
