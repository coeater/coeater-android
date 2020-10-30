package com.coeater.android.api

import com.coeater.android.model.FriendsInfo
import com.coeater.android.model.User
import retrofit2.http.Field
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface UserApi {

    @GET("users/friend/")
    suspend fun getFriends(): FriendsInfo

    @GET("users/friend/wait/")
    suspend fun getFriendRequests(): FriendsInfo

    @POST("users/friend/")
    suspend fun inviteFriend(
        @Field("code") code : String
    ): User

    @POST("users/friend/")
    suspend fun inviteFriend(
        @Field("id") id : Int
    ): User

    @PUT("user/friend/")
    suspend fun rejectFriend(
        @Field("id") id : Int
    )
}
