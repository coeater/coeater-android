package com.coeater.android.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("nickname") val nickname: String = "",
    @SerializedName("code") val code: String = ""
)
