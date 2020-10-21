package com.github.forrestdp.states

import com.github.forrestdp.*
import com.github.forrestdp.tableentities.CartItem
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.InlineKeyboardButton
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.Update
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal

private fun CartItem.Companion.sortedCartItemsWithChatId(chatId: Long) =
    all().filter { it.user.chatId.value == chatId }.sortedBy { it.id.value }

fun goToCartWithNewMessage(bot: Bot, update: Update) {
    showCartFirstTime(bot, update)
}

fun goToCartWithEditingMessage(bot: Bot, update: Update, selectedItemIndex: Int) {
    showEditedCart(bot, update, selectedItemIndex)
}

fun deleteItemFromCart(itemId: Int, chatId: Long) {
    transaction {
        CartItem.all().firstOrNull { it.item.id.value == itemId && it.user.chatId.value == chatId }?.delete()
            ?: error("Item with such id not found")
    }
}

fun setItemCountInCart(itemId: Int, chatId: Long, itemCount: Int) {
    assert(itemCount >= 0)
    if (itemCount == 0) {
        deleteItemFromCart(itemId, chatId)
        return
    }
    transaction {
        val cartItem = CartItem.all().firstOrNull { it.item.id.value == itemId && it.user.chatId.value == chatId }
            ?: error("Cart item with such id and user chat id is not found")
        cartItem.itemCount = itemCount
    }
}

private fun showEditedCart(bot: Bot, update: Update, selectedItemIndex: Int) {
    val chatId = update.message?.chat?.id
        ?: update.callbackQuery?.message?.chat?.id
        ?: error("Message is not defined")
    val messageId = update.message?.messageId
        ?: update.callbackQuery?.message?.messageId
        ?: error("Message is not defines")
    val cartItem = transaction {
        CartItem.sortedCartItemsWithChatId(chatId)
            .getOrNull(selectedItemIndex)
    }

    val (ikm, text) = getIkmAndText(cartItem, chatId)

    bot.editMessageText(
        chatId,
        messageId,
        text = text,
        replyMarkup = ikm
    )
}

private fun showCartFirstTime(bot: Bot, update: Update) {
    val chatId = update.message?.chat?.id
        ?: update.callbackQuery?.message?.chat?.id
        ?: error("Message is not defined")
    val cartItem = transaction {
        CartItem.sortedCartItemsWithChatId(chatId).firstOrNull()
    }

    val (ikm, text) = getIkmAndText(cartItem, chatId)

    bot.sendMessage(
        chatId,
        text = text,
        replyMarkup = ikm
    )
}

private fun getIkmAndText(cartItem: CartItem?, chatId: Long): Pair<InlineKeyboardMarkup?, String> {
    if (cartItem == null) {
        return null to """
            В корзине пусто
        """.trimIndent()
    }

    val itemId = transaction { cartItem.item.id.value }
    val name = transaction { cartItem.item.name }
    val price = transaction { cartItem.item.price }
    val itemCount = transaction { cartItem.itemCount }
    val cost = price?.multiply(BigDecimal(itemCount))?.toString() ?: "--"
    val allUserItemsCount = transaction { CartItem.all().filter { it.user.chatId.value == chatId }.size }
    val indexOfSelectedItem = transaction {
        CartItem.sortedCartItemsWithChatId(chatId).map { it.id.value }.indexOf(cartItem.id.value)
    }
    val normalizedIndexOfSelectedFile = indexOfSelectedItem + 1

    val removeAllButton = InlineKeyboardButton(
        "❌",
        // TODO изменить itemIndex на нормальное значение
        callbackData = Json.encodeToString(DeleteItemFromCartCommand.new(itemId, 0))
    )
    val itemCountPlusOne = itemCount + 1
    val addOneButton = InlineKeyboardButton(
        "➕",
        callbackData = Json.encodeToString(SetItemCountInCartCommand.new(itemId, indexOfSelectedItem, itemCountPlusOne))
    )
    val itemCountButton = InlineKeyboardButton(
        "$itemCount шт.",
        callbackData = NO_ACTION_CALLBACK
    )
    val itemCountMinusOne = itemCount - 1
    val removeOneButton = InlineKeyboardButton(
        "➖",
        callbackData = if (itemCountMinusOne == 0) {
            // TODO изменить itemIndex на нормальное значение
            Json.encodeToString(DeleteItemFromCartCommand.new(itemId, 0))
        } else {
            Json.encodeToString(SetItemCountInCartCommand.new(itemId, indexOfSelectedItem, itemCountMinusOne))
        }
    )
    val previousIndex = if (indexOfSelectedItem == 0) allUserItemsCount - 1 else indexOfSelectedItem - 1
    val previousButton = InlineKeyboardButton(
        "⬅️",
        callbackData = Json.encodeToString(SelectAnotherItemFromCartCommand.new(previousIndex))
    )
    val indexButton = InlineKeyboardButton(
        "$normalizedIndexOfSelectedFile/$allUserItemsCount",
        callbackData = NO_ACTION_CALLBACK,
    )
    val nextIndex = if (indexOfSelectedItem + 1 == allUserItemsCount) 0 else indexOfSelectedItem + 1
    val nextButton = InlineKeyboardButton(
        "➡️",
        callbackData = Json.encodeToString(SelectAnotherItemFromCartCommand.new(nextIndex))
    )
    val checkoutButton = InlineKeyboardButton(
        "Оформить заказ",
        callbackData = CHECKOUT_CALLBACK
    )
    val catalogButton = InlineKeyboardButton(
        "Продолжить покупки",
        callbackData = CATALOG_CALLBACK
    )

    val ikm = InlineKeyboardMarkup(
        listOf(
            listOf(removeAllButton, addOneButton, itemCountButton, removeOneButton),
            listOf(previousButton, indexButton, nextButton),
            listOf(checkoutButton),
            listOf(catalogButton),
        )
    )

    return ikm to """
                Корзина:
                $name
                $itemCount шт. * $price ₽ = $cost ₽
            """.trimIndent()
}