package data.repositoryImpl

import domain.TicketStatus
import domain.entities.Ticket
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TicketRepositoryImplTest {
    private val testTicketRepositoryImpl: TicketRepositoryImpl = TicketRepositoryImpl()

    private val ticketTest1 = Ticket(
        id = -1,
        eventId = EVENT_ID,
        userId = USER_ID
    )
    private val ticketTest2 = Ticket(
        id = -1,
        eventId = EVENT_ID,
        userId = USER_ID
    )
    private val ticketTest3 = Ticket(
        id = 5,
        eventId = EVENT_ID,
        userId = USER_ID
    )
    private val ticketTestEdited = Ticket(
        id = 5,
        eventId = EVENT_ID,
        userId = USER_ID,
        status = TicketStatus.PURCHASED
    )
    @Test
    @DisplayName("addTicket test")
    fun addTicket() {
        testTicketRepositoryImpl.addTicket(ticketTest1)
        testTicketRepositoryImpl.addTicket(ticketTest2)
        testTicketRepositoryImpl.addTicket(ticketTest3)

        assertEquals(3, testTicketRepositoryImpl.ticketList.size)
    }

    @Test
    @DisplayName("deleteTicket test")
    fun deleteTicket() {
        testTicketRepositoryImpl.addTicket(ticketTest1)
        testTicketRepositoryImpl.addTicket(ticketTest2)
        testTicketRepositoryImpl.addTicket(ticketTest3)

        testTicketRepositoryImpl.deleteTicket(ticketTest1)
        assertEquals(2, testTicketRepositoryImpl.ticketList.size)

        testTicketRepositoryImpl.deleteTicket(ticketTest2)
        assertEquals(1, testTicketRepositoryImpl.ticketList.size)

        testTicketRepositoryImpl.deleteTicket(ticketTest3)
        assertEquals(0, testTicketRepositoryImpl.ticketList.size)
    }

    @Test
    @DisplayName("editTicket test")
    fun editTicket() {
        testTicketRepositoryImpl.addTicket(ticketTest1)
        testTicketRepositoryImpl.addTicket(ticketTest2)
        testTicketRepositoryImpl.addTicket(ticketTest3)

        testTicketRepositoryImpl.editTicket(ticketTestEdited)
        assertEquals(testTicketRepositoryImpl.getTicket(5).status, TicketStatus.PURCHASED)
    }

    @Test
    @DisplayName("getTicket test")
    fun getTicket() {
        testTicketRepositoryImpl.addTicket(ticketTest1)
        testTicketRepositoryImpl.addTicket(ticketTest2)
        testTicketRepositoryImpl.addTicket(ticketTest3)

        assertEquals(ticketTest1, testTicketRepositoryImpl.getTicket(0))
        assertEquals(ticketTest2, testTicketRepositoryImpl.getTicket(1))
        assertEquals(ticketTest3, testTicketRepositoryImpl.getTicket(5))
    }

    @Test
    @DisplayName("getAllTickets test")
    fun getAllTickets() {
        testTicketRepositoryImpl.addTicket(ticketTest1)
        testTicketRepositoryImpl.addTicket(ticketTest2)
        testTicketRepositoryImpl.addTicket(ticketTest3)

        assertEquals(mutableListOf(ticketTest1, ticketTest2,ticketTest3), testTicketRepositoryImpl.getAllTickets())
    }

    companion object {
        private const val EVENT_ID = 10
        private const val USER_ID = 100
    }
}