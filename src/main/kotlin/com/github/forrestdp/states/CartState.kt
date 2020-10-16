package com.github.forrestdp.states

import com.github.forrestdp.*
import com.github.forrestdp.tables.Items
import com.github.forrestdp.tables.SelectedItems
import com.github.forrestdp.tables.UsersItems
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.InlineKeyboardButton
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.Update
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal

fun toCart(bot: Bot, update: Update) {
    bot.showCart(update)
}

private fun Bot.showCart(update: Update) {
    data class NamePriceItemCountAllItemsCountAndIndexOfSelectedItem(
            val name: String,
            val price: BigDecimal?,
            val itemCount: Int,
            val allItemsCount: Long,
            val indexOfSelectedItem: Int,
    )

    val chatId = update.callbackQuery?.message?.chat?.id ?: throw Exception("Message is not defined")
    val selectedItemId = transaction {
        SelectedItems
                .slice(SelectedItems.usersItemsId)
                .select { SelectedItems.userChatId eq chatId }
                .firstOrNull()
                ?.getOrNull(SelectedItems.usersItemsId) ?: throw Exception("Selected item is not found")
    }
    val info = transaction {
        val query = (UsersItems innerJoin Items)
                .slice(Items.id, Items.name, Items.price, UsersItems.itemCount)
                .select {
                    (UsersItems.itemId eq Items.id) and (UsersItems.userChatId eq chatId)
                }
                .orderBy(Items.id)
        val allItemsCount = query.count()
        val resultRow = query
                .andWhere { UsersItems.itemId eq selectedItemId }
                .firstOrNull() ?: throw Exception("Name and price for cart are not found")
        val indexOfSelectedItem = query.map { it[Items.id].value }.indexOf(selectedItemId)

        NamePriceItemCountAllItemsCountAndIndexOfSelectedItem(
                resultRow[Items.name],
                resultRow[Items.price],
                resultRow[UsersItems.itemCount],
                allItemsCount,
                indexOfSelectedItem
        )
    }
    val name = info.name
    val price = info.price?.toString() ?: "--"
    val itemCount = info.itemCount
    val cost = info.price?.multiply(BigDecimal(itemCount))?.toString() ?: "--"
    val allUserItemsCount = info.allItemsCount
    val indexOfSelectedItem = info.indexOfSelectedItem

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