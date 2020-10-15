package com.coeater.android.model

import com.google.gson.annotations.SerializedName

data class FriendsInfo(
    @SerializedName("owner") val owner: User = User(),
    @SerializedName("count") val count: Int = 0,
    @SerializedName("friends") val friends: List<User> = listOf()
)
