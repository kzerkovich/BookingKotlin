package domain

import kotlinx.serialization.Serializable

@Serializable
enum class TicketStatus {
    BOOKED, PURCHASED, AVAILABLE
}