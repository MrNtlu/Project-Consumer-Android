package com.mrntlu.projectconsumer.adapters

import android.annotation.SuppressLint
import androidx.recyclerview.widget.RecyclerView
import com.mrntlu.projectconsumer.interfaces.ErrorViewHolderBind
import com.mrntlu.projectconsumer.interfaces.Interaction
import com.mrntlu.projectconsumer.interfaces.ItemViewHolderBind
import com.mrntlu.projectconsumer.interfaces.PaginationExhaustViewHolderBind
import com.mrntlu.projectconsumer.utils.RecyclerViewEnum

@Suppress("UNCHECKED_CAST")
@SuppressLint("NotifyDataSetChanged")
abstract class BaseAdapter<T>(open val interaction: Interaction<T>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
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
        return if (isLoading || errorMessage != null || arrayList.isEmpty())
            1
        else {
            if (arrayList.isNotEmpty() && !isPaginating && canPaginate) //View Type
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

    fun setLoadingView(isPaginating: Boolean) {
        if (isPaginating) {
            setState(RecyclerViewEnum.PaginationLoading)
            notifyItemInserted(itemCount)
        } else {
            setState(RecyclerViewEnum.Loading)
            notifyDataSetChanged()
        }
    }

    fun setData(
        newList: ArrayList<T>,
        isPaginationData: Boolean = false,
        isPaginationExhausted: Boolean = false
    ) {
        setState(if (isPaginationExhausted) RecyclerViewEnum.PaginationExhaust else RecyclerViewEnum.View)

        if (!isPaginationData) {
            if (arrayList.isNotEmpty())
                arrayList.clear()
            arrayList.addAll(newList)
            notifyDataSetChanged()
        } else {
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
            }
            RecyclerViewEnum.Loading -> {
                isLoading = true
                isPaginating = false
                errorMessage = null
                canPaginate = true
            }
            RecyclerViewEnum.Error -> {
                isLoading = false
                isPaginating = false
            }
            RecyclerViewEnum.View -> {
                isLoading = false
                isPaginating = false
                errorMessage = null
            }
            RecyclerViewEnum.PaginationLoading -> {
                isLoading = false
                isPaginating = true
                errorMessage = null
            }
            RecyclerViewEnum.PaginationExhaust -> {
                isLoading = false
                isPaginating = false
                canPaginate = false
            }
        }
    }
}