package api.controllers

import domain.entities.Event
import domain.repository.EventRepository
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.valiktor.validate
import org.valiktor.functions.*
import java.util.*

fun Route.eventController(repository: EventRepository) {
    route("/events") {
        post {
            val event = call.receive<Event>().apply {
                validate(this) {
                    validate(Event::date).isLessThanOrEqualTo(Date())
                    validate(Event::availableTickets).isPositive()
                    validate(Event::price).isPositive()
                }
            }
            repository.addEvent(event)
            call.respond(event)
        }

        get {
            val location = call.request.queryParameters["location"]
            val events = repository.getAllEvents().filter { location == null || it.location == location }
            call.respond(events)
        }

        delete("/{eventId}") {
            val eventId = call.parameters["eventId"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid ID")
            repository.deleteEvent(eventId)
            call.respond(mapOf("message" to "Event deleted"))
        }
    }
}