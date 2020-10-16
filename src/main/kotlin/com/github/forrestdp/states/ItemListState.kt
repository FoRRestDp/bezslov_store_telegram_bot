package com.github.forrestdp.states

import com.github.forrestdp.tableentities.Category
import com.github.forrestdp.tableentities.Item
import com.github.forrestdp.tableentities.User
import com.github.forrestdp.tableentities.getItemCountInUserCart
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.InlineKeyboardButton
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.Update
import org.jetbrains.exposed.sql.transactions.transaction

@Suppress("UNUSED_PARAMETER")
fun toItemsList(bot: Bot, update: Update, chatId: Long, data: String) {
    val categoryId = data.split('_')[1].toIntOrNull()
        ?: throw Exception("Product id is not defined")

    transaction {
        val user = User.findById(chatId) ?: error("User with this chat id not found")
        val items = Item.all().filter { item ->
            !item.isHidden &&
                    item.category.id.value == categoryId &&
                    item.owningUsers.contains(user)
        }
        val category = Category.findById(categoryId) ?: "неизвестно какой"
        for (item in items) {
            val id = item.id.value
            val name = item.name
            val description = item.description ?: "его пока нет"
            val price = item.price?.toString() ?: "--"
            val itemsCount = getItemCountInUserCart(user, item)
            val responseText = """
                                $name
                                (В категории: $category)
                                Описание: $description
                            """.trimIndent()
            val imk = InlineKeyboardMarkup(
                listOf(
                    listOf(
                        InlineKeyboardButton(
                            "Добавить в корзину \u2013 $price ₽ \n (В корзине: $itemsCount)",
                            callbackData = "addItemToCart_$id"
                        )
                    )
                )
            )
            bot.sendMessage(chatId, responseText, replyMarkup = imk)
        }
    }
}