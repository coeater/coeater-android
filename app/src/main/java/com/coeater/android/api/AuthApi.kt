package com.coeater.android.api

import android.media.Image
import com.coeater.android.model.UserManage
import retrofit2.http.*
import java.io.File

interface AuthApi {

    @FormUrlEncoded
    @POST("users/register/")
    suspend fun register(
        @Field("uid") uid: String,
        @Field("nickname") nickname: String,
        @Field("profile") profile: File?
    ): UserManage

    @GET("users/register/")
    suspend fun login(
        @Query("uid") uid: String
    ): UserManage
}
