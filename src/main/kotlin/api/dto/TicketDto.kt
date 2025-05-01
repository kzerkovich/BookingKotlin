package api.dto

import domain.TicketStatus
import domain.entities.Ticket
import kotlinx.serialization.Serializable
import org.valiktor.functions.isPositive
import org.valiktor.validate

@Serializable
data class CreateTicketRequest(
    val eventId: Int,
    val userId: Int,
    val bookingId: Int? = null
) {
    fun validate() = validate(this) {
        validate(CreateTicketRequest::eventId).isPositive()
        validate(CreateTicketRequest::userId).isPositive()
    }

    fun toEntity() = Ticket(
        id = 0,
        eventId = eventId,
        userId = userId,
        bookingId = bookingId,
        status = TicketStatus.AVAILABLE
    )
}

@Serializable
data class PurchaseTicketsRequest(
    val paymentData: String
)

@Serializable
data class TicketResponse(
    val id: Int,
    val eventId: Int,
    val userId: Int,
    val bookingId: Int?,
    val status: TicketStatus,
    val purchaseDate: Long?
) {
    constructor(ticket: Ticket) : this(
        id = ticket.id,
        eventId = ticket.eventId,
        userId = ticket.userId,
        bookingId = ticket.bookingId,
        status = ticket.status,
        purchaseDate = ticket.purchaseDate?.time
    )
}