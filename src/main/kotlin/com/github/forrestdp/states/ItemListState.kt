package com.github.forrestdp.states

import com.github.forrestdp.*
import com.github.forrestdp.entities.*
import com.github.forrestdp.entities.User
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.*
import org.jetbrains.exposed.sql.transactions.transaction

fun Bot.goToItemsListStateWithNewMessage(chatId: Long, categoryId: Int, pageNumber: Int): Unit =
    showItemList(this, categoryId, chatId, pageNumber)

fun Bot.goToItemsListStateWithEditingMessage(update: Update, itemId: Int, itemCount: Int) {
    val chatId = update.chatId
    val messageId = update.messageId
    val price = transaction {
        Item.findByIdNotHidden(itemId)?.price?.toString() ?: "--"
    }
    editMessageReplyMarkup(
        chatId,
        messageId,
        replyMarkup = InlineKeyboardMarkup(listOf(
            listOf(InlineKeyboardButton(
                addToCartText(price, itemCount),
                callbackData = SetItemCountInListCallbackData.new(itemId, itemCount + 1).toJsonString()
            )),
            listOf(InlineKeyboardButton(
                GO_TO_CART_INLINE_BUTTON_TEXT,
                callbackData = ShowCartCallbackData.new().toJsonString()
            )),
        ))
    )
}

private const val ITEM_LIST_PAGE_SIZE = 5

private const val GO_TO_CART_INLINE_BUTTON_TEXT = "Перейти в корзину"
private const val GO_BACK_INLINE_BUTTON_TEXT = "↩️ Назад"
private const val SHOW_MORE_INLINE_BUTTON_TEXT = "⬇️ Показать ещё"

private const val USER_NOT_FOUND_RESPONSE = "User with such chat id was not found"

private fun oneItemMessageText(
    itemName: String,
    imageUrl: String?,
    categoryName: String,
    description: String,
): String = """
            *$itemName*[.]($imageUrl)
            _(В категории: $categoryName)_
            *Описание:*
        """.trimIndent() + "\n$description"

private fun addToCartText(price: String, itemCount: Int) =
    "Купить \u2013 $price ₽ ($itemCount)"

private fun shownItemsMessageText(
    shownItemsCount: Int,
    noun: String,
    itemsCountInCategory: Int,
) = "\uD83D\uDD04 Показано $shownItemsCount $noun из $itemsCountInCategory"

private fun showItemList(
    bot: Bot,
    categoryId: Int,
    chatId: Long,
    pageNumber: Int,
): Unit = transaction {
    val categoryName = Category.findById(categoryId)?.name ?: "неизвестно какой"

    bot.sendMessageWithCategoryNameAndReplyKeyboard(chatId, categoryName)

    val user = User.findById(chatId) ?: error(USER_NOT_FOUND_RESPONSE)
    val items = Item.allNotHidden().filter { item ->
        item.category.id.value == categoryId
    }.takePage(pageNumber, ITEM_LIST_PAGE_SIZE)

    for (item in items) {
        bot.sendMessageWithOneItem(chatId, item, user, categoryName)
    }

    bot.sendMessageWithShownItemsCount(chatId, items, categoryId, pageNumber)
}

private fun Bot.sendMessageWithShownItemsCount(
    chatId: Long,
    items: List<Item>,
    categoryId: Int,
    pageNumber: Int,
) {
    val itemsCountInCategory = Item.allNotHidden().filter { it.category.id.value == categoryId }.size
    val shownItemsCount = pageNumber * ITEM_LIST_PAGE_SIZE + items.size
    val ikm = getImk(shownItemsCount, itemsCountInCategory, categoryId, pageNumber)
    val noun = chooseNounEnding(shownItemsCount)

    sendMessage(
        chatId,
        shownItemsMessageText(shownItemsCount, noun, itemsCountInCategory),
        replyMarkup = ikm,
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
            GO_BACK_INLINE_BUTTON_TEXT,
            callbackData = ShowCategoriesCallbackData.new().toJsonString(),
        ),
        InlineKeyboardButton(
            SHOW_MORE_INLINE_BUTTON_TEXT,
            callbackData = ShowItemsCallbackData.new(categoryId, pageNumber + 1).toJsonString(),
        ),
    )))
} else {
    InlineKeyboardMarkup.createSingleButton(
        InlineKeyboardButton(
            GO_BACK_INLINE_BUTTON_TEXT,
            callbackData = ShowCategoriesCallbackData.new().toJsonString(),
        ),
    )
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

private fun Bot.sendMessageWithOneItem(chatId: Long, item: Item, user: User, categoryName: String) {
    val id = item.id.value
    val itemName = item.name
    val description = item.description ?: "его пока нет"
    val price = item.price?.toString() ?: "--"
    val itemCount = CartItem
        .allNotHidden()
        .firstOrNull {
            it.item == item && it.user == user
        }?.itemCount ?: 0
    val imageUrl = item.imageUrl
    val responseText = oneItemMessageText(itemName, imageUrl, categoryName, description)
    val ikm = InlineKeyboardMarkup.createSingleButton(
        InlineKeyboardButton(
            addToCartText(price, itemCount),
            callbackData = SetItemCountInListCallbackData.new(id, itemCount + 1).toJsonString(),
        )
    )
    sendMessage(chatId, responseText, replyMarkup = ikm, parseMode = ParseMode.MARKDOWN)
}

private fun Bot.sendMessageWithCategoryNameAndReplyKeyboard(chatId: Long, categoryName: String) {
    val krm = KeyboardReplyMarkup.createSimpleKeyboard(
        listOf(listOf(
            HOME_BUTTON_TEXT,
            CART_BUTTON_TEXT,
            CATEGORIES_LIST_BUTTON_TEXT,
        )),
        resizeKeyboard = true,
    )
    sendMessage(chatId, categoryName, replyMarkup = krm)
}

private fun <E> List<E>.takePage(pageNumber: Int, pageSize: Int): List<E> = filterIndexed { index, _ ->
    index in (pageNumber * pageSize) until (pageNumber * pageSize + pageSize)
}
