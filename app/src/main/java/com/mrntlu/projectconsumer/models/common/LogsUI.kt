package com.mrntlu.projectconsumer.models.common

import com.mrntlu.projectconsumer.models.main.userList.Log

data class LogsUI(
    val log: Log,
    val isHeader: Boolean,
)