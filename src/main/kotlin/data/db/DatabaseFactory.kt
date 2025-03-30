package data.db

import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import org.jetbrains.exposed.sql.Database
import java.sql.DriverManager

object DatabaseFactory {
    fun init() {
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

        connection.close()

        Database.connect(
            url = "jdbc:postgresql://localhost:5432/booking_db",
            user = "booking_user",
            password = "booking_pass"
        )
    }
}