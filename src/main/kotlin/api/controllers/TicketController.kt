package api.controllers

import domain.entities.Ticket
import domain.repository.TicketRepository
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.valiktor.functions.isPositive
import org.valiktor.validate

fun Route.ticketController(repository: TicketRepository) {
    route("/tickets") {
        get {
            val eventId = call.request.queryParameters["eventId"]?.toIntOrNull()
                ?: throw IllegalArgumentException("Event ID is required")
            val tickets = repository.getAllTickets().filter { it.eventId == eventId }
            call.respond(tickets)
        }

        post {
            val ticket = call.receive<Ticket>().apply {
                validate(this) {
                    validate(Ticket::eventId).isPositive()
                    validate(Ticket::userId).isPositive()
                }
            }
            val createdTicket = repository.addTicket(ticket)
            call.respond(HttpStatusCode.Created, createdTicket)
        }

        delete("/{ticketId}") {
            val ticketId = call.parameters["ticketId"]?.toIntOrNull()
                ?: throw IllegalArgumentException("Invalid ticket ID")
            repository.deleteTicket(ticketId)
            call.respond(HttpStatusCode.NoContent)
        }
    }
}