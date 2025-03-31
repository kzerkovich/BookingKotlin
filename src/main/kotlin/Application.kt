import data.db.DatabaseFactory
import di.appModule
import domain.usecases.BookingUseCase
import domain.usecases.EventUseCase
import domain.usecases.TicketUseCase
import domain.usecases.UserUseCase
import org.koin.core.context.GlobalContext.startKoin
import exception.configureExceptionHandling
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.openapi.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
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
    configureExceptionHandling()

    val userController: UserUseCase by inject(named("userController"))
    val eventController: EventUseCase by inject(named("eventController"))
    val bookingController: BookingUseCase by inject(named("bookingController"))
    val ticketController: TicketUseCase by inject(named("ticketController"))


    routing {
        userController
        eventController
        bookingController
        ticketController
    }
}