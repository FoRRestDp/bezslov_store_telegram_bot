package com.github.forrestdp

import com.github.forrestdp.states.*
import com.github.kotlintelegrambot.dispatcher.Dispatcher
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.text

fun Dispatcher.dispatch() {
    // Home
    command("start") { bot, update -> toHomeWithCommand(bot, update) }
    text(HOME_BUTTON_TEXT) { bot, update -> toHomeWithButton(bot, update) }
    // Catalog
    text(CATEGORIES_LIST_BUTTON_TEXT) { bot, update -> toCategoriesList(bot, update) }
    // User cart
    text(CART_BUTTON_TEXT) { bot, update -> toCart(bot, update) }
    callbackQuery(CART_CALLBACK) { bot, update -> toCart(bot, update) }
    // Checkout
    text(CHECKOUT_BUTTON_TEXT) { bot, update -> toCheckout(bot, update) }
    // Other
    callbackQuery { bot, update ->  toDynamicRouter(bot, update)}
}