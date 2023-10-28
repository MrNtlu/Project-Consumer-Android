package com.mrntlu.projectconsumer.models.common

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ReviewSummary(
    @SerializedName("avg_star")
    val averageStar: Float,

    @SerializedName("total_votes")
    val totalVotes: Int,

    @SerializedName("is_reviewed")
    val isReviewed: Boolean,

    @SerializedName("star_counts")
    val starCounts: StarCounts,
): Parcelable {
    constructor(): this(0f, 0, false, StarCounts())
}

@Parcelize
data class StarCounts(
    @SerializedName("one_star")
    val oneStar: Int,

    @SerializedName("two_star")
    val twoStar: Int,

    @SerializedName("three_star")
    val threeStar: Int,

    @SerializedName("four_star")
    val fourStar: Int,

    @SerializedName("five_star")
    val fiveStar: Int,
): Parcelable {
    constructor(): this(0, 0, 0, 0, 0)
}
