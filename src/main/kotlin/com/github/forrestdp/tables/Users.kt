package com.github.forrestdp.tables

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object Users : IdTable<Long>() {
    val chatId: Column<Long> = long("chat_id")
    override val primaryKey = PrimaryKey(chatId)
    override val id = chatId.entityId()
    val firstName = varchar("first_name", 32).nullable()
    val lastName = varchar("last_name", 32).nullable()
    val phone = varchar("phone", 15).nullable()
    val address = varchar("address", 255).nullable()
    val action = varchar("action", 20).nullable()
}