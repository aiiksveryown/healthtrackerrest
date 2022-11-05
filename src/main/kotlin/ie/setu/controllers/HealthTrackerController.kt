package ie.setu.controllers

import ie.setu.domain.repository.UserDAO
import io.javalin.http.Context

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import ie.setu.domain.User


object HealthTrackerController {

    private val userDao = UserDAO()

    fun getAllUsers(ctx: Context) {
        ctx.json(userDao.getAll())
    }

    fun getUserByUserId(ctx: Context) {
        val user = userDao.findById(ctx.pathParam("user-id").toInt())
        if (user != null) {
            ctx.json(user)
        }
    }

    fun getUserByEmail(ctx: Context) {
        val user = userDao.findByEmail(ctx.pathParam("email"))
        if (user != null) {
            ctx.json(user)
        }
    }

    fun addUser(ctx: Context) {
        val mapper = jacksonObjectMapper()
        val user = mapper.readValue<User>(ctx.body())
        userDao.save(user)
        ctx.json(user)
    }

    fun updateUser(ctx: Context) {
        val mapper = jacksonObjectMapper()
        val user = mapper.readValue<User>(ctx.body())
        userDao.update(ctx.pathParam("user-id").toInt(), user)
        ctx.json(user)
    }

    fun deleteUser(ctx: Context) {
        userDao.delete(ctx.pathParam("user-id").toInt())
    }
}