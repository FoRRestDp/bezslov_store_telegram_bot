package com.github.forrestdp.tableentities

import com.github.forrestdp.tables.SelectedItems
import com.github.forrestdp.tables.Users
import com.github.forrestdp.tables.UsersItems
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class SelectedItem(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<SelectedItem>(SelectedItems)
    var user by User referencedOn Users.chatId
    var cartItem by CartItem referencedOn UsersItems.id
}