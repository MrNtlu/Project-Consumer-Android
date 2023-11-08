package com.mrntlu.projectconsumer.ui

import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.mrntlu.projectconsumer.adapters.LegendContentAdapter
import com.mrntlu.projectconsumer.models.auth.UserInfo
import com.mrntlu.projectconsumer.models.auth.UserInfoCommon
import com.mrntlu.projectconsumer.utils.dpToPxFloat
import com.mrntlu.projectconsumer.utils.loadWithGlide
import com.mrntlu.projectconsumer.viewmodels.shared.UserSharedViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

abstract class BaseProfileFragment<T>: BaseFragment<T>() {
    protected val userSharedViewModel: UserSharedViewModel by activityViewModels()

    protected var userInfo: UserInfo? = null

    protected var legendContentAdapter: LegendContentAdapter? = null

    protected suspend fun setUI(
        profileIV: ImageView,
        profilePlaceHolderIV: ImageView,
        profileImageProgressBar: ProgressBar,
        profileUsernameTV: TextView,
        movieStatTV: TextView,
        tvStatTV: TextView,
        gameStatTV: TextView,
        animeStatTV: TextView,
        movieWatchedTV: TextView,
        tvWatchedTV: TextView,
        animeWatchedTV: TextView,
        gamePlayedTV: TextView,
        profileLevelBar: LinearProgressIndicator,
        profileLevelTV: TextView,
    ) {
        userInfo?.let {
            binding.apply {
                setImage(
                it.image ?: "",
                    profileIV,
                    profilePlaceHolderIV,
                    profileImageProgressBar
                )

                withContext(Dispatchers.Default) {
                    val movieCountStr = it.movieCount.toString()
                    val tvCountStr = it.tvCount.toString()
                    val gameCountStr = it.gameCount.toString()
                    val animeCountStr = it.animeCount.toString()

                    val movieWatchedStr = convertLength(it.movieWatchedTime)
                    val tvWatchedStr = it.tvWatchedEpisodes.toString()
                    val animeWatchedStr = it.animeWatchedEpisodes.toString()
                    val gameTotalPlayedStr = convertLength(it.gameTotalHoursPlayed)

                    val levelStr = "${it.level} lv."

                    withContext(Dispatchers.Main) {
                        profileUsernameTV.text = it.username

                        movieStatTV.text = movieCountStr
                        tvStatTV.text = tvCountStr
                        gameStatTV.text = gameCountStr
                        animeStatTV.text = animeCountStr

                        movieWatchedTV.text = movieWatchedStr
                        tvWatchedTV.text = tvWatchedStr
                        animeWatchedTV.text = animeWatchedStr
                        gamePlayedTV.text = gameTotalPlayedStr

                        profileLevelBar.progress = it.level
                        profileLevelTV.text = levelStr
                    }
                }
            }
        }
    }

    protected fun setRecyclerView(
        legendContentRV: RecyclerView,
        onClick: (UserInfoCommon) -> Unit,
    ) {
        legendContentRV.apply {
            val linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            layoutManager = linearLayoutManager

            legendContentAdapter = LegendContentAdapter(
                userInfo?.legendContent ?: arrayListOf(),
                legendContentRV.context.dpToPxFloat(6f),
            ) { item ->
                onClick(item)
            }
            adapter = legendContentAdapter
        }
    }

    protected fun setImage(
        imageUrl: String,
        profileIV: ImageView,
        profilePlaceHolderIV: ImageView,
        profileImageProgressBar: ProgressBar,
    ) {
        profileIV.loadWithGlide(
            imageUrl,
            profilePlaceHolderIV,
            profileImageProgressBar,
        ) {
            transform(CenterCrop())
        }
    }

    private fun convertLength(length: Long): String {
        return if (length > 60) {
            val hours = length / 60
            if (hours < 10)
                String.format("%01d", hours)
            else
                String.format("%02d", hours)
        } else length.toString()
    }
}