package com.github.forrestdp.tables

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column

//object SelectedItems : IdTable<Long>("selected_items") {
//    val userChatId = reference("user_chat_id", Users)
//    override val primaryKey = PrimaryKey(userChatId)
//    val usersItemsId = reference("users_items_id", UsersItems)
//    override val id: Column<EntityID<Long>> = userChatId
//}