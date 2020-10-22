package com.github.forrestdp.states

import com.github.forrestdp.AddItemToCartCommand
import com.github.forrestdp.ShowCartCommand
import com.github.forrestdp.getAddToCartText
import com.github.forrestdp.tableentities.CartItem
import com.github.forrestdp.tableentities.Item
import com.github.forrestdp.tableentities.User
import com.github.forrestdp.tableentities.findByIdNotHidden
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.InlineKeyboardButton
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.Update
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.transactions.transaction

fun addItemToCart(bot: Bot, update: Update, chatId: Long, selectedItemId: Int) {
    val selectedItemCount = transaction {
        val user = User.findById(chatId) ?: error("User with such chat id not found")
        val item = Item.findByIdNotHidden(selectedItemId) ?: error("Item with such id not found or hidden")
        val cartItem = CartItem.all().firstOrNull {
            it.item == item && it.user == user
        }
        if (cartItem == null) {
            CartItem.new {
                this.item = item
                this.user = user
            }
        } else {
            cartItem.itemCount++
        }
        CartItem.all().firstOrNull {
            it.item == item && it.user == user
        }?.itemCount ?: error("Cart item has not been added")
    }
    val messageId = update.callbackQuery?.message?.messageId
        ?: error("Message is not defined")
    val price = transaction {
        Item.findByIdNotHidden(selectedItemId)?.price?.toString() ?: "--"
    }
    val result = bot.editMessageReplyMarkup(chatId, messageId, replyMarkup = InlineKeyboardMarkup(listOf(
        listOf(InlineKeyboardButton(
            getAddToCartText(price, selectedItemCount),
            callbackData = Json.encodeToString(AddItemToCartCommand.new(selectedItemId))
        )),
        listOf(InlineKeyboardButton(
            "Перейти в корзину",
            callbackData = Json.encodeToString(ShowCartCommand.new())
        ))
    )))
    bot.sendMessage(
        chatId,
        result.first?.errorBody()?.string() ?: "O_o"
    )
}
