package ie.setu.domain.repository

import ie.setu.domain.User
import ie.setu.domain.db.Users
import ie.setu.utils.mapToUser
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * UserDAO is a repository class that handles all the database operations related to the User entity.
 */
class UserDAO {
    fun getAll() : ArrayList<User>{
        println("Getting users dao")
        val userList: ArrayList<User> = arrayListOf()
        transaction {
            Users.selectAll().map {
                userList.add(mapToUser(it)) }
        }
        println(userList)
        return userList
    }

    fun findById(id: Int): User?{
        return transaction {
            Users.select() {
                Users.id eq id}
                .map{mapToUser(it)}
                .firstOrNull()
        }
    }

    fun findByEmail(email: String): User?{
        return transaction {
            Users.select() {
                Users.email eq email}
                .map{mapToUser(it)}
                .firstOrNull()
        }
    }

    fun update(id: Int, user: User): Int{
        return transaction {
            Users.update ({
                Users.id eq id}) {
                it[name] = user.name
                it[email] = user.email
            }
        }
    }

    fun delete(id: Int):Int{
        return transaction {
            Users.deleteWhere { Users.id eq id }
        }
    }

    /**
     * This method is used to create a new user in the database.
     * @param user The user object to be created in the database.
     * @return The id of the newly created user.
     */
    fun save(user: User) : Int?{
        println("Saving user dao")
        return transaction {
            Users.insert {
                it[name] = user.name
                it[email] = user.email
            } get Users.id
        }
    }
}