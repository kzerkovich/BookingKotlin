package api.controllers

import domain.entities.Booking
import domain.repository.BookingRepository
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.valiktor.validate
import org.valiktor.functions.*

fun Route.bookingController(repository: BookingRepository) {
    route("/bookings") {
        post {
            val booking = call.receive<Booking>().apply {
                validate(this) {
                    validate(Booking::eventId).isPositive()
                    validate(Booking::userId).isPositive()
                }
            }
            val createdBooking = repository.addBooking(booking)
            call.respond(HttpStatusCode.Created, createdBooking)
        }

        get {
            val userId = call.request.queryParameters["userId"]?.toIntOrNull()
                ?: throw IllegalArgumentException("User ID is required")
            val bookings = repository.getAllBookings().filter { it.userId == userId }
            call.respond(bookings)
        }

        delete("/{bookingId}") {
            val bookingId = call.parameters["bookingId"]?.toIntOrNull()
                ?: throw IllegalArgumentException("Invalid booking ID")
            repository.deleteBooking(bookingId)
            call.respond(HttpStatusCode.NoContent)
        }
    }
}