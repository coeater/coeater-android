package com.coeater.android.model

import com.google.gson.annotations.SerializedName

data class FriendsInfo(
    @SerializedName("owner") val owner: User = User(),
    @SerializedName("count") val count: Int = 0,
    @SerializedName("friends") val friends: List<User> = listOf(),
    @SerializedName("requests") val requests: List<User> = listOf(),
    @SerializedName("histories") val histories: List<User> = listOf()
)
