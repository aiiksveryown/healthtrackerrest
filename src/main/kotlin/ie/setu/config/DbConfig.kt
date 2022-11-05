package ie.setu.config

import mu.KotlinLogging
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.name

class DbConfig{
    private val logger = KotlinLogging.logger {}

    //NOTE: you need the ?sslmode=require otherwise you get an error complaining about the ssl certificate
    fun getDbConnection() :Database{
        logger.info{"Starting DB Connection..."}

        val dbConnection = Database.connect(
            "jdbc:postgresql://ec2-18-210-64-223.compute-1.amazonaws.com:5432/d9rig16jghm5lr?sslmode=require",
            driver = "org.postgresql.Driver",
            user = "onypsuyppefkzx",
            password = "63dec05445406a1d46d4a8c4ed7c7582c9d0ece41d3b70df1be8975be7eaf675")


        logger.info{"DbConfig name = " + dbConnection.name}
        logger.info{"DbConfig url = " + dbConnection.url}

        return dbConnection
    }
}