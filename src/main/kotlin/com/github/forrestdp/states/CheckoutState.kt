package com.github.forrestdp.states

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId

fun Bot.sendCheckoutMessage(chatId: Long) {
    sendMessage(ChatId.fromId(chatId), "Пока недоступно")
}
