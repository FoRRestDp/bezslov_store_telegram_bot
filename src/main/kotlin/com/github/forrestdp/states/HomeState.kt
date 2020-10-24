package com.github.forrestdp.states

import com.github.forrestdp.*
import com.github.forrestdp.entities.User
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.KeyboardReplyMarkup
import com.github.kotlintelegrambot.entities.Update
import org.jetbrains.exposed.sql.transactions.transaction

fun Bot.goToHomeStateFirstTime(update: Update) {
    val chatId = update.chatId
    insertIgnoreUser(chatId)
    sendHomeMessage(chatId)
}

fun Bot.goToHomeState(update: Update) = sendHomeMessage(update.chatId)

private const val HOME_MESSAGE_TEXT = "Добро пожаловать в магазин \"Без слов\""

private fun Bot.sendHomeMessage(chatId: Long) {
    val krm = KeyboardReplyMarkup.createSimpleKeyboard(
        listOf(
            listOf(CATEGORIES_LIST_BUTTON_TEXT, CART_BUTTON_TEXT),
            listOf(ORDERS_BUTTON_TEXT, HELP_BUTTON_TEXT),
        ),
        resizeKeyboard = true
    )
    sendMessage(chatId, HOME_MESSAGE_TEXT, replyMarkup = krm)
}

private fun insertIgnoreUser(chatId: Long) = transaction {
    if (User.findById(chatId) == null) {
        User.new(chatId) {}
    }
}
