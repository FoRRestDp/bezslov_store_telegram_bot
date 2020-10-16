package com.github.forrestdp.states

import com.github.forrestdp.CART_CALLBACK
import com.github.forrestdp.tableentities.Cart
import com.github.forrestdp.tableentities.Item
import com.github.forrestdp.tableentities.User
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.InlineKeyboardButton
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.Update
import org.jetbrains.exposed.sql.transactions.transaction

fun addItemToCart(bot: Bot, update: Update, chatId: Long, data: String) {
    val selectedItemId = data.split("_")[1].toIntOrNull()
            ?: error("Product id is not defined")
    val selectedItemCount = transaction {
        val user = User.findById(chatId) ?: error("User with such chat id not found")
        val item = Item.findById(selectedItemId) ?: error("Item with such id not found")
        val cart = Cart.all().firstOrNull { 
            it.item == item && it.user == user
        }
        if (cart == null) {
            Cart.new { 
                this.item = item
                this.user = user
            }
        } else {
            cart.itemCount++
        }
        cart?.itemCount ?: 0
    }
    val messageId = update.callbackQuery?.message?.messageId
            ?: error("Message is not defined")
    val price = transaction {
        Item.findById(selectedItemId)?.price?.toString() ?: "--"
    }
    bot.editMessageReplyMarkup(chatId, messageId, replyMarkup = InlineKeyboardMarkup(listOf(
            listOf(InlineKeyboardButton(
                    "Добавить в корзину \u2013 $price ₽ \n (В корзине: ${selectedItemCount + 1})",
                    callbackData = "addItemToCart_$selectedItemId"
            )),
            listOf(InlineKeyboardButton("В корзину", callbackData = CART_CALLBACK))
    )))
}