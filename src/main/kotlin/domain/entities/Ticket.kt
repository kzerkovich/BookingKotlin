package domain.entities

import domain.TicketStatus

data class Ticket(
    var id: Int,
    val eventId: Int,
    val userId: Int,
    val status: TicketStatus = TicketStatus.AVAILABLE
)
