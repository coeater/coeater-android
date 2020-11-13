package com.coeater.android.api

import com.coeater.android.model.FriendsInfo
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface HistoryApi {

    @GET("users/history/")
    suspend fun getHistory(): FriendsInfo

    @FormUrlEncoded
    @POST("users/history/")
    suspend fun addHistory(
        @Field("id") id : Int
    ): FriendsInfo
}