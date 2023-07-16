package com.mrntlu.projectconsumer.models.main.userList

data class LogsByDate(
    val date: String,
    val count: Int,
    val data: List<Log>,
)