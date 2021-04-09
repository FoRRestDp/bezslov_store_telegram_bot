package com.github.forrestdp.entities

import com.github.forrestdp.tables.UsersItems
import com.github.kotlintelegrambot.entities.ChatId
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.transactions.transaction

class CartItem(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<CartItem>(UsersItems)

    var item by Item referencedOn UsersItems.itemId
    var itemCount by UsersItems.itemCount
    var user by User referencedOn UsersItems.userChatId
}

fun CartItem.Companion.allNotHidden() = all().filterNot { it.item.isHidden }

fun deleteItemFromCart(chatId: Long, itemId: Int) = transaction {
    require(itemId >= 0)
    CartItem.all()
        .firstOrNull { it.item.id.value == itemId && it.user.chatId.value == chatId }
        ?.delete()
        ?: error(CART_ITEM_NOT_FOUND_RESPONSE)
}

fun setItemCountInCart(chatId: Long, itemId: Int, itemCount: Int) {
    check(itemCount >= 0)
    if (itemCount == 0) {
        deleteItemFromCart(chatId, itemId)
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

fun setItemCountInList(chatId: Long, itemId: Int, itemCount: Int): Unit = transaction {
    require(chatId > 0)
    require(itemId >= 0)
    require(itemCount >= 0)
    val user = User.findById(chatId) ?: error(USER_NOT_FOUND_RESPONSE)
    val item = Item.findByIdNotHidden(itemId) ?: error(ITEM_NOT_FOUND_OR_HIDDEN_RESPONSE)
    val cartItem = CartItem
        .allNotHidden()
        .firstOrNull {
            it.item == item && it.user == user
        }
    if (cartItem == null) {
        CartItem.new {
            this.item = item
            this.user = user
            this.itemCount = itemCount
        }
    } else {
        cartItem.itemCount = itemCount
    }
}

private const val CART_ITEM_NOT_FOUND_RESPONSE = "Item in cart with such itemId and chatId was not found"
private const val USER_NOT_FOUND_RESPONSE = "User with such chat id was not found"
private const val ITEM_NOT_FOUND_OR_HIDDEN_RESPONSE = "Item with such id not found or hidden"
