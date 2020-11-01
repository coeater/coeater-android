package com.coeater.android.api

import com.coeater.android.model.RoomResponse
import retrofit2.http.*

interface MatchApi {
    @FormUrlEncoded
    @POST("match/invitation/")
    suspend fun joinRoom(@Field("room_code") roomCode: String): RoomResponse

    @FormUrlEncoded
    @POST("match/invitation/")
    suspend fun createRoom(@Field("id")id: Int? = null): RoomResponse

    @GET("match/invitation/{id}/")
    suspend fun getRoom(
        @Path("id") id: Int
    ): RoomResponse

    @PUT("match/invitation/{id}/")
    suspend fun acceptInvitation(
        @Path("id") id: Int
    ): RoomResponse

    @DELETE("match/invitation/{id}/")
    suspend fun rejectInvitation(
        @Path("id") id: Int
    ): RoomResponse
}
