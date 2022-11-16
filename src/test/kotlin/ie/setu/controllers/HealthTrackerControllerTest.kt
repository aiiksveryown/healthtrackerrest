package ie.setu.controllers

import ie.setu.config.DbConfig
import ie.setu.domain.Activity

import ie.setu.domain.User
import org.junit.jupiter.api.TestInstance

import kong.unirest.Unirest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

import ie.setu.helpers.*
import ie.setu.utils.jsonToObject
import kong.unirest.HttpResponse
import kong.unirest.JsonNode
import org.joda.time.DateTime

import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Nested

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
open class HealthTrackerControllerTest {
    val db = DbConfig().getDbConnection()
    private val app = ServerContainer.instance
    private val origin = "http://localhost:" + app.port()

    @Nested
    inner class ReadUsers {
        @Test
        fun `get all users from the database returns 200 or 404 response`() {
            val response = retrieveAllUsers()
            if (response.status == 200) {
                val retrievedUsers: ArrayList<User> = jsonToObject(response.body.toString())
                assertNotEquals(0, retrievedUsers.size)
            }
            else {
                assertEquals(404, response.status)
            }
        }

        @Test
        fun `get user by id when user does not exist returns 404 response`() {

            //Arrange - test data for user id
            val id = nonExistingId

            // Act - attempt to retrieve the non-existent user from the database
            val  retrieveResponse = retrieveUserById(id)

            // Assert -  verify return code
            assertEquals(404, retrieveResponse.status)
        }

        @Test
        fun `get user by email when user does not exist returns 404 response`() {
            // Arrange & Act - attempt to retrieve the non-existent user from the database
            val retrieveResponse = retrieveUserByEmail(nonExistingEmail)
            // Assert -  verify return code
            assertEquals(404, retrieveResponse.status)
        }

        @Test
        fun `getting a user by id when id exists, returns a 200 response`() {

            //Arrange - add the user
            val addResponse = addUser(validName, validEmail)
            val addedUser : User = jsonToObject(addResponse.body.toString())

            //Assert - retrieve the added user from the database and verify return code
            val retrieveResponse = retrieveUserById(addedUser.id)
            assertEquals(200, retrieveResponse.status)

            //After - restore the db to previous state by deleting the added user
            deleteUser(addedUser.id)
        }

        @Test
        fun `getting a user by email when email exists, returns a 200 response`() {

            //Arrange - add the user
            addUser(validName, validEmail)

            //Assert - retrieve the added user from the database and verify return code
            val retrieveResponse = retrieveUserByEmail(validEmail)
            assertEquals(200, retrieveResponse.status)

            //After - restore the db to previous state by deleting the added user
            val retrievedUser : User = jsonToObject(retrieveResponse.body.toString())
            deleteUser(retrievedUser.id)
        }
    }
    @Nested
    inner class CreateUsers {
        @Test
        fun `add a user with correct details returns a 201 response`() {

            //Arrange & Act & Assert
            //    add the user and verify return code (using fixture data)
            val addResponse = addUser(validName, validEmail)
            assertEquals(201, addResponse.status)

            //Assert - retrieve the added user from the database and verify return code
            val retrieveResponse= retrieveUserByEmail(validEmail)
            assertEquals(200, retrieveResponse.status)

            //Assert - verify the contents of the retrieved user
            val retrievedUser : User = jsonToObject(addResponse.body.toString())
            assertEquals(validEmail, retrievedUser.email)
            assertEquals(validName, retrievedUser.name)

            //After - restore the db to previous state by deleting the added user
            val deleteResponse = deleteUser(retrievedUser.id)
            assertEquals(204, deleteResponse.status)
        }
    }

    @Nested
    inner class UpdateUsers {
        @Test
        fun `updating a user when it exists, returns a 204 response`() {

            //Arrange - add the user that we plan to do an update on
            val addedResponse = addUser(validName, validEmail)
            val addedUser : User = jsonToObject(addedResponse.body.toString())

            //Act & Assert - update the email and name of the retrieved user and assert 204 is returned
            assertEquals(204, updateUser(addedUser.id, updatedName, updatedEmail).status)

            //Act & Assert - retrieve updated user and assert details are correct
            val updatedUserResponse = retrieveUserById(addedUser.id)
            val updatedUser : User = jsonToObject(updatedUserResponse.body.toString())
            assertEquals(updatedName, updatedUser.name)
            assertEquals(updatedEmail, updatedUser.email)

            //After - restore the db to previous state by deleting the added user
            deleteUser(addedUser.id)
        }

        @Test
        fun `updating a user when it doesn't exist, returns a 404 response`() {

            //Act & Assert - attempt to update the email and name of user that doesn't exist
            assertEquals(404, updateUser(-1, updatedName, updatedEmail).status)
        }
    }

    @Nested
    inner class DeleteUsers {
        @Test
        fun `deleting a user when it doesn't exist, returns a 404 response`() {
            //Act & Assert - attempt to delete a user that doesn't exist
            assertEquals(404, deleteUser(-1).status)
        }

        @Test
        fun `deleting a user when it exists, returns a 204 response`() {

            //Arrange - add the user that we plan to delete
            val addedResponse = addUser(validName, validEmail)
            val addedUser : User = jsonToObject(addedResponse.body.toString())

            //Act & Assert - delete the added user and assert a 204 is returned
            assertEquals(204, deleteUser(addedUser.id).status)

            //Act & Assert - attempt to retrieve the deleted user --> 404 response
            assertEquals(404, retrieveUserById(addedUser.id).status)
        }
    }

    @Nested
    inner class CreateActivities {
        //   post(  "/api/activities", HealthTrackerController::addActivity)
        @Test
        fun `add an activity with correct details returns a 201 response`() {

            //Arrange - add the user that we plan to do an add activity on
            val addedResponse = addUser(validName, validEmail)
            val addedUser : User = jsonToObject(addedResponse.body.toString())

            //Act & Assert - add the activity and verify return code (using fixture data)
            val addResponse = addActivity(addedUser.id, validActivityDescription, validActivityDuration, validActivityCalories, validActivityStarted )
            val addedActivity : Activity = jsonToObject(addResponse.body.toString())

            assertEquals(201, addResponse.status)

            //Assert - retrieve the added activity from the database and verify return code
            val retrieveResponse= retrieveActivityById(addedActivity.id)

            assertEquals(200, retrieveResponse.status)

            //Assert - verify the contents of the retrieved activity
            val retrievedActivity : Activity = jsonToObject(addResponse.body.toString())
            assertEquals(validActivityDescription, retrievedActivity.description)
            assertEquals(validActivityCalories, retrievedActivity.calories)
            assertEquals(validActivityDuration, retrievedActivity.duration)
            assertEquals(addedUser.id, retrievedActivity.userId)

            //After - restore the db to previous state by deleting the added user
            val deleteResponse = deleteUser(addedUser.id)
            assertEquals(204, deleteResponse.status)
        }
    }

    @Nested
    inner class ReadActivities {
        //   get(   "/api/users/:user-id/activities", HealthTrackerController::getActivitiesByUserId)
        @Test
        fun `getting activities by user id when user id exists, returns a 200 response`() {

            //Arrange - add the user
            val addResponse = addUser(validName, validEmail)
            val addedUser : User = jsonToObject(addResponse.body.toString())

            //Act & Assert - add the activity and verify return code (using fixture data)
            addActivity(addedUser.id, validActivityDescription, validActivityDuration, validActivityCalories, validActivityStarted )

            //Assert - retrieve the added user's activities from the database and verify return code
            val retrieveResponse= retrieveActivitiesByUserId(addedUser.id)

            assertEquals(200, retrieveResponse.status)

            //After - restore the db to previous state by deleting the added user
            deleteUser(addedUser.id)
        }

        @Test
        fun `getting activities by user id when user id doesn't exist, returns a 404 response`() {

            //Assert - retrieve the added user's activities from the database and verify return code
            val retrieveResponse= retrieveActivitiesByUserId(nonExistingId)

            assertEquals(404, retrieveResponse.status)
        }

        @Test
        fun `getting activities by user id when there are no activities for the user, returns a 204 response`() {

            //Arrange - add the user
            val addResponse = addUser(validName, validEmail)
            val addedUser : User = jsonToObject(addResponse.body.toString())

            //Assert - retrieve the added user's activities from the database and verify return code
            val retrieveResponse= retrieveActivitiesByUserId(addedUser.id)

            assertEquals(204, retrieveResponse.status)

            //After - restore the db to previous state by deleting the added user
            deleteUser(addedUser.id)
        }

        //   get(   "/api/activities", HealthTrackerController::getAllActivities)
        @Test
        fun `getting all activities when there are activities, returns a 200 response`() {

            //Arrange - add the user
            val addResponse = addUser(validName, validEmail)
            val addedUser : User = jsonToObject(addResponse.body.toString())

            //Act & Assert - add the activity and verify return code (using fixture data)
            addActivity(addedUser.id, validActivityDescription, validActivityDuration, validActivityCalories, validActivityStarted )

            //Assert - retrieve the added user's activities from the database and verify return code
            val retrieveResponse= retrieveAllActivities()

            assertEquals(200, retrieveResponse.status)

            //After - restore the db to previous state by deleting the added user
            deleteUser(addedUser.id)
        }

        //   get(   "/api/activities/:activity-id", HealthTrackerController::getActivitiesByActivityId)
        @Test
        fun `getting activities by activity id when activity id exists, returns a 200 response`() {
            //Arrange - add the user
            val addResponse = addUser(validName, validEmail)
            val addedUser : User = jsonToObject(addResponse.body.toString())

            //Act & Assert - add the activity and verify return code (using fixture data)
            val addActivityResponse = addActivity(addedUser.id, validActivityDescription, validActivityDuration, validActivityCalories, validActivityStarted )
            val addedActivity : Activity = jsonToObject(addActivityResponse.body.toString())

            //Assert - retrieve the added user's activities from the database and verify return code
            val retrieveResponse= retrieveActivityById(addedActivity.id)

            assertEquals(200, retrieveResponse.status)

            //After - restore the db to previous state by deleting the added user
            deleteUser(addedUser.id)
        }

        @Test
        fun `getting activities by activity id when activity id doesn't exist, returns a 404 response`() {
            //Assert - retrieve the added user's activities from the database and verify return code
            val retrieveResponse= retrieveActivityById(nonExistingId)

            assertEquals(404, retrieveResponse.status)
        }
    }

    @Nested
    inner class UpdateActivities {
        //  patch( "/api/activities/:activity-id", HealthTrackerController::updateActivity)
        @Test
        fun `updating an activity with correct details returns a 204 response`() {
            //Arrange - add the user
            val addResponse = addUser(validName, validEmail)
            val addedUser : User = jsonToObject(addResponse.body.toString())

            //Act & Assert - add the activity and verify return code (using fixture data)
            val addActivityResponse = addActivity(addedUser.id, validActivityDescription, validActivityDuration, validActivityCalories, validActivityStarted )
            val addedActivity : Activity = jsonToObject(addActivityResponse.body.toString())

            //Act - update the activity
            val updateResponse = updateActivity(addedUser.id, addedActivity.id, updatedActivityDescription, updatedActivityDuration, updatedActivityCalories, validActivityStarted )

            assertEquals(204, updateResponse.status)

            //After - restore the db to previous state by deleting the added user
            deleteUser(addedUser.id)
        }

        @Test
        fun `updating an activity that doesn't exist returns a 404 response`() {

            //Act - update the activity
            val updateResponse = updateActivity(nonExistingId, nonExistingId, updatedActivityDescription, updatedActivityDuration, updatedActivityCalories, updatedActivityStarted )

            assertEquals(404, updateResponse.status)
        }
    }

    @Nested
    inner class DeleteActivities {
        //   delete("/api/activities/:activity-id", HealthTrackerController::deleteActivityByActivityId)
        @Test
        fun `deleting an activity with correct details returns a 204 response`() {
            //Arrange - add the user
            val addResponse = addUser(validName, validEmail)
            val addedUser : User = jsonToObject(addResponse.body.toString())

            //Act & Assert - add the activity and verify return code (using fixture data)
            val addActivityResponse = addActivity(addedUser.id, validActivityDescription, validActivityDuration, validActivityCalories, validActivityStarted )
            val addedActivity : Activity = jsonToObject(addActivityResponse.body.toString())

            //Act - delete the activity
            val deleteResponse = deleteActivityByActivityId(addedActivity.id)

            assertEquals(204, deleteResponse.status)

            //After - restore the db to previous state by deleting the added user
            deleteUser(addedUser.id)
        }

        //   delete("/api/users/:user-id/activities", HealthTrackerController::deleteActivityByUserId)
        @Test
        fun `deleting all activities for a user with correct details returns a 204 response`() {
            //Arrange - add the user
            val addResponse = addUser(validName, validEmail)
            val addedUser : User = jsonToObject(addResponse.body.toString())

            //Act & Assert - add the activity
            addActivity(addedUser.id, validActivityDescription, validActivityDuration, validActivityCalories, validActivityStarted )

            //Act - delete the activity
            val deleteResponse = deleteActivitiesByUserId(addedUser.id)

            // assert
            assertEquals(204, deleteResponse.status)

            //After - restore the db to previous state by deleting the added user
            deleteUser(addedUser.id)
        }
    }

    //Helper methods
    //helper function to get all users from the database
    private fun retrieveAllUsers(): HttpResponse<JsonNode> {
        return Unirest.get("$origin/api/users/").asJson()
    }

    //helper function to add a test user to the database
    fun addUser (name: String, email: String): HttpResponse<JsonNode> {
        return Unirest.post("$origin/api/users")
            .body("{\"name\":\"$name\", \"email\":\"$email\"}")
            .asJson()
    }

    //helper function to delete a test user from the database
    fun deleteUser (id: Int): HttpResponse<String> {
        return Unirest.delete("$origin/api/users/$id").asString()
    }

    //helper function to retrieve a test user from the database by email
    fun retrieveUserByEmail(email : String) : HttpResponse<String> {
        return Unirest.get(origin + "/api/users/email/${email}").asString()
    }

    //helper function to retrieve a test user from the database by id
    fun retrieveUserById(id: Int) : HttpResponse<String> {
        return Unirest.get(origin + "/api/users/${id}").asString()
    }

    //helper function to add a test user to the database
    fun updateUser (id: Int, name: String, email: String): HttpResponse<JsonNode> {
        return Unirest.patch("$origin/api/users/$id")
            .body("{\"name\":\"$name\", \"email\":\"$email\"}")
            .asJson()
    }

    //helper function to add a test activity to the database
    fun addActivity (userId: Int, description: String, duration: Double, calories: Int, started: DateTime): HttpResponse<JsonNode> {
        return Unirest.post("$origin/api/activities")
            .body("{\"userId\":\"$userId\", \"description\":\"$description\", \"duration\":\"$duration\", \"calories\":\"$calories\", \"started\":\"$started\"}")
            .asJson()
    }

    //helper function to retrieve a test activity from the database by id
    fun retrieveActivityById(activityId: Int) : HttpResponse<String> {
        return Unirest.get(origin + "/api/activities/${activityId}").asString()
    }

    //helper function to retrieve all activities for a user from the database by user id
    fun retrieveActivitiesByUserId(userId: Int) : HttpResponse<String> {
        return Unirest.get(origin + "/api/users/${userId}/activities").asString() // ###
    }

    //helper function to retrieve all activities from the database
    fun retrieveAllActivities() : HttpResponse<String> {
        return Unirest.get("$origin/api/activities").asString()
    }

    //helper function to update a test activity in the database
    fun updateActivity (userId: Int, activityId: Int, description: String, duration: Double, calories: Int, started: DateTime): HttpResponse<JsonNode> {
        return Unirest.patch("$origin/api/activities/$activityId")
            .body("{\"userId\":\"$userId\", \"description\":\"$description\", \"duration\":\"$duration\", \"calories\":\"$calories\", \"started\":\"$started\"}")
            .asJson()
    }

    //helper function to delete a test activity from the database by activity id
    fun deleteActivityByActivityId (activityId: Int): HttpResponse<String> {
        return Unirest.delete("$origin/api/activities/$activityId").asString()
    }

    //helper function to delete activities from the database by user id
    fun deleteActivitiesByUserId (userId: Int): HttpResponse<String> {
        return Unirest.delete("$origin/api/users/$userId/activities").asString()
    }
}
