package com.github.forrestdp.states

import com.github.forrestdp.HOME_BUTTON_TEXT
import com.github.forrestdp.tableentities.Category
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.*
import org.jetbrains.exposed.sql.transactions.transaction

// List of all CATEGORIES of items

fun toCategoriesList(bot: Bot, update: Update) {
    val chatId = update.message?.chat?.id ?: error("Message not defined")
    transaction {
        val categories = Category
            .all()
            .filterNot { it.isHidden }
            .sortedBy { it.id }
        val inlineKeyboardMarkup = InlineKeyboardMarkup(listOf(
                categories
                        .map { InlineKeyboardButton(text = it.name, callbackData = "showItems_${it.id}") }
        ))
        val replyKeyboardMarkup = KeyboardReplyMarkup(KeyboardButton(HOME_BUTTON_TEXT))
        bot.sendMessage(chatId, "Каталог", replyMarkup = replyKeyboardMarkup)
        bot.sendMessage(chatId, "Выберите категорию товара:", replyMarkup = inlineKeyboardMarkup)
    }
}