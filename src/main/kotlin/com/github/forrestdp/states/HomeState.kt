package com.github.forrestdp.states

import com.github.forrestdp.CART_BUTTON_TEXT
import com.github.forrestdp.CATEGORIES_LIST_BUTTON_TEXT
import com.github.forrestdp.HELP_BUTTON_TEXT
import com.github.forrestdp.ORDERS_BUTTON_TEXT
import com.github.forrestdp.tableentities.User
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.KeyboardButton
import com.github.kotlintelegrambot.entities.KeyboardReplyMarkup
import com.github.kotlintelegrambot.entities.Update
import org.jetbrains.exposed.sql.transactions.transaction

fun toHomeFirstTime(bot: Bot, update: Update) {
    val chatId = update.message?.chat?.id ?: error("Message not defined")
    transaction {
        if (User.findById(chatId) == null) {
            User.new(chatId) {}
        }
    }
    bot.sendHomeMessage(chatId)
}

fun toHome(bot: Bot, update: Update) {
    val chatId = update.message?.chat?.id ?: error("Message not defined")
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