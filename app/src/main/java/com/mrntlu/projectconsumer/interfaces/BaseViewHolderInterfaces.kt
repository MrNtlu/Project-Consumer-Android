package com.mrntlu.projectconsumer.interfaces

interface ItemViewHolderBind<T> {
    fun bind(item: T, position: Int, interaction: Interaction<T>)
}

interface ErrorViewHolderBind<T> {
    fun bind(errorMessage: String?, interaction: Interaction<T>, shouldHideCancelButton: Boolean = false)
}

interface LoadingViewHolderBind {
    fun bind(aspectRatio: Float?, isDarkTheme: Boolean)
}

interface PaginationLoadingViewHolderBind {
    fun bind(gridCount: Int, aspectRatio: Float?, isDarkTheme: Boolean)
}

interface PaginationExhaustViewHolderBind<T> {
    fun bind(interaction: Interaction<T>)
}