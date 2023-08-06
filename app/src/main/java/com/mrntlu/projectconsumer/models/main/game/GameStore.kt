package com.mrntlu.projectconsumer.models.main.game

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class GameStore(
    val url: String,

    @SerializedName("store_id")
    val storeId: Int,
) : Parcelable
