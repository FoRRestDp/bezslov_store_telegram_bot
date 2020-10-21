package com.github.forrestdp.tableentities

import com.github.forrestdp.tables.Items
import com.github.forrestdp.tables.UsersItems
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class Item(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Item>(Items)
    var category by Category referencedOn Items.category
    var name by Items.name
    var description by Items.description
    var imageTelegram by Items.telegramImage
    var img by Items.imageLink
    var price by Items.price
    var isHidden by Items.isHidden
    var owningUsers by User via UsersItems
}