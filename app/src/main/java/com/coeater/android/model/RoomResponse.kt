package com.coeater.android.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

enum class AcceptedState {
    ACCEPTED, DECLINE, NOTCHECK
}
@Parcelize
data class RoomResponse(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("created") val created: String = "",
    @SerializedName("room_code") val room_code: String = "",
    @SerializedName("inviter") val inviter: Int = 0,
    @SerializedName("invitee") val invitee: String = "",
    @SerializedName("owner") val owner: User = User(),
    @SerializedName("target") val target: User? = null,
    @SerializedName("accepted") private val _accepted: Boolean? = null,
    @SerializedName("checked") val checked: Boolean = false
) : Parcelable {
    val accepted: AcceptedState
        get() =
            when (_accepted) {
                true -> {
                    AcceptedState.ACCEPTED
                }
                false -> {
                    AcceptedState.DECLINE
                }
                null -> {
                    AcceptedState.NOTCHECK
                }
            }
}
