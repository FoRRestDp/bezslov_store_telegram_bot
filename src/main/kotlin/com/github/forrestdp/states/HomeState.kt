package com.github.forrestdp.states

import com.github.forrestdp.*
import com.github.forrestdp.tableentities.User
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.KeyboardButton
import com.github.kotlintelegrambot.entities.KeyboardReplyMarkup
import com.github.kotlintelegrambot.entities.Update
import org.jetbrains.exposed.sql.transactions.transaction

fun toHomeFirstTime(bot: Bot, update: Update) {
    val chatId = update.chatId
    transaction {
        if (User.findById(chatId) == null) {
            User.new(chatId) {}
        }
    }
    bot.sendHomeMessage(chatId)
}

fun toHome(bot: Bot, update: Update) {
    val chatId = update.chatId
    bot.sendHomeMessage(chatId)
}

private fun Bot.sendHomeMessage(chatId: Long) {
    val catalogButton = KeyboardButton(CATEGORIES_LIST_BUTTON_TEXT)
    val cartButton = KeyboardButton(CART_BUTTON_TEXT)
    val ordersButton = KeyboardButton(ORDERS_BUTTON_TEXT)
    val helpButton = KeyboardButton(HELP_BUTTON_TEXT)
    val mainKeyboardMarkup = KeyboardReplyMarkup(listOf(
        listOf(catalogButton, cartButton),
        listOf(ordersButton, helpButton),
    ), resizeKeyboard = true)
    this.sendMessage(
        chatId,
        "Добро пожаловать в магазин \"Без слов\"",
        replyMarkup = mainKeyboardMarkup
    )
}
