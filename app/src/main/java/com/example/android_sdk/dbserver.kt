package io.kthulu.sdk

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
            if (ConfigHolder.databaseUrl != null && ConfigHolder.databaseUsername != null &&
                ConfigHolder.databasePassword != null && ConfigHolder.databaseDriverClassName != null
            ) {
                // User-specified configuration
                connectToDatabase(
                    ConfigHolder.databaseUrl!!,
                    ConfigHolder.databaseUsername!!,
                    ConfigHolder.databasePassword!!,
                    ConfigHolder.databaseDriverClassName!!
                )
            } else {
                // Manual configuration
                connectToDatabase(
                    "jdbc:mariadb://210.207.161.10:3306/kthulu?useUnicode=true&amp;characterEncoding=UTF-8&amp;useSSL=false",
                    "kthulu",
                    "kthulu123",
                    "org.mariadb.jdbc.Driver"
                )
            }

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

        try{
            statement = connection.createStatement()
            return statement.executeQuery(sqlQuery)
        } catch (ex : SQLException){
            ex.printStackTrace()
        } finally {
            statement?.close()
        }

        return null
    }
}