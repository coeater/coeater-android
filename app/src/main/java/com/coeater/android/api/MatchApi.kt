package com.coeater.android.api

import com.coeater.android.model.FriendsInfo
import com.coeater.android.model.RoomResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface MatchApi {
    @FormUrlEncoded
    @POST("match/invitation/")
    suspend fun joinRoom( @Field("room_code") roomCode: String): RoomResponse

    @FormUrlEncoded
    @POST("match/invitation/")
    suspend fun createRoom(@Field("id")id: Int? = null): RoomResponse
}
