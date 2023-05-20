package com.mrntlu.projectconsumer.adapters

import android.annotation.SuppressLint
import androidx.recyclerview.widget.RecyclerView
import com.mrntlu.projectconsumer.interfaces.*
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.RecyclerViewEnum

@Suppress("UNCHECKED_CAST")
@SuppressLint("NotifyDataSetChanged")
abstract class BaseAdapter<T>(open val interaction: Interaction<T>, private val gridCount: Int, private val isDarkTheme: Boolean): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
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
                (holder as PaginationLoadingViewHolderBind).bind(gridCount, isDarkTheme)
            }
            RecyclerViewEnum.Loading.value -> {
                (holder as LoadingViewHolderBind).bind(isDarkTheme)
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
        else if (isPaginating && position == arrayList.size)
            RecyclerViewEnum.PaginationLoading.value
        else if (!canPaginate && position == arrayList.size)
            RecyclerViewEnum.PaginationExhaust.value
        else if (arrayList.isEmpty())
            RecyclerViewEnum.Empty.value
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
        setState(RecyclerViewEnum.Loading)
        notifyDataSetChanged()
    }

    fun setData(
        newList: ArrayList<T>,
        isPaginationData: Boolean = false,
        isPaginationExhausted: Boolean = false,
        isPaginating: Boolean = false,
    ) {
        setState(
            if (isPaginationExhausted) RecyclerViewEnum.PaginationExhaust
            else if (isPaginating) RecyclerViewEnum.PaginationLoading
            else RecyclerViewEnum.View
        )

        if (!isPaginationData && !isPaginating) {
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