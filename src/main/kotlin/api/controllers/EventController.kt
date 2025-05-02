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
                try {
                    val request = call.receive<CreateEventRequest>()
                    println("POST /events | ${request.name} (${request.date})")
                    request.validate()

                    val event = request.toEntity()
                    val createdEvent = eventService.createEvent(event)
                    println("Event ${createdEvent.id} created")
                    call.respond(HttpStatusCode.Created, EventResponse(createdEvent))
                } catch (e: Exception) {
                    println("Event creation failed")
                    throw e
                }
            }

            get {
                try {
                    println("GET /events")
                    val events = eventService.getAllEvents().map { EventResponse(it) }
                    println("Returning ${events.size} events")
                    call.respond(events)
                } catch (e: Exception) {
                    println("Failed to get events")
                    throw e
                }
            }

            get("/{id}") {
                try {
                    val eventId = call.parameters["id"]?.toIntOrNull()
                        ?: throw IllegalArgumentException("Invalid event ID").also {
                            println("EventID parameter missing")
                        }
                    println("GET /events/$eventId")
                    val event = eventService.getEvent(eventId)
                    println("Event details: ${event.name}")
                    call.respond(EventResponse(event))
                } catch (e: Exception) {
                    println("Failed to get event")
                    throw e
                }
            }

            put("/{id}") {
                try {
                    val eventId = call.parameters["id"]?.toIntOrNull()
                        ?: throw IllegalArgumentException("Invalid event ID").also {
                            println("EventID parameter missing")
                        }
                    println("PUT /events/$eventId")
                    val request = call.receive<UpdateEventRequest>()

                    val existingEvent = eventService.getEvent(eventId)
                    val updatedEvent = request.applyTo(existingEvent)
                    eventService.updateEvent(updatedEvent)
                    println("Event $eventId updated")
                    call.respond(EventResponse(updatedEvent))
                } catch (e: Exception) {
                    println("Event update failed")
                    throw e
                }
            }

            delete("/{id}") {
                try {
                    val eventId = call.parameters["id"]?.toIntOrNull()
                        ?: throw IllegalArgumentException("Invalid event ID").also {
                            println("EventID parameter missing")
                        }
                    println("DELETE /events/$eventId")
                    eventService.deleteEvent(eventId)
                    println("Event $eventId deleted")
                    call.respond(HttpStatusCode.NoContent)
                } catch (e: Exception) {
                    println("Event deletion failed")
                    throw e
                }
            }

            get("/location/{location}") {
                try {
                    val location = call.parameters["location"]
                        ?: throw IllegalArgumentException("Location is required").also {
                            println("Location parameter missing")
                        }

                    println("GET /events/location/$location")
                    if (location.isBlank()) {
                        println("Empty location parameter")
                        throw IllegalArgumentException("Location cannot be empty")
                    }

                    val events = eventService.getEventsByLocation(location)
                        .map { EventResponse(it) }
                    println("Found ${events.size} events in $location")
                    call.respond(events)
                } catch (e: Exception) {
                    println("Event deletion failed")
                    throw e
                }
            }

            get("/filter") {
                try {
                    val location = call.request.queryParameters["location"]
                    val category = call.request.queryParameters["category"]
                    val startDate = call.request.queryParameters["startDate"]?.toLongOrNull()?.let { Date(it) }
                    val endDate = call.request.queryParameters["endDate"]?.toLongOrNull()?.let { Date(it) }

                    println(
                        "GET /events/filter | Params: " +
                                "location=$location, category=$category, " +
                                "startDate=$startDate, endDate=$endDate"
                    )

                    val events = eventService.getFilteredEvents(location, category, startDate, endDate)
                        .map { EventResponse(it) }
                    println("Found ${events.size} events matching filters")
                    call.respond(events)
                } catch (e: Exception) {
                    println("Event deletion failed")
                    throw e
                }
            }

            authenticate("auth-jwt") {
                put("/{id}/tickets") {
                    try {
                        val eventId = call.parameters["id"]?.toIntOrNull()
                            ?: throw IllegalArgumentException("Invalid event ID").also {
                                println("Missing event ID in tickets update")
                            }
                        val newTotal = call.request.queryParameters["total"]?.toIntOrNull()
                            ?: throw IllegalArgumentException("Total tickets required").also {
                                println("Missing 'total' parameter")
                            }

                        val userPrincipal = call.principal<JWTPrincipal>()
                            ?: throw AccessDeniedException("Authentication required").also {
                                println("Unauthorized tickets update attempt")
                            }
                        val role = fromDbModelToEnum(userPrincipal.payload.getClaim("role").asString())
                        println(
                            "PUT /events/$eventId/tickets | " +
                                    "New total: $newTotal | " +
                                    "Initiator role: $role"
                        )

                        val event = eventService.updateEventTickets(eventId, newTotal, role)
                        println("Tickets updated for event $eventId. New total: $newTotal")
                        call.respond(EventResponse(event))
                    } catch (e: Exception) {
                        println("Failed to update tickets for event")
                        throw e
                    }
                }
            }

            authenticate("auth-jwt") {
                post("/{id}/cancel") {
                    try {
                        val eventId = call.parameters["id"]?.toIntOrNull()
                            ?: throw IllegalArgumentException("Invalid event ID").also {
                                println("Missing event ID in cancel request")
                            }
                        val requesterRole = call.principal<JWTPrincipal>()
                            ?: throw AccessDeniedException("Authentication required").also {
                                println("Unauthorized cancel attempt")
                            }

                        val role = fromDbModelToEnum(requesterRole.payload.getClaim("role").asString())
                        println(
                            "POST /events/$eventId/cancel | " +
                                    "Initiator role: $role"
                        )

                        eventService.cancelEvent(eventId, role)
                        println("Event $eventId cancelled by $role")
                        call.respond(HttpStatusCode.OK)
                    } catch (e: Exception) {
                        println("Event deletion failed")
                        throw e
                    }
                }
            }

            authenticate("auth-jwt") {
                put("/{id}/price") {
                    try {
                        val eventId = call.parameters["id"]?.toIntOrNull()
                            ?: throw IllegalArgumentException("Invalid event ID").also {
                                println("Missing event ID in price update")
                            }

                        val newPrice = call.request.queryParameters["price"]?.toDoubleOrNull()
                            ?: throw IllegalArgumentException("Price is required").also {
                                println("Missing 'price' parameter")
                            }

                        val userPrincipal = call.principal<UserPrincipal>()
                            ?: throw AccessDeniedException("Authentication required").also {
                                println("Unauthorized price update attempt")
                            }
                        println(
                            "PUT /events/$eventId/price | " +
                                    "New price: $newPrice | " +
                                    "Initiator role: ${userPrincipal.role}"
                        )

                        val event = eventService.updateEventPrice(eventId, newPrice, userPrincipal.role)
                        println("Price updated for event $eventId. New price: $newPrice")
                        call.respond(EventResponse(event))
                    } catch (e: Exception) {
                        println("Event deletion failed")
                        throw e
                    }
                }
            }
        }
    }
}