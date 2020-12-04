package com.coeater.android.api

import com.coeater.android.model.FriendsInfo
import com.coeater.android.model.User
import retrofit2.http.*

interface HistoryApi {

    @GET("users/history/")
    suspend fun getHistory(
        @Query("from") from : String,
        @Query("to") to : String
    ): FriendsInfo

    @FormUrlEncoded
    @POST("users/history/")
    suspend fun addHistory(
        @Field("id") id : Int
    ): User

    @FormUrlEncoded
    @PUT("users/history/")
    suspend fun putHistory(
        @Field("id") id : Int
    ): User
}