package com.github.forrestdp.states

import com.github.forrestdp.*
import com.github.forrestdp.tableentities.*
import com.github.forrestdp.tableentities.User
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.*
import com.github.kotlintelegrambot.network.Response
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.transactions.transaction

private const val ITEM_LIST_PAGE_SIZE = 5

fun goToItemsList(
    bot: Bot,
    @Suppress("UNUSED_PARAMETER") update: Update,
    chatId: Long,
    categoryId: Int,
    pageNumber: Int,
) = transaction {
    val categoryName = Category.findById(categoryId)?.name ?: "неизвестно какой"

    sendMessageWithCategoryNameAndReplyKeyboard(bot, chatId, categoryName)

    val user = User.findById(chatId) ?: error("User with this chat id not found")
    val items = Item.allNotHidden().filter { item ->
        item.category.id.value == categoryId
    }.takePage(pageNumber, ITEM_LIST_PAGE_SIZE)

    for (item in items) {
        sendMessageWithOneItem(bot, chatId, item, user, categoryName)
    }

    sendMessageWithShownItemsCount(bot, chatId, items, categoryId, pageNumber)
}

private fun sendMessageWithShownItemsCount(
    bot: Bot,
    chatId: Long,
    items: List<Item>,
    categoryId: Int,
    pageNumber: Int,
): Pair<retrofit2.Response<Response<Message>?>?, Exception?> {
    val itemsCountInCategory = Item.allNotHidden().filter { it.category.id.value == categoryId }.size
    val shownItemsCount = pageNumber * ITEM_LIST_PAGE_SIZE + items.size
    val ikm = getImk(shownItemsCount, itemsCountInCategory, categoryId, pageNumber)
    val noun = chooseNounEnding(shownItemsCount)

    return bot.sendMessage(
        chatId,
        "\uD83D\uDD04 Показано $shownItemsCount $noun из $itemsCountInCategory",
        replyMarkup = ikm
    )
}

private fun getImk(
    shownItemsCount: Int,
    itemsCountInCategory: Int,
    categoryId: Int,
    pageNumber: Int,
): InlineKeyboardMarkup = if (shownItemsCount < itemsCountInCategory) {
    InlineKeyboardMarkup(listOf(listOf(
        InlineKeyboardButton(
            "↩️ Назад",
            callbackData = Json.encodeToString(ShowCategoriesCommand.new()),
        ),
        InlineKeyboardButton(
            "⬇️ Показать ещё",
            callbackData = Json.encodeToString(ShowItemsCommand.new(categoryId, pageNumber + 1)),
        ),
    )))
} else {
    InlineKeyboardMarkup(listOf(listOf(
        InlineKeyboardButton(
            "↩️ Назад",
            callbackData = Json.encodeToString(ShowCategoriesCommand.new())
        ),
    )))
}

private fun chooseNounEnding(shownItemsCount: Int): String {
    return if (shownItemsCount % 100 in 11..19) {
        "товаров"
    } else {
        when (shownItemsCount % 10) {
            1 -> "товар"
            2, 3, 4 -> "товара"
            else -> "товаров"
        }
    }
}

private fun sendMessageWithOneItem(bot: Bot, chatId: Long, item: Item, user: User, categoryName: String) {
    val id = item.id.value
    val itemName = item.name
    val description = item.description ?: "его пока нет"
    val price = item.price?.toString() ?: "--"
    val itemsCount = getItemCountInUserCart(user, item)
    val imageUrl = item.imageUrl
    val responseText = """
                *$itemName*[.]($imageUrl)
                _(В категории: $categoryName)_
                *Описание:*
            """.trimIndent() + "\n$description"
    val imk = InlineKeyboardMarkup(
        listOf(
            listOf(
                InlineKeyboardButton(
                    getAddToCartText(price, itemsCount),
                    callbackData = Json.encodeToString(AddItemToCartCommand.new(id))
                )
            )
        )
    )
    bot.sendMessage(chatId, responseText, replyMarkup = imk, parseMode = ParseMode.MARKDOWN)
}

private fun sendMessageWithCategoryNameAndReplyKeyboard(bot: Bot, chatId: Long, categoryName: String) {
    val replyKeyboardMarkup = KeyboardReplyMarkup(
        listOf(listOf(
            KeyboardButton(HOME_BUTTON_TEXT),
            KeyboardButton(CART_BUTTON_TEXT),
            KeyboardButton(CATEGORIES_LIST_BUTTON_TEXT)
        )),
        resizeKeyboard = true,
    )
    bot.sendMessage(chatId, categoryName, replyMarkup = replyKeyboardMarkup)
}

private fun <E> List<E>.takePage(pageNumber: Int, pageSize: Int): List<E> = filterIndexed { index, _ ->
    index in (pageNumber * pageSize) until (pageNumber * pageSize + pageSize)
}
