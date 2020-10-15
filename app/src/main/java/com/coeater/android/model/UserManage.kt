package com.coeater.android.model

import com.google.gson.annotations.SerializedName

data class UserManage(
    @SerializedName("uid") val uid: String = "",
    @SerializedName("jwt") val jwt: String = "",
    @SerializedName("code") val code: String = "",
    @SerializedName("nickname") val nickname: String = ""
)
