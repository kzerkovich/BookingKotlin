package data.repositoryImpl

import domain.entities.Booking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.*

class BookingRepositoryImplTest {
    private val  testBookingRepositoryImpl: BookingRepositoryImpl = BookingRepositoryImpl()

    private val bookingTest1 = Booking(
        id = -1,
        bookingDate = Date(System.currentTimeMillis()),
        eventID = EVENT_ID,
        userID = USER_ID
    )

    private val bookingTest2 = Booking(
        id = -1,
        bookingDate = Date(System.currentTimeMillis()),
        eventID = EVENT_ID,
        userID = USER_ID
    )

    private val bookingTest3 = Booking(
        id = 10,
        bookingDate = Date(System.currentTimeMillis()),
        eventID = EVENT_ID,
        userID = USER_ID
    )

    private val bookingTestEdited = Booking(
        id = 10,
        bookingDate = Date(System.currentTimeMillis()),
        eventID = EVENT_ID,
        userID = USER_ID + 100
    )

    @Test
    @DisplayName("addBooking test")
    fun addBooking() {
        testBookingRepositoryImpl.addBooking(bookingTest1)
        testBookingRepositoryImpl.addBooking(bookingTest2)
        testBookingRepositoryImpl.addBooking(bookingTest3)

        assertEquals(3, testBookingRepositoryImpl.bookingList.size)
    }

    @Test
    @DisplayName("deleteBooking test")
    fun deleteBooking() {
        testBookingRepositoryImpl.addBooking(bookingTest1)
        testBookingRepositoryImpl.addBooking(bookingTest2)
        testBookingRepositoryImpl.addBooking(bookingTest3)

        testBookingRepositoryImpl.deleteBooking(bookingTest1)
        assertEquals(2, testBookingRepositoryImpl.bookingList.size)

        testBookingRepositoryImpl.deleteBooking(bookingTest2)
        assertEquals(1, testBookingRepositoryImpl.bookingList.size)

        testBookingRepositoryImpl.deleteBooking(bookingTest3)
        assertEquals(0, testBookingRepositoryImpl.bookingList.size)
    }

    @Test
    @DisplayName("editBooking test")
    fun editBooking() {
        testBookingRepositoryImpl.addBooking(bookingTest1)
        testBookingRepositoryImpl.addBooking(bookingTest2)
        testBookingRepositoryImpl.addBooking(bookingTest3)

        testBookingRepositoryImpl.editBooking(bookingTestEdited)
        assertEquals(testBookingRepositoryImpl.getBooking(10).userID, USER_ID + 100)
    }

    @Test
    @DisplayName("getBooking test")
    fun getBooking() {
        testBookingRepositoryImpl.addBooking(bookingTest1)
        testBookingRepositoryImpl.addBooking(bookingTest2)
        testBookingRepositoryImpl.addBooking(bookingTest3)

        assertEquals(bookingTest1, testBookingRepositoryImpl.getBooking(0))
        assertEquals(bookingTest2, testBookingRepositoryImpl.getBooking(1))
        assertEquals(bookingTest3, testBookingRepositoryImpl.getBooking(10))
    }

    @Test
    @DisplayName("getAllBookings test")
    fun getAllBookings() {
        testBookingRepositoryImpl.addBooking(bookingTest1)
        testBookingRepositoryImpl.addBooking(bookingTest2)
        testBookingRepositoryImpl.addBooking(bookingTest3)

        assertEquals(mutableListOf(bookingTest1, bookingTest2, bookingTest3), testBookingRepositoryImpl.getAllBookings())
    }

    companion object {
        private const val EVENT_ID = 10
        private const val USER_ID = 100
    }
}