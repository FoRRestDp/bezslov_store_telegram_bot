package com.github.forrestdp

import com.github.kotlintelegrambot.entities.Update

val Update.chatId: Long
    get() = message?.chat?.id
        ?: callbackQuery?.message?.chat?.id
        ?: error("Chat id is not defined")

val Update.messageId: Long
get() = message?.messageId
    ?: callbackQuery?.message?.messageId
    ?: error("Message id is not defined")