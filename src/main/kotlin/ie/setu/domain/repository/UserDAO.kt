package ie.setu.domain.repository

import ie.setu.domain.User
import ie.setu.domain.db.Users
import ie.setu.utils.mapToUser
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction



class UserDAO {
    fun getAll() : ArrayList<User>{
        val userList: ArrayList<User> = arrayListOf()
        transaction {
            Users.selectAll().map {
                userList.add(mapToUser(it)) }
        }
        return userList
    }

    fun findById(id: Int): User?{
        return null
    }

    fun findByEmail(email: String): User?{
        return null
    }

    fun update(id: Int, user: User) {
//        val userToUpdate = findById(id)
//        if (userToUpdate != null) {
//            userToUpdate.name = user.name
//            userToUpdate.email = user.email
//        }
    }

    fun delete(id: Int){
//        users.removeIf {it.id == id}
    }

    fun save(user: User){
//        users.add(user)
    }
}