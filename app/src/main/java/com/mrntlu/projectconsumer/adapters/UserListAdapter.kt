package com.mrntlu.projectconsumer.adapters

import android.annotation.SuppressLint
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.adapters.viewholders.EmptyViewHolder
import com.mrntlu.projectconsumer.adapters.viewholders.ErrorViewHolder
import com.mrntlu.projectconsumer.databinding.CellEmptyBinding
import com.mrntlu.projectconsumer.databinding.CellErrorBinding
import com.mrntlu.projectconsumer.databinding.CellUserListBinding
import com.mrntlu.projectconsumer.databinding.CellUserListLoadingBinding
import com.mrntlu.projectconsumer.interfaces.ErrorViewHolderBind
import com.mrntlu.projectconsumer.interfaces.UserListContentModel
import com.mrntlu.projectconsumer.interfaces.UserListInteraction
import com.mrntlu.projectconsumer.interfaces.UserListModel
import com.mrntlu.projectconsumer.models.main.userList.AnimeList
import com.mrntlu.projectconsumer.models.main.userList.GameList
import com.mrntlu.projectconsumer.models.main.userList.MovieList
import com.mrntlu.projectconsumer.models.main.userList.TVSeriesList
import com.mrntlu.projectconsumer.models.main.userList.UserList
import com.mrntlu.projectconsumer.models.main.userList.convertToAnimeList
import com.mrntlu.projectconsumer.models.main.userList.convertToGameList
import com.mrntlu.projectconsumer.models.main.userList.convertToMovieList
import com.mrntlu.projectconsumer.models.main.userList.convertToTVSeriesList
import com.mrntlu.projectconsumer.utils.Constants.ContentType
import com.mrntlu.projectconsumer.utils.Constants.UserListStatus
import com.mrntlu.projectconsumer.utils.Operation
import com.mrntlu.projectconsumer.utils.OperationEnum
import com.mrntlu.projectconsumer.utils.RecyclerViewEnum
import com.mrntlu.projectconsumer.utils.getColorFromAttr
import com.mrntlu.projectconsumer.utils.loadWithGlide
import com.mrntlu.projectconsumer.utils.sendHapticFeedback
import com.mrntlu.projectconsumer.utils.setGone
import com.mrntlu.projectconsumer.utils.setSafeOnClickListener
import com.mrntlu.projectconsumer.utils.setVisibilityByCondition
import com.mrntlu.projectconsumer.utils.setVisible

@Suppress("UNCHECKED_CAST")
@SuppressLint("NotifyDataSetChanged")
class UserListAdapter(
    private var contentType: ContentType = ContentType.MOVIE,
    private val radiusInPx: Float,
    val interaction: UserListInteraction,
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var errorMessage: String? = null
    var isLoading = true

    private lateinit var userList: UserList

    private val sizeMultiplier = 0.75f
    private val sizeMultipliedRadiusInPx = radiusInPx * sizeMultiplier

    private fun handleDiffUtil(newList: ArrayList<UserListContentModel>) {
        val diffUtil = DiffUtilCallback(
            getContentList(),
            newList
        )
        val diffResults = DiffUtil.calculateDiff(diffUtil, true)

        when(contentType){
            ContentType.ANIME -> userList.animeList = newList.toList().map { it.convertToAnimeList() }
            ContentType.MOVIE -> userList.movieList = newList.toList().map { it.convertToMovieList() }
            ContentType.TV -> userList.tvList = newList.toList().map { it.convertToTVSeriesList() }
            ContentType.GAME -> userList.gameList = newList.toList().map { it.convertToGameList() }
        }

        diffResults.dispatchUpdatesTo(this@UserListAdapter)
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
                (holder as ItemViewHolder).apply {
                    bind(contentType, userList, holder.adapterPosition)

                    binding.root.setSafeOnClickListener {
                        interaction.onItemSelected(userList, holder.adapterPosition)
                    }

                    binding.userListButton.setOnClickListener {
                        handlePopupMenu(binding, holder.adapterPosition)
                    }

                    binding.incrementEpisodeButton.setSafeOnClickListener {
                        it.sendHapticFeedback()
                        interaction.onEpisodeIncrementPressed(userList, contentType, holder.adapterPosition)
                    }

                    if (contentType.request == ContentType.TV.request) {
                        binding.incrementSeasonButton.setSafeOnClickListener {
                            it.sendHapticFeedback()
                            interaction.onSeasonIncrementPressed(userList, contentType, holder.adapterPosition)
                        }
                    }
                }
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
        else if (!::userList.isInitialized || (::userList.isInitialized && getContentList().isEmpty()))
            RecyclerViewEnum.Empty.value
        else
            RecyclerViewEnum.View.value
    }

    override fun getItemCount(): Int {
        return if (isLoading)
            20
        else if (errorMessage != null || !::userList.isInitialized || (::userList.isInitialized && getContentList().isEmpty()))
            1
        else
            getContentList().size
    }

    private fun getContentList() = when(contentType){
        ContentType.ANIME -> userList.animeList
        ContentType.MOVIE -> userList.movieList
        ContentType.TV -> userList.tvList
        ContentType.GAME -> userList.gameList
    }

    private fun handlePopupMenu(binding: CellUserListBinding, position: Int) {
        val popupMenu = PopupMenu(binding.root.context, binding.userListButton)
        popupMenu.menuInflater.inflate(R.menu.user_list_item_menu, popupMenu.menu)
        var lastTimeClicked: Long = 0

        popupMenu.setOnMenuItemClickListener { item ->
            if (SystemClock.elapsedRealtime() - lastTimeClicked < 550) {
                return@setOnMenuItemClickListener false
            }
            lastTimeClicked = SystemClock.elapsedRealtime()

            binding.root.sendHapticFeedback()
            when(item.itemId) {
                R.id.detailsMenu -> {
                    interaction.onDetailsPressed(userList, contentType, position)
                }
                R.id.editMenu -> {
                    interaction.onUpdatePressed(userList, contentType, position)
                }
                R.id.deleteMenu -> {
                    interaction.onDeletePressed(userList, contentType, position)
                }
            }
            true
        }

        popupMenu.show()
    }

    fun setErrorView(errorMessage: String) {
        setState(RecyclerViewEnum.Error)
        this.errorMessage = errorMessage
        notifyDataSetChanged()
    }

    fun setLoadingView() {
        setState(RecyclerViewEnum.Loading)
        notifyDataSetChanged()
    }

    fun changeContentType(newContentType: ContentType) {
        if (contentType != newContentType) {
            contentType = newContentType
            notifyDataSetChanged()
        }
    }

    fun setData(newUserList: UserList) {
        if (!::userList.isInitialized || isLoading) {
            userList = newUserList

            setState(
                if (getContentList().isEmpty()) RecyclerViewEnum.Empty
                else RecyclerViewEnum.View
            )

            notifyDataSetChanged()
        } else {
            val newList = when(contentType) {
                ContentType.ANIME -> newUserList.animeList
                ContentType.MOVIE -> newUserList.movieList
                ContentType.TV -> newUserList.tvList
                ContentType.GAME -> newUserList.gameList
            }.toMutableList().toCollection(ArrayList())

            val currentList = getContentList().toMutableList().toCollection(ArrayList())

            setState(
                if (currentList.isEmpty() && newList.isEmpty()) RecyclerViewEnum.Empty
                else RecyclerViewEnum.View
            )

            if (
                (currentList.isEmpty() && newList.isNotEmpty()) ||
                (newList.isEmpty() && currentList.isNotEmpty())
            ) {
                userList = newUserList

                notifyDataSetChanged()
            } else
                handleDiffUtil(newList)
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

    fun handleOperation(operation: Operation<UserListModel>) {
        val newList = getContentList().toMutableList().toCollection(ArrayList())

        when(operation.operationEnum) {
            OperationEnum.Delete -> {
                newList.remove(operation.data)
            }
            OperationEnum.Update -> {
                if (operation.data != null) {
                    val index = newList.indexOfFirst {
                        it.contentId == operation.data.contentId ||
                        it.contentExternalId == operation.data.contentExternalId
                    }

                    val item = newList[index]

                    operation.data.apply {
                        newList[index] = when(contentType) {
                            ContentType.ANIME -> AnimeList(
                                id, contentStatus, score, timesFinished, mainAttribute!!, contentId, contentExternalId,
                                item.title, item.titleOriginal, item.imageUrl, item.totalEpisodes, item.createdAt,
                            )
                            ContentType.MOVIE -> MovieList(
                                id, contentStatus, score, timesFinished, contentId, contentExternalId,
                                item.title, item.titleOriginal, item.imageUrl, item.createdAt,
                            )
                            ContentType.TV -> TVSeriesList(
                                id, contentStatus, score, timesFinished, mainAttribute, subAttribute,
                                contentId, contentExternalId, item.title, item.titleOriginal,
                                item.imageUrl, item.totalEpisodes, item.totalSeasons, item.createdAt,
                            )
                            ContentType.GAME -> GameList(
                                id, contentStatus, score, timesFinished, mainAttribute, contentId,
                                contentExternalId, item.title, item.titleOriginal, item.imageUrl, item.createdAt,
                            )
                        }
                    }
                }
            }
        }

        handleDiffUtil(newList)
    }

    inner class LoadingViewHolder(binding: CellUserListLoadingBinding): RecyclerView.ViewHolder(binding.root)

    inner class ItemViewHolder(
        val binding: CellUserListBinding,
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(contentType: ContentType, userList: UserList, position: Int) {
            val contentStatus = when (contentType) {
                ContentType.ANIME -> userList.animeList[position].contentStatus
                ContentType.MOVIE -> userList.movieList[position].contentStatus
                ContentType.TV -> userList.tvList[position].contentStatus
                ContentType.GAME -> userList.gameList[position].contentStatus
            }

            val imageUrl = when (contentType) {
                ContentType.ANIME -> userList.animeList[position].imageUrl
                ContentType.MOVIE -> userList.movieList[position].imageUrl
                ContentType.TV -> userList.tvList[position].imageUrl
                ContentType.GAME -> userList.gameList[position].imageUrl
            }

            val title = when (contentType) {
                ContentType.ANIME -> userList.animeList[position].title
                ContentType.MOVIE -> userList.movieList[position].title
                ContentType.TV -> userList.tvList[position].title
                ContentType.GAME -> userList.gameList[position].title
            }

            val score = when (contentType) {
                ContentType.ANIME -> userList.animeList[position].score
                ContentType.MOVIE -> userList.movieList[position].score
                ContentType.TV -> userList.tvList[position].score
                ContentType.GAME -> userList.gameList[position].score
            }

            val totalEps = when (contentType) {
                ContentType.ANIME -> userList.animeList[position].totalEpisodes
                ContentType.TV -> userList.tvList[position].totalEpisodes
                else -> null
            }

            val watchedEps = when (contentType) {
                ContentType.ANIME -> userList.animeList[position].mainAttribute
                ContentType.TV -> userList.tvList[position].mainAttribute
                else -> null
            }

            val watchedSeasons = if (contentType == ContentType.TV)
                userList.tvList[position].subAttribute
            else null

            val totalSeasons = "/${
                if (contentType == ContentType.TV)
                    userList.tvList[position].totalSeasons
                else "?"
            } season${if ((watchedSeasons ?: 0) > 1) "s" else ""}"

            binding.apply {
                imageInclude.apply {
                    previewCard.setGone()
                    previewTV.setGone()
                    previewShimmerLayout.setVisible()
                    previewShimmerCV.radius = radiusInPx

                    previewIV.scaleType = if (contentType == ContentType.GAME)
                        ImageView.ScaleType.CENTER_CROP
                    else
                        ImageView.ScaleType.FIT_XY

                    previewIV.loadWithGlide(imageUrl ?: "", previewCard, previewShimmerLayout, sizeMultiplier) {
                        if (contentType == ContentType.GAME)
                            transform(CenterCrop(), RoundedCorners(sizeMultipliedRadiusInPx.toInt()))
                        else
                            transform(RoundedCorners(sizeMultipliedRadiusInPx.toInt()))
                    }

                    previewCard.radius = radiusInPx
                }

                val attrColor = when (contentStatus) {
                    UserListStatus[0].request -> R.attr.statusActiveColor
                    UserListStatus[1].request -> R.attr.statusFinishedColor
                    else -> R.attr.statusDroppedColor
                }

                imageInclude.previewIV.contentDescription = title
                statusColor.dividerColor = root.context.getColorFromAttr(attrColor)
                titleTV.text = title
                scoreTV.text = score?.toString() ?: "*"
                totalSeasonTV.text = totalSeasons
                watchedSeasonTV.text = watchedSeasons?.toString() ?: "?"

                val contentStatusType = UserListStatus.find { it.request == contentStatus }?.name
                    ?: UserListStatus[0].name
                contentStatusTV.text = contentStatusType

                contentProgress.apply {
                    max = when (contentType) {
                        ContentType.ANIME -> userList.animeList[position].totalEpisodes
                            ?: if (contentStatus == "finished" && watchedEps != null) watchedEps else 100

                        ContentType.MOVIE -> 1
                        ContentType.TV -> userList.tvList[position].totalEpisodes
                            ?: if (contentStatus == "finished" && watchedEps != null) watchedEps else 100

                        ContentType.GAME -> if (userList.gameList[position].mainAttribute?.compareTo(
                                1000
                            ) == -1
                        ) 1000
                        else
                            userList.gameList[position].mainAttribute?.plus(1000)
                                ?: 1
                    }
                    progress = when (contentType) {
                        ContentType.ANIME -> userList.animeList[position].mainAttribute
                        ContentType.MOVIE -> if (contentStatus == "finished") 1 else 0
                        ContentType.TV -> userList.tvList[position].mainAttribute ?: 1
                        ContentType.GAME -> userList.gameList[position].mainAttribute
                            ?: if (contentStatus == "finished") 1 else 0
                    }
                }

                incrementEpisodeButton.setVisibilityByCondition(contentStatus != "active")
                incrementSeasonButton.setVisibilityByCondition(contentStatus != "active" || contentType != ContentType.TV)

                totalSeasonTV.setVisibilityByCondition(contentType != ContentType.TV)
                watchedSeasonTV.setVisibilityByCondition(contentType != ContentType.TV)
                episodeSeasonLayout.setVisibilityByCondition(contentType == ContentType.MOVIE)

                when (contentType) {
                    ContentType.MOVIE -> {}
                    ContentType.GAME -> {
                        val hoursPlayedStr = " hours"
                        totalEpisodeTV.text = hoursPlayedStr

                        watchedEpisodeTV.text =
                            userList.gameList[position].mainAttribute?.toString() ?: "?"
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