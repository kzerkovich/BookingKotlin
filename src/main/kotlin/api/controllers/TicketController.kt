package api.controllers

import domain.entities.Ticket
import domain.services.TicketService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.valiktor.validate
import org.valiktor.functions.*

class TicketController(private val ticketService: TicketService) {

}