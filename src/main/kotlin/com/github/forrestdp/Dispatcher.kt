package com.github.forrestdp

import com.github.forrestdp.states.*
import com.github.kotlintelegrambot.dispatcher.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private var isAdmin = false

fun Dispatcher.dispatch() {
    command("start") { bot, update -> toHomeFirstTime(bot, update) }
    command("admin") { bot, update ->
        if (update.chatId != ADMIN_CHAT_ID) return@command
        isAdmin = true
        bot.sendMessage(update.chatId, "Now admin")
    }
    command("stopadmin") { bot, update ->
        if (update.chatId != ADMIN_CHAT_ID) return@command
        isAdmin = false
        bot.sendMessage(update.chatId, "Stop admin")
    }
    text(HOME_BUTTON_TEXT) { bot, update -> toHome(bot, update) }
    text(CATEGORIES_LIST_BUTTON_TEXT) { bot, update -> goToCategoriesList(bot, update) }
    text(CART_BUTTON_TEXT) { bot, update -> goToCartWithNewMessage(bot, update) }
    text(CHECKOUT_BUTTON_TEXT) { bot, update -> toCheckout(bot, update) }
    callbackQuery { bot, update -> toDynamicRouter(bot, update) }
    photos { bot, update, list ->
        if (update.chatId != ADMIN_CHAT_ID || !isAdmin) return@photos
        bot.sendMessage(
            update.chatId,
            list[0].fileId,
        )
    }
    text { bot, update ->
        if (update.chatId != ADMIN_CHAT_ID || !isAdmin) return@text
        val description = Json.encodeToString(update.message?.text)
        bot.sendMessage(
            update.chatId,
            description.trim('"'),
        )
    }
}
