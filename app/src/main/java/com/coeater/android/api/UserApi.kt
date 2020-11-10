package com.coeater.android.api

import com.coeater.android.model.FriendsInfo
import com.coeater.android.model.User
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*
import java.io.File

interface UserApi {

    @GET("users/friend/")
    suspend fun getFriends(): FriendsInfo

    @GET("users/friend/wait/")
    suspend fun getFriendRequests(): FriendsInfo

    @FormUrlEncoded
    @POST("users/friend/")
    suspend fun inviteFriend(
        @Field("code") code : String
    ): User

    @FormUrlEncoded
    @POST("users/friend/")
    suspend fun inviteFriend(
        @Field("id") id : Int
    ): User

    @FormUrlEncoded
    @PUT("users/friend/")
    suspend fun rejectFriend(
        @Field("id") id : Int
    ): Unit

    @FormUrlEncoded
    @PUT("users/{id}/")
    suspend fun setNickname(
        @Path("id") id : Int,
        @Field("nickname") nickname : String
    ): User

    @Multipart
    @PUT("users/{id}/")
    suspend fun setProfile(
        @Path("id") id : Int,
        @Part("nickname") nickname: RequestBody,
        @Part profile: MultipartBody.Part?
    ): User
}
