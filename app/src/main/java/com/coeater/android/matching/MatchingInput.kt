package com.coeater.android.matching

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
enum class MatchingMode : Parcelable {
    INVITER, INVITEE, FRIEND_INVITER, FRIEND_INVITEE
}
@Parcelize
data class MatchingInput(
    val mode: MatchingMode,
    val roomId: Int,
    val nickname: String,
    val profile: String
): Parcelable