package api.controllers

import api.auth.UserPrincipal
import api.dto.CreateEventRequest
import api.dto.EventResponse
import api.dto.UpdateEventRequest
import data.mappers.enums.RolesMapper.fromDbModelToEnum
import domain.services.EventService
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.nio.file.AccessDeniedException
import java.util.*


class EventController(private val eventService: EventService) {

    fun Route.registerRoutes() {
        route("/events") {

            post {
                val request = call.receive<CreateEventRequest>()
                request.validate()

                val event = request.toEntity()
                val createdEvent = eventService.createEvent(event)
                call.respond(HttpStatusCode.Created, EventResponse(createdEvent))
            }

            get {
                val events = eventService.getAllEvents().map { EventResponse(it) }
                call.respond(events)
            }

            get("/{id}") {
                val eventId = call.parameters["id"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid event ID")
                val event = eventService.getEvent(eventId)
                call.respond(EventResponse(event))
            }

            put("/{id}") {
                val eventId = call.parameters["id"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid event ID")
                val request = call.receive<UpdateEventRequest>()

                val existingEvent = eventService.getEvent(eventId)
                val updatedEvent = request.applyTo(existingEvent)
                eventService.updateEvent(updatedEvent)
                call.respond(EventResponse(updatedEvent))
            }

            delete("/{id}") {
                val eventId = call.parameters["id"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid event ID")
                eventService.deleteEvent(eventId)
                call.respond(HttpStatusCode.NoContent)
            }

            get("/location/{location}") {
                val location = call.parameters["location"]
                    ?: throw IllegalArgumentException("Location is required")

                if (location.isBlank()) {
                    throw IllegalArgumentException("Location cannot be empty")
                }

                val events = eventService.getEventsByLocation(location)
                    .map { EventResponse(it) }

                call.respond(events)
            }

            get("/filter") {
                val location = call.request.queryParameters["location"]
                val category = call.request.queryParameters["category"]
                val startDate = call.request.queryParameters["startDate"]?.toLongOrNull()?.let { Date(it) }
                val endDate = call.request.queryParameters["endDate"]?.toLongOrNull()?.let { Date(it) }

                val events = eventService.getFilteredEvents(location, category, startDate, endDate)
                    .map { EventResponse(it) }
                call.respond(events)
            }

            authenticate("auth-jwt") {
                put("/{id}/tickets") {
                    val eventId = call.parameters["id"]?.toIntOrNull()
                        ?: throw IllegalArgumentException("Invalid event ID")
                    val newTotal = call.request.queryParameters["total"]?.toIntOrNull()
                        ?: throw IllegalArgumentException("Total tickets required")

                    val userPrincipal = call.principal<JWTPrincipal>()
                        ?: throw AccessDeniedException("Authentication required")

                    val event = eventService.updateEventTickets(eventId, newTotal, fromDbModelToEnum(userPrincipal.payload.getClaim("role").asString()))
                    call.respond(EventResponse(event))
                }
            }

            authenticate("auth-jwt") {
                post("/{id}/cancel") {
                    val eventId = call.parameters["id"]?.toIntOrNull()
                        ?: throw IllegalArgumentException("Invalid event ID")
                    val requesterRole = call.principal<JWTPrincipal>()
                        ?: throw AccessDeniedException("Authentication required")

                    eventService.cancelEvent(eventId, fromDbModelToEnum(requesterRole.payload.getClaim("role").asString()))
                    call.respond(HttpStatusCode.OK)
                }
            }

            authenticate("auth-jwt") {
                put("/{id}/price") {
                    val eventId = call.parameters["id"]?.toIntOrNull()
                        ?: throw IllegalArgumentException("Invalid event ID")

                    val newPrice = call.request.queryParameters["price"]?.toDoubleOrNull()
                        ?: throw IllegalArgumentException("Price is required")

                    val userPrincipal = call.principal<UserPrincipal>()
                        ?: throw AccessDeniedException("Authentication required")

                    val event = eventService.updateEventPrice(eventId, newPrice, userPrincipal.role)
                    call.respond(EventResponse(event))
                }
            }
        }
    }
}