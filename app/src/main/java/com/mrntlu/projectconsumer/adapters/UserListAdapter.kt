package com.mrntlu.projectconsumer.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.adapters.viewholders.EmptyViewHolder
import com.mrntlu.projectconsumer.adapters.viewholders.ErrorViewHolder
import com.mrntlu.projectconsumer.databinding.CellEmptyBinding
import com.mrntlu.projectconsumer.databinding.CellErrorBinding
import com.mrntlu.projectconsumer.databinding.CellUserListBinding
import com.mrntlu.projectconsumer.databinding.CellUserListLoadingBinding
import com.mrntlu.projectconsumer.interfaces.ErrorViewHolderBind
import com.mrntlu.projectconsumer.interfaces.UserListInteraction
import com.mrntlu.projectconsumer.models.main.userInteraction.ConsumeLaterResponse
import com.mrntlu.projectconsumer.models.main.userList.UserList
import com.mrntlu.projectconsumer.ui.compose.LoadingShimmer
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.RecyclerViewEnum
import com.mrntlu.projectconsumer.utils.getColorFromAttr
import com.mrntlu.projectconsumer.utils.loadWithGlide
import com.mrntlu.projectconsumer.utils.setGone
import com.mrntlu.projectconsumer.utils.setVisibilityByCondition
import com.mrntlu.projectconsumer.utils.setVisible

@Suppress("UNCHECKED_CAST")
@SuppressLint("NotifyDataSetChanged")
class UserListAdapter(
    val interaction: UserListInteraction,
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var errorMessage: String? = null
    var isLoading = true

    private lateinit var userList: UserList
    var contentType: Constants.ContentType = Constants.ContentType.MOVIE

    private fun handleDiffUtil(newList: ArrayList<ConsumeLaterResponse>) {
        val diffUtil = DiffUtilCallback(
            arrayList,
            newList
        )
        val diffResults = DiffUtil.calculateDiff(diffUtil, true)

        arrayList = newList.toCollection(ArrayList())

        diffResults.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            RecyclerViewEnum.Empty.value -> EmptyViewHolder(CellEmptyBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            RecyclerViewEnum.Error.value -> ErrorViewHolder<UserList>(CellErrorBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            RecyclerViewEnum.Loading.value -> LoadingViewHolder(CellUserListLoadingBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> return ItemViewHolder(CellUserListBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(getItemViewType(position)) {
            RecyclerViewEnum.View.value -> {
                (holder as ItemViewHolder).bind(contentType, userList, position)
            }
            RecyclerViewEnum.Error.value -> {
                (holder as ErrorViewHolderBind<UserList>).bind(errorMessage, interaction, true)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (isLoading)
            RecyclerViewEnum.Loading.value
        else if (errorMessage != null)
            RecyclerViewEnum.Error.value
        else if (!::userList.isInitialized || (::userList.isInitialized && getContentSize() == 0))
            RecyclerViewEnum.Empty.value
        else
            RecyclerViewEnum.View.value
    }

    override fun getItemCount(): Int {
        return if (isLoading)
            20
        else if (errorMessage != null || !::userList.isInitialized || (::userList.isInitialized && getContentSize() == 0))
            1
        else
            getContentSize()
    }

    private fun getContentSize() = when(contentType){
        Constants.ContentType.ANIME -> userList.animeList
        Constants.ContentType.MOVIE -> userList.movieList
        Constants.ContentType.TV -> userList.tvList
        Constants.ContentType.GAME -> userList.gameList
    }.size

    fun changeContentType(newContentType: Constants.ContentType) {
        if (contentType != newContentType) {
            contentType = newContentType
            notifyDataSetChanged()
        }
    }

    private fun setState(rvEnum: RecyclerViewEnum) {
        when(rvEnum) {
            RecyclerViewEnum.Empty -> {
                isLoading = false
                errorMessage = null
            }
            RecyclerViewEnum.Loading -> {
                isLoading = true
                errorMessage = null
            }
            RecyclerViewEnum.Error -> {
                isLoading = false
            }
            RecyclerViewEnum.View -> {
                isLoading = false
                errorMessage = null
            }
            else -> {}
        }
    }

    //TODO Search & Handle Interaction

    inner class LoadingViewHolder(binding: CellUserListLoadingBinding): RecyclerView.ViewHolder(binding.root)

    inner class ItemViewHolder(
        private val binding: CellUserListBinding,
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(contentType: Constants.ContentType, userList: UserList, position: Int) {
            val contentStatus = when(contentType) {
                Constants.ContentType.ANIME -> userList.animeList[position].contentStatus
                Constants.ContentType.MOVIE -> userList.movieList[position].contentStatus
                Constants.ContentType.TV -> userList.tvList[position].contentStatus
                Constants.ContentType.GAME -> userList.gameList[position].contentStatus
            }

            val imageUrl = when(contentType) {
                Constants.ContentType.ANIME -> userList.animeList[position].imageUrl
                Constants.ContentType.MOVIE -> userList.movieList[position].imageUrl
                Constants.ContentType.TV -> userList.tvList[position].imageUrl
                Constants.ContentType.GAME -> userList.gameList[position].imageUrl
            }

            val title = when(contentType) {
                Constants.ContentType.ANIME -> userList.animeList[position].titleEn
                Constants.ContentType.MOVIE -> userList.movieList[position].titleEn
                Constants.ContentType.TV -> userList.tvList[position].titleEn
                Constants.ContentType.GAME -> userList.gameList[position].title
            }

            val score = when(contentType) {
                Constants.ContentType.ANIME -> userList.animeList[position].score
                Constants.ContentType.MOVIE -> userList.movieList[position].score
                Constants.ContentType.TV -> userList.tvList[position].score
                Constants.ContentType.GAME -> userList.gameList[position].score
            }

            val totalEps = when(contentType) {
                Constants.ContentType.ANIME -> userList.animeList[position].totalEpisodes
                Constants.ContentType.TV -> userList.tvList[position].totalEpisodes
                else -> null
            }

            val watchedEps = when(contentType) {
                Constants.ContentType.ANIME -> userList.animeList[position].watchedEpisodes
                Constants.ContentType.TV -> userList.tvList[position].watchedEpisodes
                else -> null
            }

            val watchedSeasons = if (contentType == Constants.ContentType.TV)
                userList.tvList[position].watchedSeasons
            else null

            val totalSeasons = "/${
                if (contentType == Constants.ContentType.TV)
                    userList.tvList[position].totalSeasons
                else "?"
            } season"

            binding.apply {
                imageInclude.apply {
                    previewComposeView.apply {
                        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                        setContent {
                            LoadingShimmer(isDarkTheme = false, roundedCornerSize = 6.dp) {
                                fillMaxHeight()
                            }
                        }
                    }

                    previewCard.setGone()
                    previewComposeView.setVisible()
                    previewIV.loadWithGlide(imageUrl ?: "", previewCard, previewComposeView) {
                        transform(RoundedCorners(12))
                    }

                    previewTV.text = title
                }

                val attrColor = when(contentStatus) {
                    Constants.UserListStatus[0].request -> R.attr.statusActiveColor
                    Constants.UserListStatus[1].request -> R.attr.statusFinishedColor
                    else -> R.attr.statusDroppedColor
                }

                statusColor.dividerColor = root.context.getColorFromAttr(attrColor)
                titleTV.text = title

                contentProgress.apply {
                    max = when(contentType) {
                        Constants.ContentType.ANIME -> userList.animeList[position].totalEpisodes ?: if (contentStatus == "finished" && watchedEps != null) watchedEps else 100
                        Constants.ContentType.MOVIE -> 1
                        Constants.ContentType.TV -> userList.tvList[position].totalEpisodes ?: if (contentStatus == "finished" && watchedEps != null) watchedEps else 100
                        Constants.ContentType.GAME -> 1
                    }
                    progress = when(contentType) {
                        Constants.ContentType.ANIME -> userList.animeList[position].watchedEpisodes
                        Constants.ContentType.MOVIE -> if (contentStatus == "finished") 1 else 0
                        Constants.ContentType.TV -> userList.tvList[position].watchedEpisodes
                        Constants.ContentType.GAME -> if (contentStatus == "finished") 1 else 0
                    }
                }

                scoreTV.setVisibilityByCondition(score == null)
                scoreStarIV.setVisibilityByCondition(score == null)
                scoreTV.text = score.toString()

                totalSeasonTV.setVisibilityByCondition(contentType != Constants.ContentType.TV)
                watchedSeasonTV.setVisibilityByCondition(contentType != Constants.ContentType.TV)
                totalSeasonTV.text = totalSeasons
                watchedSeasonTV.text = watchedSeasons?.toString() ?: "?"

                totalEpisodeTV.setVisibilityByCondition(contentType == Constants.ContentType.MOVIE)
                when(contentType) {
                    Constants.ContentType.MOVIE -> {
                        watchedEpisodeTV.text = contentStatus
                    }
                    Constants.ContentType.GAME -> {
                        val hoursPlayedStr = "hours played"
                        totalEpisodeTV.text = hoursPlayedStr

                        watchedEpisodeTV.text = userList.gameList[position].hoursPlayed?.toString() ?: "?"
                    }
                    else -> {
                        val totalEpsStr = "/${totalEps ?: "?"} eps"
                        totalEpisodeTV.text = totalEpsStr

                        watchedEpisodeTV.text = watchedEps.toString()
                    }
                }
            }
        }
    }
}