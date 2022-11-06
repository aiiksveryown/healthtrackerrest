package ie.setu.domain.db

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object Activities : Table("activities") {
    val id = integer("id").autoIncrement().primaryKey()
    val description = varchar("description", 100)
    val duration = double("duration")
    val calories = integer("calories")
    val started = datetime("started")
    val userId = integer("user_id").references(Users.id, onDelete = ReferenceOption.CASCADE)
}