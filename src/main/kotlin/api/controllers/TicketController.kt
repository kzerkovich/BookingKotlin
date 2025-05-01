package api.controllers

import api.dto.CreateTicketRequest
import api.dto.PurchaseTicketsRequest
import api.dto.TicketResponse
import domain.TicketStatus
import domain.services.TicketService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class TicketController(private val ticketService: TicketService) {

    fun Route.registerRoutes() {
        route("/tickets") {

            post {
                val request = call.receive<CreateTicketRequest>()
                request.validate()

                val ticket = request.toEntity()
                val createdTicket = ticketService.createTicket(ticket)
                call.respond(HttpStatusCode.Created, TicketResponse(createdTicket))
            }

            get("/event/{eventId}") {
                val eventId = call.parameters["eventId"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid event ID")

                val tickets = ticketService.getTicketsByEvent(eventId)
                    .map { TicketResponse(it) }
                call.respond(tickets)
            }

            put("/{id}/status") {
                val ticketId = call.parameters["id"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid ticket ID")

                val status = call.request.queryParameters["status"]
                    ?.let { TicketStatus.valueOf(it.uppercase()) }
                    ?: throw IllegalArgumentException("Status is required")

                val updatedTicket = ticketService.updateTicketStatus(ticketId, status)
                call.respond(TicketResponse(updatedTicket))
            }

            delete("/{id}") {
                val ticketId = call.parameters["id"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid ticket ID")

                ticketService.deleteTicket(ticketId)
                call.respond(HttpStatusCode.NoContent)
            }

            post("/purchase/{bookingId}") {
                val bookingId = call.parameters["bookingId"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid booking ID")

                val request = call.receive<PurchaseTicketsRequest>()

                val tickets = ticketService.purchaseTickets(bookingId, request.paymentData)
                call.respond(tickets.map { TicketResponse(it) })
            }
        }
    }
}