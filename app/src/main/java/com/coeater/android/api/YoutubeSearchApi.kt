package com.coeater.android.api

import com.coeater.android.model.YoutubeResult
import com.coeater.android.webrtc.youtube.DeveloperKey
import retrofit2.http.GET
import retrofit2.http.Query

interface YoutubeSearchApi {
    @GET("search?part=snippet&type=video&key=${DeveloperKey.DEVELOPER_KEY}")
    suspend fun search(
        @Query("q") query: String
    ): YoutubeResult

    @GET("search?part=snippet&type=video&key=${DeveloperKey.DEVELOPER_KEY}")
    suspend fun getOtherPage(
        @Query("q") query: String,
        @Query("pageToken") pageToken: String
    ): YoutubeResult
}