package com.example.kotlinspringrest.data.model

data class TaskUpdateRequest(
        val description: String?,
        val isReminderSet: Boolean?,
        val isTaskOpen: Boolean?,
        val priority: Priority?
)
