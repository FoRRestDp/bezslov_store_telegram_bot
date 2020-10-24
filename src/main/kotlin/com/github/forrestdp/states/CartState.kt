package com.github.forrestdp.states

import com.github.forrestdp.*
import com.github.forrestdp.entities.CartItem
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal

fun Bot.goToCartStateWithNewMessage(update: Update) = showCartFirstTime(this, update)

fun Bot.goToCartStateWithEditingMessage(update: Update, selectedItemIndex: Int) =
    showEditedCart(this, update, selectedItemIndex)

private const val MESSAGE_NOT_FOUND_RESPONSE = "Message is not defines"

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
private fun showEditedCart(bot: Bot, update: Update, selectedItemIndex: Int) {
    val chatId = update.chatId
    val cartItem = transaction {
        CartItem.allSorted(chatId)
            .getOrNull(selectedItemIndex)
    }

    val info = getCartMessageInfo(chatId, cartItem)
    val ikm = getInlineKeyboardMarkup(info)
    val text = getText(info)

    val messageId = update.message?.messageId
        ?: update.callbackQuery?.message?.messageId
        ?: error(MESSAGE_NOT_FOUND_RESPONSE)

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
        CartItem.allSorted(chatId).firstOrNull()
    }

    val info = getCartMessageInfo(chatId, cartItem)
    val text = getText(info)
    val ikm = getInlineKeyboardMarkup(info)
    val krm = getKeyboardReplyMarkup()
    
    with(bot) {
        val (response, _) = sendMessage(
            chatId,
            CART_PREMESSAGE_TEXT,
            replyMarkup = krm,
        )
        deleteMessage(chatId, response?.body()?.result?.messageId)
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
            InlineKeyboardButton(
                EMPTY_CART_INLINE_BUTTON_TEXT,
                callbackData = ShowCategoriesCallbackData.new().toJsonString(),
            )
        )
    }

    val removeAllButton = InlineKeyboardButton(
        REMOVE_ALL_FROM_CART_INLINE_BUTTON_TEXT,
        callbackData = DeleteItemFromCartCallbackData.new(info.itemId, 0).toJsonString()
    )
    val addOneButton = InlineKeyboardButton(
        ADD_ONE_TO_CART_INLINE_BUTTON_TEXT,
        callbackData = SetItemCountInCartCallbackData.new(
            info.itemId,
            info.itemIndexInCart,
            info.itemCountInCartPlusOne
        ).toJsonString()
    )
    val itemCountButton = InlineKeyboardButton(
        itemCountInlineButtonText(info.itemCountInCart),
        callbackData = NoActionCallbackData.new().toJsonString()
    )
    val removeOneButton = InlineKeyboardButton(
        REMOVE_ONE_FROM_CART_INLINE_BUTTON_TEXT,
        callbackData = if (info.itemCountInCartMinusOne == 0) {
            DeleteItemFromCartCallbackData.new(info.itemId, 0).toJsonString()
        } else {
            SetItemCountInCartCallbackData.new(
                info.itemId,
                info.itemIndexInCart,
                info.itemCountInCartMinusOne
            ).toJsonString()
        }
    )
    val previousButton = InlineKeyboardButton(
        PREVIOUS_IN_CART_INLINE_BUTTON_TEXT,
        callbackData = SetItemIndexInCartCallbackData.new(info.itemPreviousIndex).toJsonString(),
    )
    val indexButton = InlineKeyboardButton(
        indexInlineButtonText(info),
        callbackData = NoActionCallbackData.new().toJsonString(),
    )
    val nextButton = InlineKeyboardButton(
        NEXT_IN_CART_INLINE_BUTTON_TEXT,
        callbackData = SetItemIndexInCartCallbackData.new(info.itemNextIndex).toJsonString()
    )
    val checkoutButton = InlineKeyboardButton(
        checkoutInlineButtonText(info.cartTotalCost),
        callbackData = CheckoutCallbackData.new().toJsonString()
    )
    val categoriesListButton = InlineKeyboardButton(
        CATEGORIES_LIST_INLINE_BUTTON_TEXT,
        callbackData = ShowCategoriesCallbackData.new().toJsonString()
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

private fun getKeyboardReplyMarkup() = KeyboardReplyMarkup(
    listOf(listOf(
        KeyboardButton(HOME_BUTTON_TEXT),
        KeyboardButton(CART_BUTTON_TEXT),
        KeyboardButton(CATEGORIES_LIST_BUTTON_TEXT)
    )),
    resizeKeyboard = true,
)