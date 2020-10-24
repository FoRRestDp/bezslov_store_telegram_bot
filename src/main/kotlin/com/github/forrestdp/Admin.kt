package com.github.forrestdp

object Admin {
    fun isActiveAdmin(chatId: Long): Boolean = adminUserActivity[chatId] ?: false
    fun setActiveAdmin(chatId: Long, isActive: Boolean): Boolean = if (isAdmin(chatId)) {
        adminUserActivity[chatId] = isActive
        true
    } else {
        false
    }
    private fun isAdmin(chatId: Long): Boolean = adminUserActivity.containsKey(chatId)
    private val adminUserActivity = mutableMapOf(165239411L to false)
}