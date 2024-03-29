package com.mrntlu.projectconsumer.interfaces

interface DiscoverOnBottomSheet {
    fun onApply(
        genre: String?,
        status: String?,
        sort: String,
        from: Int?,
        to: Int?,
        animeTheme: String?,
        animeDemographics: String?,
        gameTBA: Boolean?,
        gamePlatform: String?,
    )
}