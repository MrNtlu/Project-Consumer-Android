package com.mrntlu.projectconsumer.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.adapters.viewholders.EmptyViewHolder
import com.mrntlu.projectconsumer.adapters.viewholders.ErrorViewHolder
import com.mrntlu.projectconsumer.adapters.viewholders.PaginationExhaustViewHolder
import com.mrntlu.projectconsumer.databinding.CellEmptyBinding
import com.mrntlu.projectconsumer.databinding.CellErrorBinding
import com.mrntlu.projectconsumer.databinding.CellPaginationExhaustBinding
import com.mrntlu.projectconsumer.databinding.CellReviewBinding
import com.mrntlu.projectconsumer.databinding.CellReviewLoadingBinding
import com.mrntlu.projectconsumer.interfaces.ErrorViewHolderBind
import com.mrntlu.projectconsumer.interfaces.PaginationExhaustViewHolderBind
import com.mrntlu.projectconsumer.interfaces.ReviewInteraction
import com.mrntlu.projectconsumer.models.main.review.Review
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.Operation
import com.mrntlu.projectconsumer.utils.OperationEnum
import com.mrntlu.projectconsumer.utils.RecyclerViewEnum
import com.mrntlu.projectconsumer.utils.convertToHumanReadableDateString
import com.mrntlu.projectconsumer.utils.getColorFromAttr
import com.mrntlu.projectconsumer.utils.loadWithGlide
import com.mrntlu.projectconsumer.utils.sendHapticFeedback
import com.mrntlu.projectconsumer.utils.setGone
import com.mrntlu.projectconsumer.utils.setSafeOnClickListener
import com.mrntlu.projectconsumer.utils.setVisibilityByCondition

@Suppress("UNCHECKED_CAST")
@SuppressLint("NotifyDataSetChanged")
class ReviewAdapter(
    private val isAuthenticated: Boolean,
    private val interaction: ReviewInteraction,
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var errorMessage: String? = null
    var isLoading = true
    var isPaginating = false
    var canPaginate = true

    private var arrayList: ArrayList<Review> = arrayListOf()

    private fun handleDiffUtil(newList: ArrayList<Review>) {
        val diffUtil = DiffUtilCallback(
            arrayList,
            newList
        )
        val diffResults = DiffUtil.calculateDiff(diffUtil, true)

        arrayList = newList.toCollection(ArrayList())

        diffResults.dispatchUpdatesTo(this@ReviewAdapter)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            RecyclerViewEnum.Empty.value -> EmptyViewHolder(CellEmptyBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            RecyclerViewEnum.Loading.value -> ReviewLoadingViewHolder(CellReviewLoadingBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            RecyclerViewEnum.Error.value -> ErrorViewHolder<Review>(CellErrorBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            RecyclerViewEnum.PaginationLoading.value -> ReviewPaginationLoadingViewHolder(CellReviewLoadingBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            RecyclerViewEnum.PaginationExhaust.value -> PaginationExhaustViewHolder(CellPaginationExhaustBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else ->  ItemViewHolder(CellReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(getItemViewType(position)) {
            RecyclerViewEnum.View.value -> {
                (holder as ItemViewHolder).bind(arrayList[position], position)
            }
            RecyclerViewEnum.Error.value -> {
                (holder as ErrorViewHolderBind<Review>).bind(errorMessage, interaction)
            }
            RecyclerViewEnum.PaginationExhaust.value -> {
                (holder as PaginationExhaustViewHolderBind<Review>).bind(interaction)
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
        else if (!canPaginate && position == arrayList.size)
            RecyclerViewEnum.PaginationExhaust.value
        else
            RecyclerViewEnum.View.value
    }

    override fun getItemCount(): Int {
        return if (isLoading)
            Constants.REVIEW_PAGINATION_LIMIT
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

    fun setData(
        newList: ArrayList<Review>,
        isPaginationData: Boolean = false,
        isPaginationExhausted: Boolean = false,
        isPaginating: Boolean = false,
        didOrientationChanged: Boolean = false,
    ) {
        setState(
            if (arrayList.isEmpty() && newList.isEmpty()) RecyclerViewEnum.Empty
            else if (isPaginationExhausted) RecyclerViewEnum.PaginationExhaust
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

    fun handleOperation(operation: Operation<Review>) {
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

        handleDiffUtil(newList as ArrayList<Review>)
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
            RecyclerViewEnum.PaginationExhaust -> {
                isLoading = false
                isPaginating = false
                canPaginate = false
            }
        }
    }

    inner class ReviewLoadingViewHolder(
        binding: CellReviewLoadingBinding,
    ): RecyclerView.ViewHolder(binding.root)

    inner class ReviewPaginationLoadingViewHolder(
        binding: CellReviewLoadingBinding,
    ): RecyclerView.ViewHolder(binding.root)

    inner class ItemViewHolder(
        private val binding: CellReviewBinding,
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Review, position: Int) {
            item.apply {
                binding.authorImage.loadWithGlide(author.image, null, binding.authorProgressBar, 0.9f) { transform(CenterCrop()) }
                binding.authorTV.text = author.username

                binding.timeTV.text = createdAt.convertToHumanReadableDateString(true) ?: createdAt
                binding.reviewRateTV.text = star.toString()
                binding.popularityTV.text = popularity.toString()

                binding.reviewTV.text = review
                binding.reviewSpoilerTV.setVisibilityByCondition(!isSpoiler)
                binding.reviewTV.setVisibilityByCondition(isSpoiler)

                binding.authorCV.strokeColor = ContextCompat.getColor(
                    binding.root.context,
                    if (isAuthor) R.color.blue500 else android.R.color.transparent
                )
                binding.authorTV.setTextColor(
                    if (isAuthor)
                        ContextCompat.getColor(binding.root.context, R.color.blue500)
                    else
                        binding.root.context.getColorFromAttr(R.attr.mainTextColor)
                )

                binding.premiumAnimation.setVisibilityByCondition(!author.isPremium)
                binding.actionLayout.setVisibilityByCondition(!isAuthor)
                if (!isAuthenticated)
                    binding.actionLayout.setGone()

                binding.authorCV.setSafeOnClickListener {
                    interaction.onProfileClicked(item, position)
                }

                binding.authorTV.setSafeOnClickListener {
                    interaction.onProfileClicked(item, position)
                }

                binding.likeButton.setImageResource(if (isLiked) R.drawable.ic_like else R.drawable.ic_like_outline)
                binding.likeButton.isEnabled = isAuthenticated && !isAuthor
                binding.likeButton.setSafeOnClickListener {
                    binding.root.sendHapticFeedback()
                    interaction.onLikeClicked(item, position)
                }

                binding.editButton.setSafeOnClickListener {
                    binding.root.sendHapticFeedback()
                    interaction.onEditClicked(item, position)
                }

                binding.deleteButton.setSafeOnClickListener {
                    binding.root.sendHapticFeedback()
                    interaction.onDeleteClicked(item, position)
                }

                binding.root.setSafeOnClickListener {
                    interaction.onItemSelected(item, position)
                }
            }
        }
    }
}