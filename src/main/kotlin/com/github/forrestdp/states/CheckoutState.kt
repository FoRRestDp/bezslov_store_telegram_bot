package com.github.forrestdp.states

import com.github.forrestdp.chatId
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.Update

fun Bot.sendCheckoutMessage(chatId: Long) {
    sendMessage(chatId, "Пока недоступно")
}
