package com.github.forrestdp.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import java.math.BigDecimal

object Items : IntIdTable() {
    override val primaryKey = PrimaryKey(id)
    val category = reference("category", Categories)
    val name: Column<String> = varchar("name", 200)
    val description: Column<String?> = text("description").nullable()
    val telegramImage: Column<String?> = varchar("image_tlg", 200).nullable()
    val imageLink: Column<String?> = varchar("img", 200).nullable()
    val price: Column<BigDecimal?> = decimal("price", 9, 2).nullable()
    val isHidden: Column<Boolean> = bool("hide").default(false)
}
