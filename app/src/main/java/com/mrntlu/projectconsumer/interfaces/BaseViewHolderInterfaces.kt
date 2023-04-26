package com.mrntlu.projectconsumer.interfaces

interface ItemViewHolderBind<T> {
    fun bind(item: T, position: Int, interaction: Interaction<T>)
}

interface ErrorViewHolderBind<T> {
    fun bind(errorMessage: String?, interaction: Interaction<T>)
}

interface LoadingViewHolderBind {
    fun bind(isDarkTheme: Boolean)
}

interface PaginationLoadingViewHolderBind {
    fun bind(gridCount: Int, isDarkTheme: Boolean)
}

interface PaginationExhaustViewHolderBind<T> {
    fun bind(interaction: Interaction<T>)
}