package com.github.forrestdp.tables

import org.jetbrains.exposed.sql.Table

object SelectedItems : Table() {
    val userChatId = reference("user_chat_id", Users.chatId)
    override val primaryKey = PrimaryKey(userChatId)
    val usersItemsId = reference("users_items_id", UsersItems.id)
}