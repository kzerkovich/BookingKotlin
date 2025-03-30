package data.usecases

import domain.entities.Booking
import domain.BookingStatus
import domain.entities.Event
import domain.repository.BookingRepository
import domain.repository.EventRepository
import domain.repository.UsersRepository
import domain.usecases.BookingUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import java.util.*

class BookingUseCaseTest {
    private val bookingRepository = mockk<BookingRepository>()
    private val eventRepository = mockk<EventRepository>()
    private val userRepository = mockk<UsersRepository>()
    private lateinit var bookingUseCase: BookingUseCase

    @BeforeEach
    fun setup() {
        val testModule = module {
            single { bookingRepository }
            single { eventRepository }
            single { userRepository }
            single { BookingUseCase(get(), get(), get()) }
        }
        startKoin { modules(testModule) }
        bookingUseCase = BookingUseCase(bookingRepository, eventRepository, userRepository)
    }

    @Test
    fun `createBooking should throw error when no tickets available`() {
        val eventId = 1
        val mockEvent = Event(
            id = eventId,
            name = "Test Event",
            date = Date(),
            location = "Test Location",
            category = "Test Category",
            availableTickets = 0,
            price = 100.0
        )

        every { eventRepository.getEvent(eventId) } returns mockEvent
        every { userRepository.getUser(any()) } returns mockk()

        assertThrows<IllegalArgumentException> {
            bookingUseCase.createBooking(
                Booking(
                    id = 0,
                    eventId = eventId,
                    userId = 1,
                    bookingDate = Date()
                )
            )
        }

        verify(exactly = 0) { bookingRepository.addBooking(any()) }
    }

    @Test
    fun `cancelBooking should update status to CANCELLED`() {
        val bookingId = 1
        every { bookingRepository.getBooking(bookingId) } returns Booking(
            id = bookingId,
            status = BookingStatus.CONFIRMED,
            eventId = 123,
            userId = 1,
            bookingDate = Date()
        )
        every { bookingRepository.editBooking(any()) } returns 1

        bookingUseCase.cancelBooking(bookingId)

        verify { bookingRepository.editBooking(match { it.status == BookingStatus.CANCELLED }) }
    }

    @AfterEach
    fun tearDown() {
        stopKoin()
    }
}