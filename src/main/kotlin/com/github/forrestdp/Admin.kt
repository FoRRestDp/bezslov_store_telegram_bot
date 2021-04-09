package com.github.forrestdp

import com.github.kotlintelegrambot.entities.ChatId

object Admin {
    private val activeAdmins = mutableSetOf<ChatId>()
    private val admins = setOf(ChatId.fromId(165239411L))
    
    fun isActiveAdmin(chatId: ChatId): Boolean = activeAdmins.contains(chatId)
    
    fun activateAdmin(chatId: ChatId): Boolean = if (admins.contains(chatId)) {
        activeAdmins.add(chatId)
        true
    } else {
        false
    }

    fun deactivateAdmin(chatId: ChatId): Boolean = activeAdmins.remove(chatId)
}