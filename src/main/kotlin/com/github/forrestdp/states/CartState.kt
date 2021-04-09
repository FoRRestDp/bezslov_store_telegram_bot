package com.github.forrestdp.states

import com.github.forrestdp.*
import com.github.forrestdp.entities.CartItem
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.*
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal

fun Bot.sendCartMessage(chatId: Long) {
    require(chatId > 0)
    showCartFirstTime(this, chatId)
}

fun Bot.editCartMessage(chatId: Long, messageId: Long, selectedItemIndex: Int) {
    require(selectedItemIndex >= 0)
    require(chatId > 0)
    require(messageId > 0)
    showEditedCart(this, chatId, messageId, selectedItemIndex)
}

private const val EMPTY_CART_INLINE_BUTTON_TEXT = "\uD83D\uDCD6 Купить что-нибудь"
private const val REMOVE_ALL_FROM_CART_INLINE_BUTTON_TEXT = "❌"
private const val ADD_ONE_TO_CART_INLINE_BUTTON_TEXT = "🔺"
private const val REMOVE_ONE_FROM_CART_INLINE_BUTTON_TEXT = "🔻"
private const val PREVIOUS_IN_CART_INLINE_BUTTON_TEXT = "⬅️"
private const val NEXT_IN_CART_INLINE_BUTTON_TEXT = "➡️"
private const val CATEGORIES_LIST_INLINE_BUTTON_TEXT = "\uD83D\uDCD6 Продолжить покупки"

private const val CART_PREMESSAGE_TEXT = "Переходим в корзину"

private fun checkoutInlineButtonText(cartTotalCost: String) = "✅ Заказ на $cartTotalCost ₽. Оформить?"
private fun cartMessageText(info: CartMessageInfo): String =
    """
    *Корзина:*
    ${info.itemName}
    ${info.itemCountInCart} шт[.](${info.itemImageUrl}) \* ${info.itemPrice} ₽ = ${info.itemCost} ₽
""".trimIndent()

private fun itemCountInlineButtonText(itemCountInCart: Int) = "$itemCountInCart шт."
private fun indexInlineButtonText(info: CartMessageInfo) =
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

private fun CartItem.Companion.allSorted(chatId: Long) =
    all().filter { it.user.chatId.value == chatId }.sortedBy { it.id.value }

// Меняет показываемый корзиной в данном сообщении продукт
private fun showEditedCart(bot: Bot, chatIdLong: Long, messageId: Long, selectedItemIndex: Int) {
    val cartItem = transaction {
        CartItem.allSorted(chatIdLong)
            .getOrNull(selectedItemIndex)
    }

    val info = getCartMessageInfo(chatIdLong, cartItem)
    val ikm = getInlineKeyboardMarkup(info)
    val text = getText(info)

    bot.editMessageText(
        ChatId.fromId(chatIdLong),
        messageId,
        text = text,
        replyMarkup = ikm,
        parseMode = ParseMode.MARKDOWN,
    )
}

// Присылает сообщение, в котором содержится информация о первом товаре из корзины
private fun showCartFirstTime(bot: Bot, chatIdLong: Long) {
    val chatId = ChatId.fromId(chatIdLong)
    val cartItem = transaction {
        CartItem.allSorted(chatIdLong).firstOrNull()
    }

    val info = getCartMessageInfo(chatIdLong, cartItem)
    val text = getText(info)
    val ikm = getInlineKeyboardMarkup(info)
    val krm = getKeyboardReplyMarkup()

    with(bot) {
        sendMessage(
            chatId,
            CART_PREMESSAGE_TEXT,
            replyMarkup = krm,
        )
        sendMessage(
            chatId,
            text = text,
            replyMarkup = ikm,
            parseMode = ParseMode.MARKDOWN,
        )
    }
}

private fun getText(info: CartMessageInfo?): String = if (info == null) {
    CART_IS_EMPTY
} else {
    cartMessageText(info)
}

private fun getInlineKeyboardMarkup(info: CartMessageInfo?): InlineKeyboardMarkup {
    if (info == null) {
        return InlineKeyboardMarkup.createSingleButton(
            InlineKeyboardButton.CallbackData(
                EMPTY_CART_INLINE_BUTTON_TEXT,
                callbackData = ShowCategoriesCallbackData.of().toJsonString(),
            )
        )
    }

    val removeAllButton = InlineKeyboardButton.CallbackData(
        REMOVE_ALL_FROM_CART_INLINE_BUTTON_TEXT,
        callbackData = DeleteItemFromCartCallbackData.of(info.itemId, 0).toJsonString()
    )
    val addOneButton = InlineKeyboardButton.CallbackData(
        ADD_ONE_TO_CART_INLINE_BUTTON_TEXT,
        callbackData = SetItemCountInCartCallbackData.of(
            info.itemId,
            info.itemIndexInCart,
            info.itemCountInCartPlusOne
        ).toJsonString()
    )
    val itemCountButton = InlineKeyboardButton.CallbackData(
        itemCountInlineButtonText(info.itemCountInCart),
        callbackData = NoActionCallbackData.of().toJsonString()
    )
    val removeOneButton = InlineKeyboardButton.CallbackData(
        REMOVE_ONE_FROM_CART_INLINE_BUTTON_TEXT,
        callbackData = if (info.itemCountInCartMinusOne == 0) {
            DeleteItemFromCartCallbackData.of(info.itemId, 0).toJsonString()
        } else {
            SetItemCountInCartCallbackData.of(
                info.itemId,
                info.itemIndexInCart,
                info.itemCountInCartMinusOne
            ).toJsonString()
        }
    )
    val previousButton = InlineKeyboardButton.CallbackData(
        PREVIOUS_IN_CART_INLINE_BUTTON_TEXT,
        callbackData = SetItemIndexInCartCallbackData.of(info.itemPreviousIndex).toJsonString(),
    )
    val indexButton = InlineKeyboardButton.CallbackData(
        indexInlineButtonText(info),
        callbackData = NoActionCallbackData.of().toJsonString(),
    )
    val nextButton = InlineKeyboardButton.CallbackData(
        NEXT_IN_CART_INLINE_BUTTON_TEXT,
        callbackData = SetItemIndexInCartCallbackData.of(info.itemNextIndex).toJsonString()
    )
    val checkoutButton = InlineKeyboardButton.CallbackData(
        checkoutInlineButtonText(info.cartTotalCost),
        callbackData = CheckoutCallbackData.of().toJsonString()
    )
    val categoriesListButton = InlineKeyboardButton.CallbackData(
        CATEGORIES_LIST_INLINE_BUTTON_TEXT,
        callbackData = ShowCategoriesCallbackData.of().toJsonString()
    )

    return InlineKeyboardMarkup.create(
        listOf(
            listOf(removeAllButton, addOneButton, itemCountButton, removeOneButton),
            listOf(previousButton, indexButton, nextButton),
            listOf(checkoutButton),
            listOf(categoriesListButton),
        )
    )
}

private fun getCartMessageInfo(chatId: Long, cartItem: CartItem?): CartMessageInfo? = transaction {
    require(chatId >= 0)

    if (cartItem == null) return@transaction null
    val itemIndexInCart = CartItem.allSorted(chatId).map { it.id.value }.indexOf(cartItem.id.value)
    val cartTotalItemCount = CartItem.all().filter { it.user.chatId.value == chatId }.size
    val itemPrice = cartItem.item.price
    val itemCount = cartItem.itemCount
    return@transaction CartMessageInfo(
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
            .allSorted(chatId)
            .takeUnless { list -> list.map { it.item.price }.contains(null) }
            ?.sumOf { it.item.price?.multiply(BigDecimal(it.itemCount)) ?: BigDecimal.ZERO }
            ?.toString() ?: "--",
    )
}

private fun getKeyboardReplyMarkup() = KeyboardReplyMarkup.createSimpleKeyboard(
    listOf(listOf(
        HOME_BUTTON_TEXT,
        CART_BUTTON_TEXT,
        CATEGORIES_LIST_BUTTON_TEXT,
    )),
    resizeKeyboard = true,
)