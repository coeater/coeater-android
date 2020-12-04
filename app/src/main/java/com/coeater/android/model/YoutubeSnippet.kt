package com.coeater.android.model

import com.google.gson.annotations.SerializedName

data class YoutubeThumbnails (
    @SerializedName("default") val default: YoutubeThumbnail = YoutubeThumbnail(),
    @SerializedName("medium") val medium: YoutubeThumbnail = YoutubeThumbnail(),
    @SerializedName("high") val high: YoutubeThumbnail = YoutubeThumbnail()
)

data class YoutubeSnippet (
    @SerializedName("publishedAt") val publishedAt: String = "",
    @SerializedName("channelId") val channelId: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("description") val description: String = "",
    @SerializedName("thumbnails") val thumbnails: YoutubeThumbnails = YoutubeThumbnails(),
    @SerializedName("channelTitle") val channelTitle: String = "",
    @SerializedName("liveBroadcastContent") val liveBroadcastContent: String = "",
    @SerializedName("publishTime") val publishTime: String = ""
)