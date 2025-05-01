package data.services

import domain.entities.Booking
import domain.BookingStatus
import domain.Roles
import domain.entities.Event
import domain.entities.User
import domain.repository.BookingRepository
import domain.repository.EventRepository
import domain.repository.UsersRepository
import domain.services.BookingService
import io.mockk.MockKException
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

class BookingServiceTest {
    private val bookingRepository = mockk<BookingRepository>()
    private val eventRepository = mockk<EventRepository>()
    private val userRepository = mockk<UsersRepository>()
    private lateinit var bookingService: BookingService

    @BeforeEach
    fun setup() {
        val testModule = module {
            single { bookingRepository }
            single { eventRepository }
            single { userRepository }
            single { BookingService(get(), get(), get()) }
        }
        startKoin { modules(testModule) }
        bookingService = BookingService(bookingRepository, eventRepository, userRepository)
    }

    @Test
    fun `createBooking should throw error when user is banned`() {
        val userId = 1
        val bannedUser = User(
            id = userId,
            login = "banned_user",
            password = "pass",
            email = "test@test.com",
            bannedUntil = Date(System.currentTimeMillis() + 86400000),
            role = Roles.USER
        )

        every { userRepository.getUser(userId) } returns bannedUser
        every { eventRepository.getEvent(any()) } returns Event(
            id = 1,
            name = "Test",
            description = "Test",
            date = Date(),
            location = "Test",
            category = "Test",
            totalTickets = 100,
            availableTickets = 100,
            price = 100.0
        )

        assertThrows<MockKException> {
            bookingService.createBooking(
                0,
                eventId = 1,
                ticketsCount = 1
            )
        }
    }

    @AfterEach
    fun tearDown() {
        stopKoin()
    }
}