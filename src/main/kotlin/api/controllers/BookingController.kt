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
    fun Route.registerRoutes() {
        route("/bookings") {

            post {
                try {
                    val request = call.receive<CreateBookingRequest>()
                    println("POST /bookings | UserID: ${request.userId}, EventID: ${request.eventId}, Tickets: ${request.ticketsCount}")
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
                    println("Booking ${booking.id} created")
                    call.respond(BookingResponse(booking))
                } catch (e: Exception) {
                    println("Booking creation failed")
                    throw e
                }
            }

            get {
                try {
                    val userId = call.request.queryParameters["userId"]?.toIntOrNull()
                        ?: throw IllegalArgumentException("User ID is required").also {
                            println("UserID parameter missing")
                        }

                    println("GET /bookings?userId=$userId")

                    if (userId <= 0) {
                        println("Invalid UserID: $userId")
                        throw IllegalArgumentException("User ID must be positive")
                    }

                    val bookings = bookingService.getUserBookings(userId)
                        .map { BookingResponse(it) }

                    println("Found ${bookings.size} bookings")
                    call.respond(bookings)
                } catch (e: Exception) {
                    println("Failed to get bookings")
                    throw e
                }
            }

            put("/{id}/confirm") {
                try {
                    val bookingId = call.parameters["id"]?.toIntOrNull()
                        ?: throw IllegalArgumentException("Invalid booking ID").also {
                            println("BookingID parameter missing")
                        }

                    println("PUT /bookings/$bookingId/confirm")
                    val request = call.receive<ConfirmBookingRequest>()

                    val updatedBooking = bookingService.confirmBooking(
                        bookingId = bookingId,
                        paymentData = request.paymentData
                    )

                    println("Booking $bookingId confirmed")
                    call.respond(BookingResponse(updatedBooking))
                } catch (e: Exception) {
                    println("Booking confirmation failed")
                    throw e
                }
            }

            delete("/{id}") {
                try {
                    val bookingId = call.parameters["id"]?.toIntOrNull()
                        ?: throw IllegalArgumentException("Invalid booking ID").also {
                            println("BookingID parameter missing")
                        }

                    println("DELETE /bookings/$bookingId")
                    bookingService.cancelBooking(bookingId)
                    println("Booking $bookingId deleted")
                    call.respond(HttpStatusCode.NoContent)
                } catch (e: Exception) {
                    println("Booking deletion failed")
                    throw e
                }
            }

            post("/check-expired") {
                try {
                    println("Processing expired bookings")
                    bookingService.checkExpiredBookings()
                    println("Expired bookings processed")
                    call.respond(HttpStatusCode.OK, "Expired bookings processed")
                } catch (e: Exception) {
                    println("Failed to process expired bookings")
                    throw e
                }
            }
        }
    }
}