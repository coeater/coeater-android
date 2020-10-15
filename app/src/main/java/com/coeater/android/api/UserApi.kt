package com.coeater.android.api

import com.coeater.android.model.FriendsInfo
import retrofit2.http.GET

interface UserApi {

    @GET("users/friend/")
    suspend fun getFriends(): FriendsInfo
}
