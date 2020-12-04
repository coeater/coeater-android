package com.coeater.android.model

import com.google.gson.annotations.SerializedName

data class YoutubeThumbnail (
    @SerializedName("url") val url: String = "",
    @SerializedName("width") val width: Int = 0,
    @SerializedName("height") val height: Int = 0
)