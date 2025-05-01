package api.controllers

import domain.Roles
import domain.entities.Booking
import domain.services.BookingService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.valiktor.validate
import org.valiktor.functions.*

class BookingController(private val bookingService: BookingService) {

}