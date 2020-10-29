package com.coeater.android.model

sealed class HTTPResult<out R> {
    data class Success<out T>(val data: T) : HTTPResult<T>()
    data class Error(val exception: Exception) : HTTPResult<Nothing>()
}
