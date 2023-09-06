package com.mrntlu.projectconsumer.adapters

import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.adapters.viewholders.EmptyViewHolder
import com.mrntlu.projectconsumer.adapters.viewholders.ErrorViewHolder
import com.mrntlu.projectconsumer.adapters.viewholders.LoadingViewHolder
import com.mrntlu.projectconsumer.adapters.viewholders.PaginationExhaustViewHolder
import com.mrntlu.projectconsumer.adapters.viewholders.PaginationLoadingViewHolder
import com.mrntlu.projectconsumer.databinding.CellContentAltBinding
import com.mrntlu.projectconsumer.databinding.CellContentAltLoadingBinding
import com.mrntlu.projectconsumer.databinding.CellEmptyBinding
import com.mrntlu.projectconsumer.databinding.CellErrorBinding
import com.mrntlu.projectconsumer.databinding.CellLoadingBinding
import com.mrntlu.projectconsumer.databinding.CellPaginationExhaustBinding
import com.mrntlu.projectconsumer.databinding.CellPaginationLoadingBinding
import com.mrntlu.projectconsumer.databinding.CellPreviewItemBinding
import com.mrntlu.projectconsumer.interfaces.ContentModel
import com.mrntlu.projectconsumer.interfaces.Interaction
import com.mrntlu.projectconsumer.interfaces.ItemViewHolderBind
import com.mrntlu.projectconsumer.models.main.anime.Anime
import com.mrntlu.projectconsumer.models.main.game.Game
import com.mrntlu.projectconsumer.models.main.movie.Movie
import com.mrntlu.projectconsumer.models.main.tv.TVSeries
import com.mrntlu.projectconsumer.utils.Constants.DEFAULT_RATIO
import com.mrntlu.projectconsumer.utils.Constants.GAME_RATIO
import com.mrntlu.projectconsumer.utils.RecyclerViewEnum
import com.mrntlu.projectconsumer.utils.convertToHumanReadableDateString
import com.mrntlu.projectconsumer.utils.dpToPxFloat
import com.mrntlu.projectconsumer.utils.isNotEmptyOrBlank
import com.mrntlu.projectconsumer.utils.loadWithGlide
import com.mrntlu.projectconsumer.utils.roundSingleDecimal
import com.mrntlu.projectconsumer.utils.setGone
import com.mrntlu.projectconsumer.utils.setSafeOnClickListener
import com.mrntlu.projectconsumer.utils.setVisibilityByCondition
import com.mrntlu.projectconsumer.utils.setVisible

class ContentAdapter<T: ContentModel>(
    override val interaction: Interaction<T>,
    private val isRatioDifferent: Boolean = false,
    gridCount: Int,
    isDarkTheme: Boolean,
    private val isAltLayout: Boolean = false,
): BaseGridPaginationAdapter<T>(
    interaction, gridCount, isDarkTheme,
    if (isRatioDifferent) GAME_RATIO else DEFAULT_RATIO,
    isAltLayout = isAltLayout
) {

    override fun handleDiffUtil(newList: ArrayList<T>) {
        val diffUtil = DiffUtilCallback(
            arrayList,
            newList
        )
        val diffResults = DiffUtil.calculateDiff(diffUtil, true)

        arrayList = newList.toList() as ArrayList<T>
        diffResults.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            RecyclerViewEnum.Empty.value -> EmptyViewHolder(CellEmptyBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            RecyclerViewEnum.Loading.value -> if (isAltLayout)
                LoadingAltViewHolder(CellContentAltLoadingBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else
                LoadingViewHolder(CellLoadingBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            RecyclerViewEnum.Error.value -> ErrorViewHolder<T>(CellErrorBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            RecyclerViewEnum.PaginationLoading.value -> if (isAltLayout)
                LoadingAltViewHolder(CellContentAltLoadingBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else
                PaginationLoadingViewHolder(CellPaginationLoadingBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            RecyclerViewEnum.PaginationExhaust.value -> PaginationExhaustViewHolder(CellPaginationExhaustBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> if (isAltLayout)
                ItemAltViewHolder<T>(CellContentAltBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else
                return ItemViewHolder<T>(CellPreviewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    inner class LoadingAltViewHolder(
        binding: CellContentAltLoadingBinding,
    ): RecyclerView.ViewHolder(binding.root)

    inner class ItemAltViewHolder<T: ContentModel>(
        private val binding: CellContentAltBinding,
    ): RecyclerView.ViewHolder(binding.root), ItemViewHolderBind<T> {
        override fun bind(item: T, position: Int, interaction: Interaction<T>) {
            binding.apply {
                imageInclude.apply {
                    val radiusInPx = root.context.dpToPxFloat(8f)

                    previewCard.setGone()
                    previewShimmerLayout.setVisible()
                    previewGameCV.setGone()

                    previewIV.scaleType = if (isRatioDifferent)
                        ImageView.ScaleType.CENTER_CROP
                    else
                        ImageView.ScaleType.FIT_XY

                    previewIV.loadWithGlide(item.imageURL, previewCard, previewShimmerLayout) {
                        if (isRatioDifferent)
                            transform(CenterCrop(), RoundedCorners(radiusInPx.toInt()))
                        else
                            transform(RoundedCorners(radiusInPx.toInt()))
                    }

                    previewIV.contentDescription = item.title
                    previewTV.text = item.title
                }

                when(item) {
                    is Anime -> {
                        titleTV.text = if (item.title.isNotEmptyOrBlank()) item.title else item.titleOriginal
                        titleOriginalTV.text = if (item.title.isNotEmptyOrBlank()) item.titleOriginal else item.titleJP

                        extraInfoIV.setImageDrawable(ContextCompat.getDrawable(root.context, R.drawable.ic_anime))

                        descriptionTV.text = item.description
                        scoreTV.text = if (item.score > 0)
                            item.score.toDouble().roundSingleDecimal().toString()
                        else
                            "?"

                        val fromDate = if (item.aired.from != null && item.aired.from.isNotEmptyOrBlank()) item.aired.from.convertToHumanReadableDateString(true) else "?"

                        val extraStr = if (item.episodes != null && item.episodes!! > 0)
                            "${item.episodes} eps. $fromDate"
                        else
                            fromDate

                        extraInfoTV.text = extraStr
                    }
                    is Movie -> {
                        titleTV.text = item.title
                        titleOriginalTV.text = item.titleOriginal

                        extraInfoIV.setImageDrawable(ContextCompat.getDrawable(root.context, R.drawable.ic_content_type_24))

                        descriptionTV.text = item.description
                        scoreTV.text = if (item.score > 0)
                            item.score.toDouble().roundSingleDecimal().toString()
                        else
                            "?"

                        val lengthStr = if (item.length > 10) {
                            val hours = item.length / 60
                            val minutes = item.length % 60
                            String.format("%02dh %02dm • ", hours, minutes)
                        } else null

                        val extraStr = "${lengthStr}${if (item.releaseDate.isNotEmptyOrBlank()) item.releaseDate.convertToHumanReadableDateString(true) else ""}"

                        extraInfoTV.text = extraStr
                    }
                    is TVSeries -> {
                        titleTV.text = item.title
                        titleOriginalTV.text = item.titleOriginal

                        extraInfoIV.setImageDrawable(ContextCompat.getDrawable(root.context, R.drawable.ic_tv))

                        descriptionTV.text = Html.fromHtml(item.description, HtmlCompat.FROM_HTML_MODE_LEGACY)
                        scoreTV.text = if (item.score > 0)
                            item.score.toDouble().roundSingleDecimal().toString()
                        else
                            "?"

                        val totalSeasonsStr = "${item.totalSeasons} seas."
                        val totalEpisodesStr = "${item.episodes} eps."
                        val seasonEpsStr = "$totalSeasonsStr $totalEpisodesStr"
                        extraInfoTV.text = seasonEpsStr
                    }
                    is Game -> {
                        titleTV.text = item.title
                        titleOriginalTV.text = item.titleOriginal

                        extraInfoIV.setImageDrawable(ContextCompat.getDrawable(root.context, R.drawable.ic_game_24))

                        descriptionTV.text = item.description
                        scoreTV.text = if (item.score > 0)
                            item.score.toDouble().roundSingleDecimal().toString()
                        else
                            "?"

                        val extraStr = if (item.releaseDate != null && item.releaseDate.isNotEmptyOrBlank())
                            item.releaseDate.convertToHumanReadableDateString(isAlt = true)
                        else if (item.tba)
                            "TBA"
                        else
                            item.ageRating ?: ""

                        extraInfoTV.text = extraStr
                    }
                    else -> {
                        titleTV.text = item.title
                        titleOriginalTV.text = item.titleOriginal
                        descriptionTV.text = item.description
                        scoreTV.text = if (item.score > 0)
                            item.score.toDouble().roundSingleDecimal().toString()
                        else
                            "?"

                        val extraStr = if (item.aired != null && item.episodes != null) { // Anime
                            val firstStr = item.episodes?.let {
                                "${item.episodes} eps."
                            }

                            val secondStr = item.aired?.let {
                                if (it.from != null && it.from.isNotEmptyOrBlank()) it.from.convertToHumanReadableDateString(true) else "?"
                            }

                            "$firstStr $secondStr"
                        } else if (item.length != null && item.releaseDate != null) { // Movie
                            val firstStr = item.length?.let {
                                if (it > 10) {
                                    val hours = it / 60
                                    val minutes = it % 60
                                    String.format("%02dh %02dm • ", hours, minutes)
                                } else "${item.length}m • "
                            }

                            val secondStr = item.releaseDate?.let {
                                if (it.isNotEmptyOrBlank()) it.convertToHumanReadableDateString(true) else ""
                            }

                            "$firstStr $secondStr"
                        } else if (item.episodes != null && item.totalSeasons != null) { // TV Series
                            val totalSeasonsStr = "${item.totalSeasons} seas."
                            val totalEpisodesStr = "${item.episodes} eps."

                            "$totalSeasonsStr $totalEpisodesStr"
                        } else if (item.releaseDate != null && item.releaseDate!!.isNotEmptyOrBlank()) {
                            item.releaseDate!!.convertToHumanReadableDateString(isAlt = true)
                        } else {
                            if (item.aired != null) {
                                item.aired?.let {
                                    val fromDate = if (it.from != null && it.from.isNotEmptyOrBlank()) it.from.convertToHumanReadableDateString(true) else "?"
                                    val toDate = if (it.to != null && it.to.isNotEmptyOrBlank()) it.to.convertToHumanReadableDateString(true) else "?"
                                    "$fromDate to $toDate • "
                                }
                            } else if (item.episodes != null) {
                                item.episodes?.let {
                                    "${item.episodes} eps."
                                }
                            } else if (item.length != null) {
                                item.length?.let {
                                    if (it > 10) {
                                        val hours = it / 60
                                        val minutes = it % 60
                                        String.format("%02dh %02dm • ", hours, minutes)
                                    } else "${item.length}m • "
                                }
                            } else
                                ""
                        }

                        extraInfoTV.text = extraStr
                    }
                }

                root.setSafeOnClickListener {
                    interaction.onItemSelected(item, position)
                }
            }
        }
    }

    inner class ItemViewHolder<T: ContentModel>(
        private val binding: CellPreviewItemBinding,
    ): RecyclerView.ViewHolder(binding.root), ItemViewHolderBind<T> {
        override fun bind(item: T, position: Int, interaction: Interaction<T>) {
            binding.apply {
                val radiusInPx = root.context.dpToPxFloat(8f)

                previewCard.setGone()
                previewShimmerLayout.setVisible()
                previewGameCV.setVisibilityByCondition(!isRatioDifferent)

                (previewIV.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = if (isRatioDifferent) "16:9" else "2:3"
                (previewCard.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = if (isRatioDifferent) "16:9" else "2:3"
                (previewShimmerLayout.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = if (isRatioDifferent) "16:9" else "2:3"

                if (isRatioDifferent) {
                    val shapeAppearanceModelBuilder = ShapeAppearanceModel.Builder().apply {
                        setBottomLeftCorner(CornerFamily.ROUNDED, radiusInPx)
                        setBottomRightCorner(CornerFamily.ROUNDED, radiusInPx)
                    }
                    val shapeAppearanceModel = shapeAppearanceModelBuilder.build()
                    previewGameCV.shapeAppearanceModel = shapeAppearanceModel
                }

                previewIV.scaleType = if (isRatioDifferent)
                    ImageView.ScaleType.CENTER_CROP
                else
                    ImageView.ScaleType.FIT_XY

                previewIV.loadWithGlide(item.imageURL, previewCard, previewShimmerLayout) {
                    if (isRatioDifferent)
                        transform(CenterCrop(), RoundedCorners(radiusInPx.toInt()))
                    else
                        transform(RoundedCorners(radiusInPx.toInt()))
                }

                previewIV.contentDescription = item.title
                previewTV.text = item.title
                previewGameTitleTV.text = item.title

                root.setSafeOnClickListener {
                    interaction.onItemSelected(item, position)
                }
            }
        }
    }
}