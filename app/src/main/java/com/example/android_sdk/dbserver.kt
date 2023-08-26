package com.example.android_sdk

import java.sql.*

object ConfigHolder {
    var databaseUrl: String? = null
    var databaseUsername: String? = null
    var databasePassword: String? = null
    var databaseDriverClassName: String? = null
}

fun setConfiguration(databaseUrl: String, databaseUsername: String, databasePassword: String, databaseDriverClassName: String) {
    ConfigHolder.databaseUrl = databaseUrl
    ConfigHolder.databaseUsername = databaseUsername
    ConfigHolder.databasePassword = databasePassword
    ConfigHolder.databaseDriverClassName = databaseDriverClassName
}

class DBConnector() {
    private var connection: Connection? = null

    fun connect() {
        try {
            // User-specified configuration
            connectToDatabase(
                ConfigHolder.databaseUrl!!,
                ConfigHolder.databaseUsername!!,
                ConfigHolder.databasePassword!!,
                ConfigHolder.databaseDriverClassName!!
            )
            println("Database Connection Successful")
        } catch (ex: ClassNotFoundException) {
            ex.printStackTrace()
        } catch (ex: SQLException) {
            ex.printStackTrace()
        }
    }

    private fun connectToDatabase(
        databaseUrl: String,
        databaseUsername: String,
        databasePassword: String,
        databaseDriverClassName: String
    ) {
        Class.forName(databaseDriverClassName)
        connection = DriverManager.getConnection(databaseUrl, databaseUsername, databasePassword)
    }

    fun disconnect() {
        try {
            connection?.close()
            println("Database Connection Closed")
        } catch (ex: SQLException) {
            ex.printStackTrace()
        }
    }

    fun getConnection(): Connection? {
        return connection
    }
}

class DBQueryExector(private val connection: Connection){
    fun executeQuery(sqlQuery : String) : ResultSet?{
        var statement : Statement? = null
        var resultSet : ResultSet? = null

        try{
            statement = connection.createStatement()
            resultSet = statement.executeQuery(sqlQuery)
            return resultSet
        } catch (ex : SQLException){
            ex.printStackTrace()
        } finally {
            statement?.close()
        }

        return null
    }
}
