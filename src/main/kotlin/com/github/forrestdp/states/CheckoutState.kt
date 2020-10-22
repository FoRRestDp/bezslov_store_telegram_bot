package com.github.forrestdp.states

import com.github.forrestdp.chatId
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.Update

fun toCheckout(bot: Bot, update: Update) {
    val chatId = update.chatId
    bot.sendMessage(chatId, "Пока недоступно")
}