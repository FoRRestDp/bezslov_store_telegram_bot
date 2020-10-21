package com.github.forrestdp.states

import com.github.forrestdp.HOME_BUTTON_TEXT
import com.github.forrestdp.CallbackCommand
import com.github.forrestdp.ShowItemsCommand
import com.github.forrestdp.tableentities.Category
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.transactions.transaction

// List of all CATEGORIES of items

fun toCategoriesList(bot: Bot, update: Update) {
    val chatId = update.message?.chat?.id ?: error("Message not defined")
    transaction {
        val categories = Category
            .all()
            .filterNot { it.isHidden }
            .sortedBy { it.id }
        println(categories.size)
        val inlineKeyboardMarkup = InlineKeyboardMarkup(listOf(
            categories
                .map {
                    InlineKeyboardButton(
                        text = it.name,
                        callbackData = Json.encodeToString(ShowItemsCommand.new(it.id.value, 0))
                    )
                }
        ))
        val replyKeyboardMarkup = KeyboardReplyMarkup(KeyboardButton(HOME_BUTTON_TEXT), resizeKeyboard = true)
        bot.sendMessage(chatId, "Каталог", replyMarkup = replyKeyboardMarkup)
        bot.sendMessage(chatId, "Выберите категорию товара:", replyMarkup = inlineKeyboardMarkup)
    }
}