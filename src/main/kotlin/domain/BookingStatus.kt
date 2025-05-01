package domain

import kotlinx.serialization.Serializable

@Serializable
enum class BookingStatus {
    PENDING, CONFIRMED, CANCELLED
}