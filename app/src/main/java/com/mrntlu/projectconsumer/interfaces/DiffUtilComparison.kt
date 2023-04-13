package com.mrntlu.projectconsumer.interfaces

interface DiffUtilComparison<Model> {
    fun areItemsTheSame(newItem: Model): Boolean

    fun areContentsTheSame(newItem: Model): Boolean
}