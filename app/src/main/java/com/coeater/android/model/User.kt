package com.coeater.android.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("nickname") val nickname: String = "",
    @SerializedName("code") val code: String = "",
    @SerializedName("profile") val profile: String? = null,
    @SerializedName("created") val created: String = ""
) : Parcelable
