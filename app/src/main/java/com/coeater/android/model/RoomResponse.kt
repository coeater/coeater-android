package com.coeater.android.model

import com.google.gson.annotations.SerializedName


data class RoomResponse (
    @SerializedName("id") val id : Int = 0,
    @SerializedName("created") val created : String = "",
    @SerializedName("room_code") val room_code : String = "",
    @SerializedName("inviter") val inviter : Int = 0,
    @SerializedName("invitee") val invitee : String = "",
    @SerializedName("owner") val owner : User = User(),
    @SerializedName("target") val target : User? = null,
    @SerializedName("accepted") val accepted : String = "",
    @SerializedName("checked") val checked : Boolean = false
)