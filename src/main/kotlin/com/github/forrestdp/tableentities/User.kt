package com.github.forrestdp.tableentities

import com.github.forrestdp.tables.Users
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class User(chatId: EntityID<Long>) : Entity<Long>(chatId) {
    companion object : EntityClass<Long, User>(Users)
    var chatId by Users.chatId
    var firstName by Users.firstName
    var lastName by Users.lastName
    var phone by Users.phone
    var address by Users.address
    var action by Users.action
}