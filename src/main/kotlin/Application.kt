import api.controllers.BookingController
import api.controllers.EventController
import api.controllers.TicketController
import api.controllers.UserController
import data.db.DatabaseFactory
import di.appModule
import exception.configureExceptionHandling
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.openapi.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin

fun main() {
    DatabaseFactory.init()
    embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
}

fun Application.module() {
    install(Koin) {
        modules(appModule)
    }

    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            prettyPrint = true
        })
    }

    routing {
        openAPI(path="/", swaggerFile = "openapi.json")
    }

    val bookingController by inject<BookingController>()
    val eventController by inject<EventController>()
    val userController by inject<UserController>()
    val ticketController by inject<TicketController>()



    configureExceptionHandling()
}