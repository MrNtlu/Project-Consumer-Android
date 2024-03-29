package com.mrntlu.projectconsumer.adapters

import android.annotation.SuppressLint
import androidx.recyclerview.widget.RecyclerView
import com.mrntlu.projectconsumer.interfaces.*
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.Constants.DEFAULT_RATIO
import com.mrntlu.projectconsumer.utils.RecyclerViewEnum

@Suppress("UNCHECKED_CAST")
@SuppressLint("NotifyDataSetChanged")
abstract class BaseGridPaginationAdapter<T>(
    open val interaction: Interaction<T>,
    private val gridCount: Int,
    private val isDarkTheme: Boolean,
    private val aspectRatio: Float = DEFAULT_RATIO,
    private val isAltLayout: Boolean = false,
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var errorMessage: String? = null
    var isLoading = true
    var isPaginating = false
    var canPaginate = true

    protected var arrayList: ArrayList<T> = arrayListOf()

    protected abstract fun handleDiffUtil(newList: ArrayList<T>)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(getItemViewType(position)) {
            RecyclerViewEnum.View.value -> {
                (holder as ItemViewHolderBind<T>).bind(arrayList[position], position, interaction)
            }
            RecyclerViewEnum.Error.value -> {
                (holder as ErrorViewHolderBind<T>).bind(errorMessage, interaction)
            }
            RecyclerViewEnum.PaginationLoading.value -> {
                if (!isAltLayout)
                (holder as PaginationLoadingViewHolderBind).bind(gridCount, aspectRatio, isDarkTheme)
            }
            RecyclerViewEnum.Loading.value -> {
                if (!isAltLayout)
                    (holder as LoadingViewHolderBind).bind(aspectRatio, isDarkTheme, false)
            }
            RecyclerViewEnum.PaginationExhaust.value -> {
                (holder as PaginationExhaustViewHolderBind<T>).bind(interaction)
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
            Constants.PAGINATION_LIMIT
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

    fun setData(
        newList: ArrayList<T>,
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
}