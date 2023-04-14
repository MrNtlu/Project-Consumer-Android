package com.mrntlu.projectconsumer.interfaces

interface ItemViewHolderBind<T> {
    fun bind(item: T, position: Int, interaction: Interaction<T>)
}

interface ErrorViewHolderBind<T> {
    fun bind(errorMessage: String?, interaction: Interaction<T>)
}