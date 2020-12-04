package com.coeater.android.model

import com.google.gson.annotations.SerializedName

data class YoutubePageInfo (
    @SerializedName("totalResults") val totalResults: Int = 0,
    @SerializedName("resultsPerPage") val resultsPerPage: Int = 0
)

data class YoutubeResult (
    @SerializedName("kind") val kind: String = "",
    @SerializedName("etag") val etag: String = "",
    @SerializedName("nextPageToken") val nextPageToken: String? = null,
    @SerializedName("prevPageToken") val prevPageToken: String? = null,
    @SerializedName("regionCode") val regionCode: String = "",
    @SerializedName("pageInfo") val pageInfo: YoutubePageInfo = YoutubePageInfo(),
    @SerializedName("items") val items: List<YoutubeItem> = listOf()
)