package com.github.forrestdp.entities

import com.github.forrestdp.tables.Categories
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class Category(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Category>(Categories)

    var name by Categories.name
    var isHidden by Categories.isHidden
}

fun Category.Companion.allNotHiddenSortedById() = all().filterNot { it.isHidden }.sortedBy { it.id }
