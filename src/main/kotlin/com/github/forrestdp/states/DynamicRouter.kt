package com.github.forrestdp.states

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.Update

// This function routes to other states
// This is used for dynamic number of possible callbacks

fun toDynamicRouter(bot: Bot, update: Update) {
    val chatId: Long = update.callbackQuery?.message?.chat?.id
            ?: error("Callback query or it's message is not defined")
    val data = update.callbackQuery?.data ?: ""
    when {
        data.startsWith("addItemToCart") -> { addItemToCart(bot, update, chatId, data) }
        data.startsWith("showItems") -> { toItemsList(bot, update, chatId, data) }
    }
}