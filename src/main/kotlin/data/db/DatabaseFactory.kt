package data.db

import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import org.jetbrains.exposed.sql.Database
import java.sql.DriverManager

object DatabaseFactory {
    fun init() {
        Database.connect(
            url = "jdbc:postgresql://localhost:5432/booking_db",
            driver = "org.postgresql.Driver",
            user = "booking_user",
            password = "booking_pass"
        )

        createDatabaseIfNotExists()

        val connection = DriverManager.getConnection(
            "jdbc:postgresql://localhost:5432/booking_db",
            "booking_user",
            "booking_pass"
        )

        val database = DatabaseFactory.getInstance()
            .findCorrectDatabaseImplementation(JdbcConnection(connection))

        val liquibase = Liquibase(
            "db/changelog/db.changelog-master.xml",
            ClassLoaderResourceAccessor(),
            database
        )

        liquibase.update()
    }

    private fun createDatabaseIfNotExists() {
        val adminConnection = DriverManager.getConnection(
            "jdbc:postgresql://localhost:5432/postgres",
            "postgres",
            "password"
        )

        adminConnection.use { conn ->
            val resultSet = conn.createStatement().executeQuery(
                "SELECT 1 FROM pg_database WHERE datname = 'booking_db'"
            )

            if (!resultSet.next()) {
                conn.createStatement().execute(
                    """
                CREATE DATABASE booking_db 
                WITH OWNER = booking_user 
                ENCODING = 'UTF8'
                """
                )
            }
        }

        adminConnection.close()
    }
}