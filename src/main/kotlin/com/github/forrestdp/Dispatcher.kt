package com.github.forrestdp

import com.github.forrestdp.entities.deleteItemFromCart
import com.github.forrestdp.entities.setItemCountInCart
import com.github.forrestdp.entities.setItemCountInList
import com.github.forrestdp.states.*
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.dispatcher.*
import com.github.kotlintelegrambot.entities.Update
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun Dispatcher.dispatch() {
    command("start") { bot, update -> bot.goToHomeStateFirstTime(update) }
    command("admin") { bot, update ->
        val chatId = update.chatId
        if (Admin.setActiveAdmin(chatId, true)) {
            bot.sendMessage(chatId, "Now admin")
        }
    }
    command("stopadmin") { bot, update ->
        val chatId = update.chatId
        if (Admin.setActiveAdmin(chatId, false)) {
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
    val chatId: Long = update.chatId
    val jsonData = update.callbackQuery?.data ?: ""
    if (!jsonData.startsWith("{")) return

    when (val data = runCatching { Json.decodeFromString<CallbackData>(jsonData) }.getOrNull()) {
        is SetItemCountInListCallbackData -> {
            setItemCountInList(chatId, data.itemId, data.itemCountInList)
            bot.goToItemsListStateWithEditingMessage(update, data.itemId, data.itemCountInList)
        }
        is ShowItemsCallbackData -> {
            bot.goToItemsListStateWithNewMessage(chatId, data.categoryId, data.pageNumber)
        }
        is DeleteItemFromCartCallbackData -> {
            deleteItemFromCart(data.itemId, chatId)
            bot.goToCartStateWithEditingMessage(update, data.itemIndex)
        }
        is SetItemCountInCartCallbackData -> {
            setItemCountInCart(data.itemId, chatId, data.itemCountInCart)
            bot.goToCartStateWithEditingMessage(update, data.itemIndexInCart)
        }
        is SetItemIndexInCartCallbackData -> {
            bot.goToCartStateWithEditingMessage(update, data.itemIndex)
        }
        is ShowCategoriesCallbackData -> bot.goToCategoryListState(update)
        is ShowCartCallbackData -> bot.goToCartStateWithNewMessage(update)
        is CheckoutCallbackData -> bot.goToCheckoutState(update)
        is NoActionCallbackData -> {
        }
    }
}

private fun routeText(bot: Bot, update: Update) {
    val chatId = update.chatId
    if (Admin.isActiveAdmin(chatId)) {
        val description = Json.encodeToString(update.message?.text)
        with(bot) {
            sendMessage(chatId, "Отправляю текст, подготовленный для вставки в БД")
            sendMessage(chatId, description.trim('"'))
        }
        return
    }

    when (update.message?.text) {
        HOME_BUTTON_TEXT -> bot.goToHomeState(update)
        CATEGORIES_LIST_BUTTON_TEXT -> bot.goToCategoryListState(update)
        CART_BUTTON_TEXT -> bot.goToCartStateWithNewMessage(update)
        CHECKOUT_BUTTON_TEXT -> bot.goToCheckoutState(update)
        else -> bot.sendMessage(chatId, "Сообщение не распознано")
    }
}