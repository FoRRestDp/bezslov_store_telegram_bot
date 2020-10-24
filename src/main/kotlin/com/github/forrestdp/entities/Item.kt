package com.github.forrestdp.entities

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
    var imageId by Items.telegramImage
    var imageUrl by Items.imageLink
    var price by Items.price
    var isHidden by Items.isHidden
    var owningUsers by User via UsersItems
}

fun Item.Companion.allNotHidden(): List<Item> = all().filterNot { it.isHidden }
fun Item.Companion.findByIdNotHidden(id: Int) = findById(id).takeUnless { it?.isHidden ?: true }
