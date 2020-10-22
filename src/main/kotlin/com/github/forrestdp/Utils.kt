package com.github.forrestdp

import com.github.kotlintelegrambot.entities.Update

val Update.chatId: Long
    get() = message?.chat?.id
        ?: callbackQuery?.message?.chat?.id
        ?: error("Message is not defined")

fun getAddToCartText(price: String, itemCount: Int) =
    "Добавить в корзину \u2013 $price ₽ \n (В корзине: $itemCount)"