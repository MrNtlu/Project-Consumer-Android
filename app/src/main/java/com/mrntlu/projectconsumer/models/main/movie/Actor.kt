package com.mrntlu.projectconsumer.models.main.movie

data class Actor(
    val name: String,
    val image: String,
    val character: String,
) {
    constructor(): this("", "", "")
}
