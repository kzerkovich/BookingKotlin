package data.mappers.enums

import domain.BookingStatus

object BookingStatusMapper {
    fun fromEnumToDbModel(status: BookingStatus): String {
        return when (status) {
            BookingStatus.PENDING -> "PENDING"
            BookingStatus.CANCELLED -> "CANCELLED"
            BookingStatus.CONFIRMED -> "CONFIRMED"
            else -> throw Exception("Unresolved status: $status")
        }
    }

    fun fromDbModelToEnum(status: String): BookingStatus {
        return when (status) {
            "PENDING" -> BookingStatus.PENDING
            "CANCELLED" -> BookingStatus.CANCELLED
            "CONFIRMED" -> BookingStatus.CONFIRMED
            else -> throw Exception("Unresolved status: $status")
        }
    }
}