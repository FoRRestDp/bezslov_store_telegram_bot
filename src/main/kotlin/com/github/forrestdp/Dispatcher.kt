package com.github.forrestdp

import com.github.forrestdp.entities.deleteItemFromCart
import com.github.forrestdp.entities.setItemCountInCart
import com.github.forrestdp.entities.setItemCountInList
import com.github.forrestdp.states.*
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.dispatcher.*
import com.github.kotlintelegrambot.entities.Update
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun Dispatcher.dispatch() {
    command("start") { bot, update -> bot.sendHomeMessageAndAddUser(update) }
    command("admin") { bot, update ->
        val chatId = update.chatId
        if (Admin.activateAdmin(chatId)) {
            bot.sendMessage(chatId, "Now admin")
        }
    }
    command("stopadmin") { bot, update ->
        val chatId = update.chatId
        if (Admin.deactivateAdmin(chatId)) {
            bot.sendMessage(update.chatId, "Stop admin")
        }
    }
    callbackQuery { bot, update -> routeCallback(bot, update) }
    text { bot, update -> routeText(bot, update) }
    photos { bot, update, list ->
        val chatId = update.chatId
        if (Admin.isActiveAdmin(chatId)) {
            with(bot) {
                sendMessage(chatId, "Возвращаем file id для этого изображения")
                sendMessage(chatId, list[0].fileId)
            }
        }
    }
}

private fun routeCallback(bot: Bot, update: Update) {
    val chatId = update.chatId
    val messageId = update.messageId
    val jsonData = update.callbackQuery?.data ?: ""

    val unused: Any = when (val data = Json.decodeFromStringOrNull<CallbackData>(jsonData)) {
        is SetItemCountInListCallbackData -> {
            setItemCountInList(chatId, data.itemId, data.itemCountInList)
            bot.editItemListMessage(chatId, messageId, data.itemId, data.itemCountInList)
        }
        is ShowItemsCallbackData -> {
            bot.sendItemListMessages(chatId, data.categoryId, data.pageNumber)
        }
        is DeleteItemFromCartCallbackData -> {
            deleteItemFromCart(chatId, data.itemId)
            bot.editCartMessage(chatId, messageId, data.itemIndexInCart)
        }
        is SetItemCountInCartCallbackData -> {
            setItemCountInCart(chatId, data.itemId, data.itemCountInCart)
            bot.editCartMessage(chatId, messageId, data.itemIndexInCart)
        }
        is SetItemIndexInCartCallbackData -> {
            bot.editCartMessage(chatId, messageId, data.itemIndexInCart)
        }
        is ShowCategoriesCallbackData -> bot.sendCategoryListMessage(chatId)
        is ShowCartCallbackData -> bot.sendCartMessage(chatId)
        is CheckoutCallbackData -> bot.sendCheckoutMessage(chatId)
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
    val chatId = update.chatId
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
        HOME_BUTTON_TEXT -> bot.sendHomeMessage(chatId)
        CATEGORIES_LIST_BUTTON_TEXT -> bot.sendCategoryListMessage(chatId)
        CART_BUTTON_TEXT -> bot.sendCartMessage(chatId)
        CHECKOUT_BUTTON_TEXT -> bot.sendCheckoutMessage(chatId)
        HELP_BUTTON_TEXT -> bot.sendMessage(chatId, "Пока недоступно")
        ORDERS_BUTTON_TEXT -> bot.sendMessage(chatId, "Пока недоступно")
        else -> bot.sendMessage(chatId, WRONG_COMMAND_RESPONSE)
    }
}