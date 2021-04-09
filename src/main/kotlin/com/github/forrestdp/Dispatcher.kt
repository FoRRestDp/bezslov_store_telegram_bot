package com.github.forrestdp

import com.github.forrestdp.entities.deleteItemFromCart
import com.github.forrestdp.entities.setItemCountInCart
import com.github.forrestdp.entities.setItemCountInList
import com.github.forrestdp.states.*
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.dispatcher.*
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.Update
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun Dispatcher.dispatch() {
    command("start") { bot.sendHomeMessageAndAddUser(update) }
    command("admin") {
        val chatId = ChatId.fromId(update.chatId)
        if (Admin.activateAdmin(chatId)) {
            bot.sendMessage(chatId, "Now admin")
        }
    }
    command("stopadmin") {
        val chatId = ChatId.fromId(update.chatId)
        if (Admin.deactivateAdmin(chatId)) {
            bot.sendMessage(chatId, "Stop admin")
        }
    }
    callbackQuery { routeCallback(bot, update) }
    text { routeText(bot, update) }
    photos {
        val chatId = ChatId.fromId(update.chatId)
        if (Admin.isActiveAdmin(chatId)) {
            bot.sendMessage(chatId, "Возвращаем file id для этого изображения")
            bot.sendMessage(chatId, this.media[0].fileId)
        }
    }
}

private fun routeCallback(bot: Bot, update: Update) {
    val chatIdLong = update.chatId
    val chatId = ChatId.fromId(chatIdLong)
    val messageId = update.messageId
    val jsonData = update.callbackQuery?.data ?: ""

    val unused: Any = when (val data = Json.decodeFromStringOrNull<CallbackData>(jsonData)) {
        is SetItemCountInListCallbackData -> {
            setItemCountInList(chatIdLong, data.itemId, data.itemCountInList)
            bot.editItemListMessage(chatIdLong, messageId, data.itemId, data.itemCountInList)
        }
        is ShowItemsCallbackData -> {
            bot.sendItemListMessages(chatIdLong, data.categoryId, data.pageNumber)
        }
        is DeleteItemFromCartCallbackData -> {
            deleteItemFromCart(chatIdLong, data.itemId)
            bot.editCartMessage(chatIdLong, messageId, data.itemIndexInCart)
        }
        is SetItemCountInCartCallbackData -> {
            setItemCountInCart(chatIdLong, data.itemId, data.itemCountInCart)
            bot.editCartMessage(chatIdLong, messageId, data.itemIndexInCart)
        }
        is SetItemIndexInCartCallbackData -> {
            bot.editCartMessage(chatIdLong, messageId, data.itemIndexInCart)
        }
        is ShowCategoriesCallbackData -> bot.sendCategoryListMessage(chatIdLong)
        is ShowCartCallbackData -> bot.sendCartMessage(chatIdLong)
        is CheckoutCallbackData -> bot.sendCheckoutMessage(chatIdLong)
        is NoActionCallbackData -> {
        }
        null -> {
            bot.sendMessage(chatId, "Произошла ошибка, неверные данные callback data")
        }
    }
}

private inline fun <reified T> Json.Default.decodeFromStringOrNull(string: String): T? = try {
    Json.decodeFromString<T>(string)
} catch (e: SerializationException) {
    null
}

private fun routeText(bot: Bot, update: Update) {
    val chatIdLong = update.chatId
    val chatId = ChatId.fromId(chatIdLong)
    val text = update.message?.text
    if (Admin.isActiveAdmin(chatId)) {
        val description = Json.encodeToString(text).trim('"')
        with(bot) {
            sendMessage(chatId, "Отправляю текст, подготовленный для вставки в БД")
            sendMessage(chatId, description)
        }
        return
    }

    if (text?.startsWith("/") != false) return

    val unused: Any = when (text) {
        HOME_BUTTON_TEXT -> bot.sendHomeMessage(chatIdLong)
        CATEGORIES_LIST_BUTTON_TEXT -> bot.sendCategoryListMessage(chatIdLong)
        CART_BUTTON_TEXT -> bot.sendCartMessage(chatIdLong)
        CHECKOUT_BUTTON_TEXT -> bot.sendCheckoutMessage(chatIdLong)
        HELP_BUTTON_TEXT -> bot.sendMessage(chatId, "Пока недоступно")
        ORDERS_BUTTON_TEXT -> bot.sendMessage(chatId, "Пока недоступно")
        else -> bot.sendMessage(chatId, WRONG_COMMAND_RESPONSE)
    }
}