package com.coeater.android.api

import com.coeater.android.model.FriendsInfo
import com.coeater.android.model.User
import retrofit2.http.*

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
    @PUT("user/friend/")
    suspend fun rejectFriend(
        @Field("id") id : Int
    ): Unit
}
