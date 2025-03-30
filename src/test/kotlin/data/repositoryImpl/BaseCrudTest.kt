import data.db.entities.BookingDbModel
import data.db.entities.EventDbModel
import data.db.entities.TicketDbModel
import data.db.entities.UserDbModel
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
abstract class BaseCrudTest {
    companion object {
        @Container
        val postgres = PostgreSQLContainer("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass")
    }

    @BeforeEach
    fun setup() {
        Database.connect(
            url = postgres.jdbcUrl,
            user = postgres.username,
            password = postgres.password,
            driver = "org.postgresql.Driver"
        )
        transaction {
            SchemaUtils.create(UserDbModel, EventDbModel, TicketDbModel, BookingDbModel)
        }
    }

    @AfterEach
    fun cleanup() {
        transaction {
            SchemaUtils.drop(UserDbModel, EventDbModel, TicketDbModel, BookingDbModel)
        }
    }
}