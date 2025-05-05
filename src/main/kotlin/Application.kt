import api.auth.SecurityConfiguration
import api.controllers.BookingController
import api.controllers.EventController
import api.controllers.TicketController
import api.controllers.UserController
import data.db.DatabaseFactory
import di.appModule
import exception.configureExceptionHandling
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.openapi.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin


fun main() {
    try {
        println("Starting application...")
        DatabaseFactory.init()
        embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
    } catch (e: Exception) {
        println("Application startup failed")
        throw e
    } finally {
        println("Application stopped")
    }
}

fun Application.module() {
    println("Configuring application module...")
    install(Authentication) {
        println("Initializing authentication...")
        SecurityConfiguration.apply { configureAuth() }
        println("Authentication configured")
    }

    install(Koin) {
        println("Initializing Koin DI...")
        modules(appModule)
        println("Koin DI configured")
    }

    install(ContentNegotiation) {
        println("Configuring ContentNegotiation...")
        json(Json {
            ignoreUnknownKeys = true
            prettyPrint = true
        })
        println("JSON serialization configured")
    }

    install(CORS) {
        println("Configuring CORS...")
        anyHost()
        allowCredentials = true
        allowNonSimpleContentTypes = true
        println("CORS configured")
    }

    routing {
        println("Setting up static forms...")
        staticResources("/", "static") {
            default("VK_ID.html")
        }
        println("Static forms available at /")
    }

    routing {
        println("Setting up OpenAPI documentation...")
        openAPI(path = "/api-docs", swaggerFile = "openapi.json")
        println("OpenAPI docs available at /api-docs")
    }

    println("Configuring exception handling...")
    configureExceptionHandling()
    println("Exception handling configured")

    val bookingController by inject<BookingController>()
    val eventController by inject<EventController>()
    val userController by inject<UserController>()
    val ticketController by inject<TicketController>()

    routing {
        println("Registering routes...")
        bookingController.apply {
            println("Registering booking routes")
            registerRoutes()
        }
        eventController.apply {
            println("Registering event routes")
            registerRoutes()
        }
        userController.apply {
            println("Registering user routes")
            registerRoutes()
        }
        ticketController.apply {
            println("Registering ticket routes")
            registerRoutes()
        }
        println("All routes registered")
    }

    environment.monitor.subscribe(ApplicationStarted) {
        println("Server started successfully on port 8080")
    }

    environment.monitor.subscribe(ApplicationStopPreparing) {
        println("Server shutdown initiated")
    }
}