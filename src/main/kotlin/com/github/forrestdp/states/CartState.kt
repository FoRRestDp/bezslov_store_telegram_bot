package com.github.forrestdp.states

import com.github.forrestdp.*
import com.github.forrestdp.tableentities.CartItem
import com.github.forrestdp.tableentities.SelectedItem
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.InlineKeyboardButton
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.Update
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal

fun toCart(bot: Bot, update: Update) {
    bot.showCart(update)
}

private fun Bot.showCart(update: Update) {
    val chatId = update.callbackQuery?.message?.chat?.id ?: error("Message is not defined")
    val selectedItem = transaction {
        SelectedItem.all().firstOrNull { it.user.chatId == chatId }
    }
    
    if (selectedItem == null) {
        this.sendMessage(chatId,
        """
            В корзине пусто
        """.trimIndent())
        return
    }
    
    val cartItem = transaction { selectedItem.cartItem }
    val name = transaction { cartItem.item.name }
    val price = transaction { cartItem.item.price }
    val itemCount = transaction { cartItem.itemCount }
    val cost = price?.multiply(BigDecimal(itemCount))?.toString() ?: "--"
    val allUserItemsCount = transaction { CartItem.all().filter { it.user.chatId == chatId }.size }
    val indexOfSelectedItem = transaction { CartItem.all().indexOf(cartItem) }

    val removeAllButton = InlineKeyboardButton("X", callbackData = REMOVE_FROM_CART_CALLBACK)
    val addOneButton = InlineKeyboardButton("+", callbackData = ADD_ONE_TO_CART_CALLBACK)
    val itemCountButton = InlineKeyboardButton("$itemCount шт.")
    val removeOneButton = InlineKeyboardButton("-", callbackData = REMOVE_ONE_FROM_CART_CALLBACK)
    val previousButton = InlineKeyboardButton("<-", callbackData = PREVIOUS_ONE_IN_CART_CALLBACK)
    val indexButton = InlineKeyboardButton("$indexOfSelectedItem/$allUserItemsCount")
    val nextButton = InlineKeyboardButton("->", callbackData = NEXT_ONE_IN_CART_CALLBACK)
    val checkoutButton = InlineKeyboardButton("Оформить заказ", callbackData = CHECKOUT_CALLBACK)
    val catalogButton = InlineKeyboardButton("Продолжить покупки", callbackData = CATALOG_CALLBACK)

    val imk = InlineKeyboardMarkup(listOf(
            listOf(removeAllButton, addOneButton, itemCountButton, removeOneButton),
            listOf(previousButton, indexButton, nextButton),
            listOf(checkoutButton),
            listOf(catalogButton)
    ))

    this.sendMessage(
            chatId,
            """
                Корзина:
                $name
                $itemCount шт. * $price ₽ = $cost ₽
            """.trimIndent(),
            replyMarkup = imk
    )
}