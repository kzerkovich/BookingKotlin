package exception

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import javax.security.sasl.AuthenticationException

class NotFoundException(message: String) : RuntimeException(message)
class ValidationException(message: String) : RuntimeException(message)

fun Application.configureExceptionHandling() {
    install(StatusPages) {
        exception<NotFoundException> { call, cause ->
            call.respond(HttpStatusCode.NotFound, mapOf("error" to (cause.message ?: "Ресурс не найден")))
        }

        exception<ValidationException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to (cause.message ?: "Некорректные данные")))
        }

        exception<IllegalArgumentException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to (cause.message ?: "Ошибка валидации")))
        }

        exception<NoSuchElementException> { call, _ ->
            call.respond(HttpStatusCode.NotFound, mapOf("error" to "Ресурс не найден"))
        }

        exception<AccessDeniedException> { call, cause ->
            call.respond(HttpStatusCode.Forbidden, mapOf("error" to (cause.message ?: "Доступ запрещен")))
        }

        exception<AuthenticationException> { call, cause ->
            call.respond(HttpStatusCode.Unauthorized, mapOf("error" to (cause.message ?: "Ошибка авторизации")))
        }

        exception<Throwable> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to "Внутренняя ошибка сервера: ${cause.message}")
            )
        }
    }
}