package com.github.forrestdp.states

import com.github.forrestdp.*
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.Update
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

// This function routes to other states
// This is used for dynamic number of possible callbacks

fun toDynamicRouter(bot: Bot, update: Update) {
    val chatId: Long = update.chatId
    val jsonData = update.callbackQuery?.data ?: ""
    if (!jsonData.startsWith("{")) return
    val data = runCatching {
        Json.decodeFromString<CallbackCommand>(jsonData)
    }.getOrNull() ?: return
    
    when (data) {
        is AddItemToCartCommand -> {
            addItemToCart(bot, update, chatId, data.itemId)
        }
        is ShowItemsCommand -> {
            goToItemsList(bot, update, chatId, data.categoryId, data.pageNumber)
        }
        is DeleteItemFromCartCommand -> {
            deleteItemFromCart(data.itemId, chatId)
            goToCartWithEditingMessage(bot, update, data.itemIndex)
        }
        is SetItemCountInCartCommand -> {
            setItemCountInCart(data.itemId, chatId, data.itemCount)
            goToCartWithEditingMessage(bot, update, data.itemIndex)
        }
        is SelectAnotherItemFromCartCommand -> {
            goToCartWithEditingMessage(bot, update, data.itemIndex)
        }
        is ShowCategoriesCommand -> goToCategoriesList(bot, update)
        is ShowCartCommand -> goToCartWithNewMessage(bot, update)
    }
}