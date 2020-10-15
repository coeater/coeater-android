package com.coeater.android.api

import com.coeater.android.model.UserManage
import retrofit2.http.*

interface AuthApi {

    @FormUrlEncoded
    @POST("users/register/")
    suspend fun register(
        @Field("uid") uid: String,
        @Field("nickname") nickname: String
    ): UserManage

    @GET("users/register/")
    suspend fun login(
        @Query("uid") uid: String
    ): UserManage
}
