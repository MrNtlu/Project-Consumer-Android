package com.mrntlu.projectconsumer.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.adapters.viewholders.EmptyViewHolder
import com.mrntlu.projectconsumer.adapters.viewholders.ErrorViewHolder
import com.mrntlu.projectconsumer.databinding.CellEmptyBinding
import com.mrntlu.projectconsumer.databinding.CellErrorBinding
import com.mrntlu.projectconsumer.databinding.CellReviewContentBinding
import com.mrntlu.projectconsumer.databinding.CellReviewContentLoadingBinding
import com.mrntlu.projectconsumer.interfaces.ErrorViewHolderBind
import com.mrntlu.projectconsumer.interfaces.ReviewWithContentInteraction
import com.mrntlu.projectconsumer.models.main.review.ReviewWithContent
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.Constants.REVIEW_PAGINATION_LIMIT
import com.mrntlu.projectconsumer.utils.Operation
import com.mrntlu.projectconsumer.utils.OperationEnum
import com.mrntlu.projectconsumer.utils.RecyclerViewEnum
import com.mrntlu.projectconsumer.utils.convertToHumanReadableDateString
import com.mrntlu.projectconsumer.utils.loadWithGlide
import com.mrntlu.projectconsumer.utils.sendHapticFeedback
import com.mrntlu.projectconsumer.utils.setGone
import com.mrntlu.projectconsumer.utils.setSafeOnClickListener
import com.mrntlu.projectconsumer.utils.setVisibilityByCondition
import com.mrntlu.projectconsumer.utils.setVisible
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Suppress("UNCHECKED_CAST")
@SuppressLint("NotifyDataSetChanged")
class ReviewWithContentAdapter(
    private val isAuthenticated: Boolean,
    private val radiusInPx: Float,
    private val interaction: ReviewWithContentInteraction,
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val sizeMultiplier = 0.7f
    private val sizeMultipliedRadiusInPx = radiusInPx * sizeMultiplier

    private var errorMessage: String? = null
    var isLoading = true
    var isPaginating = false
    var canPaginate = true

    private var arrayList: ArrayList<ReviewWithContent> = arrayListOf()

    private suspend fun handleDiffUtil(newList: ArrayList<ReviewWithContent>) = withContext(Dispatchers.Default) {
        val diffUtil = DiffUtilCallback(
            arrayList,
            newList
        )
        val diffResults = DiffUtil.calculateDiff(diffUtil, true)

        arrayList = newList.toCollection(ArrayList())

        withContext(Dispatchers.Main) {
            diffResults.dispatchUpdatesTo(this@ReviewWithContentAdapter)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            RecyclerViewEnum.Empty.value -> EmptyViewHolder(CellEmptyBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            RecyclerViewEnum.Loading.value -> ReviewLoadingViewHolder(CellReviewContentLoadingBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            RecyclerViewEnum.Error.value -> ErrorViewHolder<ReviewWithContent>(CellErrorBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            RecyclerViewEnum.PaginationLoading.value -> ReviewPaginationLoadingViewHolder(CellReviewContentLoadingBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else ->  ItemViewHolder(CellReviewContentBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(getItemViewType(position)) {
            RecyclerViewEnum.View.value -> {
                (holder as ItemViewHolder).bind(arrayList[position], position)
            }
            RecyclerViewEnum.Error.value -> {
                (holder as ErrorViewHolderBind<ReviewWithContent>).bind(errorMessage, interaction)
            }
        }
    }

    override fun getItemViewType(position: Int) : Int {
        return if (isLoading)
            RecyclerViewEnum.Loading.value
        else if (errorMessage != null)
            RecyclerViewEnum.Error.value
        else if (arrayList.isEmpty())
            RecyclerViewEnum.Empty.value
        else if (isPaginating && position == arrayList.size)
            RecyclerViewEnum.PaginationLoading.value
        else
            RecyclerViewEnum.View.value
    }

    override fun getItemCount(): Int {
        return if (isLoading)
            REVIEW_PAGINATION_LIMIT
        else if (errorMessage != null || arrayList.isEmpty()) {
            1
        }
        else {
            if (arrayList.isNotEmpty() && !isPaginating && canPaginate)
                arrayList.size
            else
                arrayList.size.plus(1)
        }
    }

    suspend fun setData(
        newList: ArrayList<ReviewWithContent>,
        isPaginationData: Boolean = false,
        isPaginationExhausted: Boolean = false,
        isPaginating: Boolean = false,
        didOrientationChanged: Boolean = false,
    ) {
        setState(
            if (arrayList.isEmpty() && newList.isEmpty()) RecyclerViewEnum.Empty
            else if (isPaginating) RecyclerViewEnum.PaginationLoading
            else RecyclerViewEnum.View
        )

        if (newList.isNotEmpty()) {
            if (didOrientationChanged || (!isPaginationData && !isPaginating)) {
                if (arrayList.isNotEmpty())
                    arrayList.clear()

                arrayList.addAll(newList)
                notifyDataSetChanged()
            } else if (isPaginating) {
                notifyItemInserted(itemCount)

                if (arrayList.isEmpty())
                    handleDiffUtil(newList)
            } else {
                if (!isPaginationExhausted)
                    notifyItemRemoved(itemCount)

                handleDiffUtil(newList)
            }
        } else if (arrayList.isEmpty() && newList.isEmpty())
            notifyDataSetChanged()
    }

    suspend fun handleOperation(operation: Operation<ReviewWithContent>) {
        val newList = arrayList.toMutableList()

        when(operation.operationEnum) {
            OperationEnum.Delete -> {
                newList.remove(operation.data)
            }
            OperationEnum.Update -> {
                if (operation.data != null) {
                    val index = newList.indexOfFirst {
                        it.id == operation.data.id
                    }
                    newList[index] = operation.data
                }
            }
        }

        handleDiffUtil(newList as ArrayList<ReviewWithContent>)
    }

    fun setErrorView(errorMessage: String) {
        setState(RecyclerViewEnum.Error)
        this.errorMessage = errorMessage
        notifyDataSetChanged()
    }

    fun setLoadingView() {
        if (arrayList.isNotEmpty())
            arrayList.clear()
        setState(RecyclerViewEnum.Loading)
        notifyDataSetChanged()
    }

    private fun setState(rvEnum: RecyclerViewEnum) {
        when(rvEnum) {
            RecyclerViewEnum.Empty -> {
                isLoading = false
                isPaginating = false
                errorMessage = null
                canPaginate = false
            }
            RecyclerViewEnum.Loading -> {
                isLoading = true
                isPaginating = false
                errorMessage = null
                canPaginate = false
            }
            RecyclerViewEnum.Error -> {
                isLoading = false
                isPaginating = false
                canPaginate = false
            }
            RecyclerViewEnum.View -> {
                isLoading = false
                isPaginating = false
                errorMessage = null
                canPaginate = true
            }
            RecyclerViewEnum.PaginationLoading -> {
                isLoading = false
                isPaginating = true
                errorMessage = null
                canPaginate = false
            }
            else -> {}
        }
    }

    inner class ReviewLoadingViewHolder(
        binding: CellReviewContentLoadingBinding,
    ): RecyclerView.ViewHolder(binding.root)

    inner class ReviewPaginationLoadingViewHolder(
        binding: CellReviewContentLoadingBinding,
    ): RecyclerView.ViewHolder(binding.root)

    inner class ItemViewHolder(
        private val binding: CellReviewContentBinding,
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ReviewWithContent, position: Int) {
            binding.apply {
                val isRatioDifferent = item.contentType == Constants.ContentType.GAME.request

                imageInclude.apply {
                    previewCard.setGone()
                    previewShimmerLayout.setVisible()
                    previewShimmerCV.radius = radiusInPx
                    previewTV.setGone()

                    previewIV.scaleType = if (isRatioDifferent)
                        ImageView.ScaleType.CENTER_CROP
                    else
                        ImageView.ScaleType.FIT_XY

                    previewIV.loadWithGlide(item.content.imageURL, previewCard, previewShimmerLayout, sizeMultiplier) {
                        if (isRatioDifferent)
                            transform(CenterCrop(), RoundedCorners(sizeMultipliedRadiusInPx.toInt()))
                        else
                            transform(RoundedCorners(sizeMultipliedRadiusInPx.toInt()))
                    }

                    previewCard.radius = radiusInPx
                    previewIV.contentDescription = item.content.titleEn
                }
                contentTitleTV.text = item.content.titleEn

                timeTV.text = item.createdAt.convertToHumanReadableDateString(true) ?: item.createdAt
                reviewRateTV.text = item.star.toString()
                popularityTV.text = item.popularity.toString()
                reviewTV.text = item.review

                binding.reviewSpoilerTV.setVisibilityByCondition(!item.isSpoiler)
                binding.reviewTV.setVisibilityByCondition(item.isSpoiler)

                likeButton.setImageResource(if (item.isLiked) R.drawable.ic_like else R.drawable.ic_like_outline)
                likeButton.isEnabled = isAuthenticated && !item.isAuthor
                binding.likeButton.setSafeOnClickListener {
                    binding.root.sendHapticFeedback()
                    interaction.onLikeClicked(item, position)
                }

                actionLayout.setVisibilityByCondition(!item.isAuthor)
                if (!isAuthenticated)
                    actionLayout.setGone()

                imageInclude.root.setSafeOnClickListener {
                    interaction.onContentClicked(item, position)
                }

                binding.editButton.setSafeOnClickListener {
                    binding.root.sendHapticFeedback()
                    interaction.onEditClicked(item, position)
                }

                binding.deleteButton.setSafeOnClickListener {
                    binding.root.sendHapticFeedback()
                    interaction.onDeleteClicked(item, position)
                }

                root.setSafeOnClickListener {
                    interaction.onItemSelected(item, position)
                }
            }
        }
    }
}