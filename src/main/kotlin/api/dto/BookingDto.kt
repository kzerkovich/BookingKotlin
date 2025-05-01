package api.dto

import domain.entities.Booking
import domain.BookingStatus
import kotlinx.serialization.Serializable


@Serializable
data class CreateBookingRequest(
    val userId: Int,
    val eventId: Int,
    val ticketsCount: Int
)

@Serializable
data class ConfirmBookingRequest(
    val paymentData: String
)

@Serializable
data class BookingResponse(
    val id: Int,
    val eventId: Int,
    val userId: Int,
    val ticketsCount: Int,
    val bookingDate: Long,
    val expirationDate: Long,
    val status: BookingStatus
) {
    constructor(booking: Booking) : this(
        id = booking.id,
        eventId = booking.eventId,
        userId = booking.userId,
        ticketsCount = booking.ticketsCount,
        bookingDate = booking.bookingDate.time,
        expirationDate = booking.expirationDate.time,
        status = booking.status
    )
}