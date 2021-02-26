package com.github.forrestdp

object Admin {
    private val activeAdmins = mutableSetOf<Long>()
    private val admins = setOf(165239411L)
    
    fun isActiveAdmin(chatId: Long): Boolean = activeAdmins.contains(chatId)
    
    fun activateAdmin(chatId: Long): Boolean = if (admins.contains(chatId)) {
        activeAdmins.add(chatId)
        true
    } else {
        false
    }

    fun deactivateAdmin(chatId: Long): Boolean = activeAdmins.remove(chatId)
}