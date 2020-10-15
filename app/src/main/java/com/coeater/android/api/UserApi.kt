package com.coeater.android.api

import com.coeater.android.model.FriendsInfo
import retrofit2.http.*

interface UserApi {

    @GET("users/friend/")
    suspend fun getFriends(): FriendsInfo
}
