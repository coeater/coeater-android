package com.coeater.android.model

import com.google.gson.annotations.SerializedName

data class YoutubeId (
    @SerializedName("kind") val kind: String = "",
    @SerializedName("videoId") val videoId: String = ""
)

data class YoutubeItem (
    @SerializedName("kind") val kind: String = "",
    @SerializedName("etag") val etag: String = "",
    @SerializedName("id") val id: YoutubeId = YoutubeId(),
    @SerializedName("snippet") val snippet: YoutubeSnippet = YoutubeSnippet()
)