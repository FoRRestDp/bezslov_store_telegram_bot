package com.github.forrestdp.entities

import com.github.forrestdp.tables.Users
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class User(chatId: EntityID<Long>) : LongEntity(chatId) {
    companion object : LongEntityClass<User>(Users)

    val chatId by Users.id
    var firstName by Users.firstName
    var lastName by Users.lastName
    var phone by Users.phone
    var address by Users.address
    var action by Users.action
}
