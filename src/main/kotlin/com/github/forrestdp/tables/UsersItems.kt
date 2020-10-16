package com.github.forrestdp.tables

import org.jetbrains.exposed.dao.id.IntIdTable

object UsersItems : IntIdTable("users_items") {
    override val primaryKey = PrimaryKey(id)
    val itemId = reference("item_id", Items)
    val itemCount = integer("item_count").default(1)
    val userChatId = reference("user_chat_id", Users)
}