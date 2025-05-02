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
                try {
                    val request = call.receive<CreateTicketRequest>()
                    println("POST /tickets | EventID: ${request.eventId}, BookingId: ${request.bookingId}")
                    request.validate()

                    val ticket = request.toEntity()
                    val createdTicket = ticketService.createTicket(ticket)
                    println("Ticket ${createdTicket.id} created")
                    call.respond(HttpStatusCode.Created, TicketResponse(createdTicket))
                } catch (e: Exception) {
                    println("Ticket creation failed: ${e.message}")
                    throw e
                }
            }

            get("/event/{eventId}") {
                try {
                    val eventId = call.parameters["eventId"]?.toIntOrNull()
                        ?: throw IllegalArgumentException("Invalid event ID").also {
                            println("Missing event ID parameter")
                        }

                    println("GET /tickets/event/$eventId")

                    val tickets = ticketService.getTicketsByEvent(eventId)
                        .map { TicketResponse(it) }
                    println("Found ${tickets.size} tickets for event $eventId")
                    call.respond(tickets)
                } catch (e: Exception) {
                    println("Failed to get tickets for event")
                    throw e
                }
            }

            put("/{id}/status") {
                try {
                    val ticketId = call.parameters["id"]?.toIntOrNull()
                        ?: throw IllegalArgumentException("Invalid ticket ID").also {
                            println("Missing ticket ID parameter")
                        }

                    val status = call.request.queryParameters["status"]
                        ?.let { TicketStatus.valueOf(it.uppercase()) }
                        ?: throw IllegalArgumentException("Status is required").also {
                            println("Missing status parameter")
                        }

                    println("PUT /tickets/$ticketId/status | New status: $status")
                    val updatedTicket = ticketService.updateTicketStatus(ticketId, status)
                    println("Ticket $ticketId status updated to $status")
                    call.respond(TicketResponse(updatedTicket))
                } catch (e: Exception) {
                    println("Failed to update ticket status")
                    throw e
                }
            }

            delete("/{id}") {
                try {
                    val ticketId = call.parameters["id"]?.toIntOrNull()
                        ?: throw IllegalArgumentException("Invalid ticket ID").also {
                            println("Missing ticket ID parameter")
                        }

                    println("DELETE /tickets/$ticketId")
                    ticketService.deleteTicket(ticketId)
                    println("Ticket $ticketId deleted")
                    call.respond(HttpStatusCode.NoContent)
                } catch (e: Exception) {
                    println("Failed to delete ticket")
                    throw e
                }
            }

            post("/purchase/{bookingId}") {
                try {
                    val bookingId = call.parameters["bookingId"]?.toIntOrNull()
                        ?: throw IllegalArgumentException("Invalid booking ID").also {
                            println("Missing booking ID parameter")
                        }

                    val request = call.receive<PurchaseTicketsRequest>()
                    println("POST /tickets/purchase/$bookingId | Payment data: ${request.paymentData}")
                    val tickets = ticketService.purchaseTickets(bookingId, request.paymentData)
                    println("${tickets.size} tickets purchased for booking $bookingId")
                    call.respond(tickets)
                } catch (e: Exception) {
                    println("Ticket purchase failed for booking")
                    throw e
                }
            }
        }
    }
}