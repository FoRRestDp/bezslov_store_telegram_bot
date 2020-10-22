package com.github.forrestdp.tableentities

import com.github.forrestdp.tables.UsersItems
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class CartItem(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<CartItem>(UsersItems)

    var item by Item referencedOn UsersItems.itemId
    var itemCount by UsersItems.itemCount
    var user by User referencedOn UsersItems.userChatId
}

fun getItemCountInUserCart(user: User, item: Item): Int =
    CartItem.all().firstOrNull {
        it.item == item && it.user == user
    }?.itemCount ?: 0
