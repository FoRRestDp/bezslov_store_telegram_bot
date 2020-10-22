package com.github.forrestdp.states

import com.github.forrestdp.*
import com.github.forrestdp.tableentities.Category
import com.github.forrestdp.tableentities.allNotHidden
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.transactions.transaction

// List of all CATEGORIES of items

fun goToCategoriesList(bot: Bot, update: Update) {
    val chatId = update.chatId
    transaction {
        val categories = Category
            .allNotHidden()
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
        val replyKeyboardMarkup = KeyboardReplyMarkup(
            listOf(listOf(
                KeyboardButton(HOME_BUTTON_TEXT),
            )),
            resizeKeyboard = true,
        )
        bot.sendMessage(chatId, "Каталог", replyMarkup = replyKeyboardMarkup)
        bot.sendMessage(chatId, "Выберите категорию товара:", replyMarkup = inlineKeyboardMarkup)
    }
}
