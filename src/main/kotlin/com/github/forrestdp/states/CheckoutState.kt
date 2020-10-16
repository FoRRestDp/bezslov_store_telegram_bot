package com.github.forrestdp.states

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.Update

fun toCheckout(bot: Bot, update: Update) {
    val chatId = update.message?.chat?.id ?: throw Exception("Message is not defined")
    bot.sendMessage(chatId, "Пока недоступно")
}