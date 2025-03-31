package exception

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

class NotFoundException(message: String) : RuntimeException(message)
class ValidationException(message: String) : RuntimeException(message)

fun Application.configureExceptionHandling() {
    install(StatusPages) {
        exception<NotFoundException> { call, cause ->
            call.respond(
                HttpStatusCode.NotFound,
                mapOf("error" to (cause.message ?: "Ресурс не найден"))
            )
        }

        exception<ValidationException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to (cause.message ?: "Некорректные данные"))
            )
        }

        exception<Throwable> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to "Внутренняя ошибка сервера")
            )
            throw cause
        }
    }
}