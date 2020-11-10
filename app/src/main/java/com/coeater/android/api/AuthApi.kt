package com.coeater.android.api

import android.media.Image
import com.coeater.android.model.UserManage
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*
import java.io.File

interface AuthApi {

    @Multipart
    @POST("users/register/")
    suspend fun register(
        @Part("uid") uid: RequestBody,
        @Part("nickname") nickname: RequestBody,
        @Part profile: MultipartBody.Part?
    ): UserManage

    @GET("users/register/")
    suspend fun login(
        @Query("uid") uid: String
    ): UserManage
}
