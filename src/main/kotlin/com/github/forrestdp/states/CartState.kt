package com.github.forrestdp.states

import com.github.forrestdp.*
import com.github.forrestdp.tableentities.CartItem
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.InlineKeyboardButton
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.Update
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal

private const val CART_ITEM_NOT_FOUND_RESPONSE = "Item in cart with such itemId and chatId was not found"
private const val MESSAGE_NOT_FOUND_RESPONSE = "Message is not defines"

private const val EMPTY_CART_INLINE_BUTTON_TEXT = "\uD83D\uDCD6 Купить что-нибудь"
private const val REMOVE_ALL_FROM_CART_INLINE_BUTTON_TEXT = "❌"
private const val ADD_ONE_TO_CART_INLINE_BUTTON_TEXT = "🔺"
private const val REMOVE_ONE_FROM_CART_INLINE_BUTTON_TEXT = "🔻"
private const val PREVIOUS_IN_CART_INLINE_BUTTON_TEXT = "⬅️"
private const val NEXT_IN_CART_INLINE_BUTTON_TEXT = "➡️"
private const val CATEGORIES_LIST_INLINE_BUTTON_TEXT = "\uD83D\uDCD6 Продолжить покупки"

private fun getCheckoutInlineButtonText(cartTotalCost: String) = "✅ Заказ на $cartTotalCost ₽. Оформить?"
private fun getCartMessageText(info: CartMessageInfo): String =
    """
    *Корзина:*
    ${info.itemName}
    ${info.itemCountInCart} шт[.](${info.itemImageUrl}) \* ${info.itemPrice} ₽ = ${info.itemCost} ₽
""".trimIndent()

private fun getItemCountInlineButtonText(itemCountInCart: Int) = "$itemCountInCart шт."
private fun getIndexInlineButtonText(info: CartMessageInfo) =
    "${info.itemNormalizedIndexInCart}/${info.cartTotalItemCount}"

private class CartMessageInfo(
    val itemId: Int,
    val itemIndexInCart: Int,
    val itemNormalizedIndexInCart: Int,
    val itemPreviousIndex: Int,
    val itemNextIndex: Int,
    val itemName: String,
    val itemCountInCart: Int,
    val itemCountInCartPlusOne: Int,
    val itemCountInCartMinusOne: Int,
    val itemImageUrl: String,
    val itemPrice: String,
    val itemCost: String,
    val cartTotalItemCount: Int,
    val cartTotalCost: String,
)

fun goToCartWithNewMessage(bot: Bot, update: Update) {
    showCartFirstTime(bot, update)
}

fun goToCartWithEditingMessage(bot: Bot, update: Update, selectedItemIndex: Int) {
    showEditedCart(bot, update, selectedItemIndex)
}

fun deleteItemFromCart(itemId: Int, chatId: Long) {
    transaction {
        CartItem.all()
            .firstOrNull { it.item.id.value == itemId && it.user.chatId.value == chatId }
            ?.delete()
            ?: error(CART_ITEM_NOT_FOUND_RESPONSE)
    }
}

fun setItemCountInCart(itemId: Int, chatId: Long, itemCount: Int) {
    assert(itemCount >= 0)
    if (itemCount == 0) {
        deleteItemFromCart(itemId, chatId)
        return
    }
    transaction {
        val cartItem = CartItem
            .all()
            .firstOrNull { it.item.id.value == itemId && it.user.chatId.value == chatId }
            ?: error(CART_ITEM_NOT_FOUND_RESPONSE)
        cartItem.itemCount = itemCount
    }
}

private fun CartItem.Companion.allSortedWithChatId(chatId: Long) =
    all().filter { it.user.chatId.value == chatId }.sortedBy { it.id.value }

// Меняет показываемый корзиной в данном сообщении продукт
private fun showEditedCart(bot: Bot, update: Update, selectedItemIndex: Int) {
    val chatId = update.chatId
    val messageId = update.message?.messageId
        ?: update.callbackQuery?.message?.messageId
        ?: error(MESSAGE_NOT_FOUND_RESPONSE)
    val cartItem = transaction {
        CartItem.allSortedWithChatId(chatId)
            .getOrNull(selectedItemIndex)
    }

    val info = getCartMessageInfo(chatId, cartItem)

    val ikm = getIkm(info)
    val text = getText(info)

    bot.editMessageText(
        chatId,
        messageId,
        text = text,
        replyMarkup = ikm,
        parseMode = ParseMode.MARKDOWN,
    )
}

// Присылает сообщение, в котором содержится информация о первом товаре из корзины
private fun showCartFirstTime(bot: Bot, update: Update) {
    val chatId = update.chatId
    val cartItem = transaction {
        CartItem.allSortedWithChatId(chatId).firstOrNull()
    }

    val info = getCartMessageInfo(chatId, cartItem)
    val ikm = getIkm(info)
    val text = getText(info)

    bot.sendMessage(
        chatId,
        text = text,
        replyMarkup = ikm,
        parseMode = ParseMode.MARKDOWN,
    )
}

private fun getText(info: CartMessageInfo?): String = if (info == null) {
    CART_IS_EMPTY
} else {
    getCartMessageText(info)
}

private fun getIkm(info: CartMessageInfo?): InlineKeyboardMarkup {
    if (info == null) {
        return InlineKeyboardMarkup(listOf(listOf(
            InlineKeyboardButton(
                EMPTY_CART_INLINE_BUTTON_TEXT,
                callbackData = Json.encodeToString(ShowCategoriesCommand.new()),
            )
        )))
    }

    val removeAllButton = InlineKeyboardButton(
        REMOVE_ALL_FROM_CART_INLINE_BUTTON_TEXT,
        // TODO изменить itemIndex на нормальное значение
        callbackData = Json.encodeToString(DeleteItemFromCartCommand.new(info.itemId, 0))
    )
    val addOneButton = InlineKeyboardButton(
        ADD_ONE_TO_CART_INLINE_BUTTON_TEXT,
        callbackData = Json.encodeToString(SetItemCountInCartCommand.new(info.itemId,
            info.itemIndexInCart,
            info.itemCountInCartPlusOne))
    )
    val itemCountButton = InlineKeyboardButton(
        getItemCountInlineButtonText(info.itemCountInCart),
        callbackData = NO_ACTION_CALLBACK
    )
    val removeOneButton = InlineKeyboardButton(
        REMOVE_ONE_FROM_CART_INLINE_BUTTON_TEXT,
        callbackData = if (info.itemCountInCartMinusOne == 0) {
            // TODO изменить itemIndex на нормальное значение
            Json.encodeToString(DeleteItemFromCartCommand.new(info.itemId, 0))
        } else {
            Json.encodeToString(SetItemCountInCartCommand.new(info.itemId,
                info.itemIndexInCart,
                info.itemCountInCartMinusOne))
        }
    )
    val previousButton = InlineKeyboardButton(
        PREVIOUS_IN_CART_INLINE_BUTTON_TEXT,
        callbackData = Json.encodeToString(SelectAnotherItemFromCartCommand.new(info.itemPreviousIndex)),
    )
    val indexButton = InlineKeyboardButton(
        getIndexInlineButtonText(info),
        callbackData = NO_ACTION_CALLBACK,
    )
    val nextButton = InlineKeyboardButton(
        NEXT_IN_CART_INLINE_BUTTON_TEXT,
        callbackData = Json.encodeToString(SelectAnotherItemFromCartCommand.new(info.itemNextIndex))
    )
    val checkoutButton = InlineKeyboardButton(
        getCheckoutInlineButtonText(info.cartTotalCost),
        callbackData = CHECKOUT_CALLBACK
    )
    val categoriesListButton = InlineKeyboardButton(
        CATEGORIES_LIST_INLINE_BUTTON_TEXT,
        callbackData = Json.encodeToString(ShowCategoriesCommand.new())
    )

    return InlineKeyboardMarkup(
        listOf(
            listOf(removeAllButton, addOneButton, itemCountButton, removeOneButton),
            listOf(previousButton, indexButton, nextButton),
            listOf(checkoutButton),
            listOf(categoriesListButton),
        )
    )
}

private fun getCartMessageInfo(chatId: Long, cartItem: CartItem?): CartMessageInfo? = transaction {
    if (cartItem == null) return@transaction null
    val itemIndexInCart = CartItem.allSortedWithChatId(chatId).map { it.id.value }.indexOf(cartItem.id.value)
    val cartTotalItemCount = CartItem.all().filter { it.user.chatId.value == chatId }.size
    val itemPrice = cartItem.item.price
    val itemCount = cartItem.itemCount
    CartMessageInfo(
        itemId = cartItem.item.id.value,
        itemIndexInCart = itemIndexInCart,
        itemNormalizedIndexInCart = itemIndexInCart + 1,
        itemPreviousIndex = if (itemIndexInCart == 0) cartTotalItemCount - 1 else itemIndexInCart - 1,
        itemNextIndex = if (itemIndexInCart + 1 == cartTotalItemCount) 0 else itemIndexInCart + 1,
        itemName = cartItem.item.name,
        itemCountInCart = itemCount,
        itemCountInCartPlusOne = itemCount + 1,
        itemCountInCartMinusOne = itemCount - 1,
        itemImageUrl = cartItem.item.imageUrl ?: "null",
        itemPrice = itemPrice?.toString() ?: "--",
        itemCost = itemPrice?.multiply(BigDecimal(itemCount))?.toString() ?: "--",
        cartTotalItemCount = cartTotalItemCount,
        cartTotalCost = CartItem
            .allSortedWithChatId(chatId)
            .takeUnless { list -> list.map { it.item.price }.contains(null) }
            ?.sumOf { it.item.price?.multiply(BigDecimal(it.itemCount)) ?: BigDecimal.ZERO }
            ?.toString() ?: "--",
    )
}

/*private fun getIkmAndText(cartItem: CartItem?, chatId: Long): Pair<InlineKeyboardMarkup?, String> {
    if (cartItem == null) {
        val catalogButton = InlineKeyboardButton(
            EMPTY_CART_INLINE_BUTTON_TEXT,
            callbackData = Json.encodeToString(ShowCategoriesCommand.new())
        )
        val ikm = InlineKeyboardMarkup(listOf(listOf(catalogButton)))
        return ikm to CART_IS_EMPTY
    }

    val info = getCartMessageInfo(chatId, cartItem)

    val itemId = transaction { cartItem.item.id.value }
    val name = transaction { cartItem.item.name }
    val price = transaction { cartItem.item.price }
    val itemCount = transaction { cartItem.itemCount }
    val cost = price?.multiply(BigDecimal(itemCount))?.toString() ?: "--"
    val allUserItemsCount = transaction { CartItem.all().filter { it.user.chatId.value == chatId }.size }
    val indexOfSelectedItem = transaction {
        CartItem.allSortedWithChatId(chatId).map { it.id.value }.indexOf(cartItem.id.value)
    }
    val normalizedIndexOfSelectedFile = indexOfSelectedItem + 1
    val total = transaction {
        CartItem
            .allSortedWithChatId(chatId)
            .sumOf { it.item.price?.multiply(BigDecimal(it.itemCount)) ?: BigDecimal.ZERO }
    }
    val imageUrl = transaction { cartItem.item.imageUrl }

    val removeAllButton = InlineKeyboardButton(
        REMOVE_ALL_FROM_CART_INLINE_BUTTON_TEXT,
        // TODO изменить itemIndex на нормальное значение
        callbackData = Json.encodeToString(DeleteItemFromCartCommand.new(info.itemId, 0))
    )
    val addOneButton = InlineKeyboardButton(
        ADD_ONE_TO_CART_INLINE_BUTTON_TEXT,
        callbackData = Json.encodeToString(SetItemCountInCartCommand.new(info.itemId, info.itemIndexInCart, info.itemCountInCartPlusOne))
    )
    val itemCountButton = InlineKeyboardButton(
        "${info.itemCountInCart} шт.",
        callbackData = NO_ACTION_CALLBACK
    )
    val removeOneButton = InlineKeyboardButton(
        REMOVE_ONE_FROM_CART_INLINE_BUTTON_TEXT,
        callbackData = if (info.itemCountInCartMinusOne == 0) {
            // TODO изменить itemIndex на нормальное значение
            Json.encodeToString(DeleteItemFromCartCommand.new(info.itemId, 0))
        } else {
            Json.encodeToString(SetItemCountInCartCommand.new(info.itemId, info.itemIndexInCart, info.itemCountInCartMinusOne))
        }
    )
    val previousButton = InlineKeyboardButton(
        PREVIOUS_IN_CART_INLINE_BUTTON_TEXT,
        callbackData = Json.encodeToString(SelectAnotherItemFromCartCommand.new(info.itemPreviousIndex)),
    )
    val indexButton = InlineKeyboardButton(
        "${info.itemNormalizedIndexInCart}/${info.cartTotalItemCount}",
        callbackData = NO_ACTION_CALLBACK,
    )
    val nextButton = InlineKeyboardButton(
        "➡️",
        callbackData = Json.encodeToString(SelectAnotherItemFromCartCommand.new(info.itemNextIndex))
    )
    val checkoutButton = InlineKeyboardButton(
        "✅ Заказ на ${info.cartTotalCost} ₽. Оформить?",
        callbackData = CHECKOUT_CALLBACK
    )
    val catalogButton = InlineKeyboardButton(
        "\uD83D\uDCD6 Продолжить покупки",
        callbackData = Json.encodeToString(ShowCategoriesCommand.new())
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
                *Корзина:*
                ${info.itemName}
                ${info.itemCountInCart} шт[.](${info.itemImageUrl}) \* ${info.itemPrice} ₽ = ${info.cartTotalCost} ₽
            """.trimIndent()
}*/
