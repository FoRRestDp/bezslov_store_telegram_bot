package com.github.forrestdp.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column

object Categories : IntIdTable() {
    override val primaryKey = PrimaryKey(id)
    val name: Column<String> = varchar("name", 20)
    val isHidden: Column<Boolean> = bool("hide").default(false)
}