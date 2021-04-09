package com.github.forrestdp.states

import com.github.forrestdp.*
import com.github.forrestdp.entities.Category
import com.github.forrestdp.entities.allNotHiddenSortedById
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.*
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import org.jetbrains.exposed.sql.transactions.transaction

private const val CATEGORY_LIST_PREMESSAGE_TEXT = "Каталог"
private const val CATEGORY_LIST_MESSAGE_TEXT = "Выберите категорию товара:"

fun Bot.sendCategoryListMessage(chatId: Long): Unit = transaction {
    val categories = Category.allNotHiddenSortedById()
    val ikm = InlineKeyboardMarkup.create(listOf(
        categories.map { category ->
            InlineKeyboardButton.CallbackData(
                text = category.name,
                callbackData = ShowItemsCallbackData.of(category.id.value, 0).toJsonString(),
            )
        }
    ))
    val krm = KeyboardReplyMarkup.createSimpleKeyboard(
        listOf(listOf(HOME_BUTTON_TEXT)),
        resizeKeyboard = true,
    )
    sendMessage(ChatId.fromId(chatId), CATEGORY_LIST_PREMESSAGE_TEXT, replyMarkup = krm)
    sendMessage(ChatId.fromId(chatId), CATEGORY_LIST_MESSAGE_TEXT, replyMarkup = ikm)
}
