package data.mappers.enums

import domain.TicketStatus

object TicketStatusMapper {
    fun fromEnumToDbModel(status: TicketStatus): String {
        return when (status) {
            TicketStatus.PURCHASED -> "PURCHASED"
            TicketStatus.BOOKED -> "BOOKED"
            TicketStatus.AVAILABLE -> "AVAILABLE"
            else -> throw Exception("Unresolved status: $status")
        }
    }

    fun fromDbModelToEnum(status: String): TicketStatus {
        return when (status) {
            "PURCHASED" -> TicketStatus.PURCHASED
            "BOOKED" -> TicketStatus.BOOKED
            "AVAILABLE" -> TicketStatus.AVAILABLE
            else -> throw Exception("Unresolved status: $status")
        }
    }
}