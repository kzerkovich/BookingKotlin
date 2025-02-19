package domain.entities

import domain.TicketStatus

data class Ticket(
    var id: Int = UNDEFINED_ID,
    val eventId: Int,
    val userId: Int,
    val status: TicketStatus = TicketStatus.AVAILABLE
) {
    companion object {
        const val UNDEFINED_ID = -1
    }
}
