package com.github.forrestdp.states

import com.github.forrestdp.HOME_BUTTON_TEXT
import com.github.forrestdp.AddItemToCartCommand
import com.github.forrestdp.CallbackCommand
import com.github.forrestdp.tableentities.Category
import com.github.forrestdp.tableentities.Item
import com.github.forrestdp.tableentities.User
import com.github.forrestdp.tableentities.getItemCountInUserCart
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.transactions.transaction

private const val ITEM_LIST_PAGE_SIZE = 5

fun toItemsList(
    bot: Bot,
    @Suppress("UNUSED_PARAMETER") update: Update,
    chatId: Long,
    categoryId: Int,
    pageNumber: Int
) {
    transaction {
        val user = User.findById(chatId) ?: error("User with this chat id not found")
        val items = Item.all().filter { item ->
            !item.isHidden && item.category.id.value == categoryId
        }.takePage(pageNumber, ITEM_LIST_PAGE_SIZE)
        val categoryName = Category.findById(categoryId)?.name ?: "неизвестно какой"
        for (item in items) {
            val id = item.id.value
            val itemName = item.name
            val description = item.description ?: "его пока нет"
            val price = item.price?.toString() ?: "--"
            val itemsCount = getItemCountInUserCart(user, item)
            val responseText = """
                $itemName
                (В категории: $categoryName)
                Описание: $description
            """.trimIndent()
            val imk = InlineKeyboardMarkup(
                listOf(
                    listOf(
                        InlineKeyboardButton(
                            "Добавить в корзину \u2013 $price ₽ \n (В корзине: $itemsCount)",
                            callbackData = Json.encodeToString(AddItemToCartCommand.new(id))
                        )
                    )
                )
            )
            bot.sendMessage(chatId, responseText, replyMarkup = imk)
        }
        val replyKeyboardMarkup = KeyboardReplyMarkup(KeyboardButton(HOME_BUTTON_TEXT), resizeKeyboard = true)
        bot.sendMessage(chatId, "", replyMarkup = replyKeyboardMarkup)
    }
}

private fun <E> List<E>.takePage(pageNumber: Int, pageSize: Int): List<E> = filterIndexed { index, _ -> 
    index in (pageNumber * pageSize) until (pageNumber * pageSize + pageSize)
}
