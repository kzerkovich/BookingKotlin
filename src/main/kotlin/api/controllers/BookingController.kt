package api.controllers

import api.dto.BookingResponse
import api.dto.ConfirmBookingRequest
import api.dto.CreateBookingRequest
import domain.services.BookingService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.valiktor.functions.isPositive
import org.valiktor.validate

class BookingController(private val bookingService: BookingService) {

    fun Route.registerRoutes(){
        route("/bookings") {

            post {
                val request = call.receive<CreateBookingRequest>()

                validate(request) {
                    validate(CreateBookingRequest::userId).isPositive()
                    validate(CreateBookingRequest::eventId).isPositive()
                    validate(CreateBookingRequest::ticketsCount).isPositive()
                }

                val booking = bookingService.createBooking(
                    userId = request.userId,
                    eventId = request.eventId,
                    ticketsCount = request.ticketsCount
                )

                call.respond(BookingResponse(booking))
            }

            get {
                val userId = call.request.queryParameters["userId"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("User ID is required")

                if (userId <= 0) {
                    throw IllegalArgumentException("User ID must be positive")
                }

                val bookings = bookingService.getUserBookings(userId)
                    .map { BookingResponse(it) }

                call.respond(bookings)
            }

            put("/{id}/confirm") {
                val bookingId = call.parameters["id"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid booking ID")

                val request = call.receive<ConfirmBookingRequest>()

                val updatedBooking = bookingService.confirmBooking(
                    bookingId = bookingId,
                    paymentData = request.paymentData
                )

                call.respond(BookingResponse(updatedBooking))
            }

            delete("/{id}") {
                val bookingId = call.parameters["id"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid booking ID")

                bookingService.cancelBooking(bookingId)
                call.respond(HttpStatusCode.NoContent)
            }

            post("/check-expired") {
                bookingService.checkExpiredBookings()
                call.respond(HttpStatusCode.OK, "Expired bookings processed")
            }
        }
    }
}