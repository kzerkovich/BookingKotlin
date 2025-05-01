package api

import api.controllers.BookingController
import api.dto.BookingResponse
import api.dto.ConfirmBookingRequest
import api.dto.CreateBookingRequest
import domain.BookingStatus
import domain.Serializer
import domain.entities.Booking
import domain.services.BookingService
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.mockk.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import org.junit.jupiter.api.Test
import org.valiktor.ConstraintViolationException
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BookingControllerTest {
    private val mockBookingService = mockk<BookingService>()
    private val bookingController = BookingController(mockBookingService)

    @Test
    fun `POST bookings should create booking`() = testApplication {
        val testBooking = Booking(
            id = 1,
            eventId = 2,
            userId = 3,
            ticketsCount = 4,
            bookingDate = Date(),
            expirationDate = Date(),
            status = BookingStatus.PENDING
        )

        every { mockBookingService.createBooking(any(), any(), any()) } returns testBooking

        application {
            install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    explicitNulls = false
                    serializersModule = SerializersModule {
                        contextual(Date::class, Serializer)
                    }
                })
            }
            routing {
                bookingController.apply { registerRoutes() }
            }
        }

        val client = createClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    serializersModule = SerializersModule {
                        contextual(Date::class, Serializer)
                    }
                })
            }
        }

        val request = CreateBookingRequest(
            userId = 3,
            eventId = 2,
            ticketsCount = 4
        )

        val response = client.post("/bookings") {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(request)
        }

        verify(exactly = 1) {
            mockBookingService.createBooking(
                userId = 3,
                eventId = 2,
                ticketsCount = 4
            )
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = response.body<BookingResponse>()
        assertEquals(testBooking.id, responseBody.id)
    }

    @Test
    fun `POST bookings rejects invalid data`() = testApplication {
        application {
            install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
                jackson()
            }
            routing { bookingController.apply { registerRoutes() } }
        }

        val client = createClient {
            install(ContentNegotiation) {
                jackson()
            }
        }

        listOf(
            CreateBookingRequest(0, 1, 1),
            CreateBookingRequest(1, 0, 1),
            CreateBookingRequest(1, 1, 0)
        ).forEach { badRequest ->
            assertFailsWith<ConstraintViolationException>  {
                client.post("/bookings") {
                    contentType(ContentType.Application.Json)
                    setBody(badRequest)
                }
            }
        }
    }

    @Test
    fun `GET bookings returns user bookings`() = testApplication {
        install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
            json()
        }
        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }

        val mockBookings = listOf(createTestBooking(), createTestBooking())
        coEvery { mockBookingService.getUserBookings(1) } returns mockBookings

        routing { bookingController.apply { registerRoutes() }}

        val response = client.get("/bookings?userId=1") {
            header(HttpHeaders.Accept, ContentType.Application.Json)
        }
        assertEquals(HttpStatusCode.OK, response.status)

        val bookings = response.body<List<BookingResponse>>()
        assertEquals(2, bookings.size)

        assertFailsWith<IllegalArgumentException> {
            client.get("/bookings") {
                header(HttpHeaders.Accept, ContentType.Application.Json)
            }
        }

        assertFailsWith<IllegalArgumentException> {
            client.get("/bookings?userId=-1") {
                header(HttpHeaders.Accept, ContentType.Application.Json)
            }
        }
    }


    @Test
    fun `PUT confirm booking updates status`() = testApplication {
        val mockBooking = createTestBooking().copy(status = BookingStatus.CONFIRMED)
        coEvery { mockBookingService.confirmBooking(1, any()) } returns mockBooking

        application {
            install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
                json()
            }
            routing {
                bookingController.apply { registerRoutes() }
            }
        }

        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }

        val request = ConfirmBookingRequest("payment_123")
        val response = client.put("/bookings/1/confirm") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        assertEquals(HttpStatusCode.OK, response.status)
        coVerify { mockBookingService.confirmBooking(1, "payment_123") }
    }

    @Test
    fun `DELETE booking cancels reservation`() = testApplication {
        coEvery { mockBookingService.cancelBooking(1) } just Runs

        routing { bookingController.apply { registerRoutes() } }

        val response = client.delete("/bookings/1")
        assertEquals(HttpStatusCode.NoContent, response.status)

        assertFailsWith<IllegalArgumentException> {
            client.delete("/bookings/invalid")
        }
    }

    @Test
    fun `POST check-expired processes bookings`() = testApplication {
        coEvery { mockBookingService.checkExpiredBookings() } just Runs

        routing { bookingController.apply { registerRoutes() } }

        val response = client.post("/bookings/check-expired")
        assertEquals(HttpStatusCode.OK, response.status)
        coVerify { mockBookingService.checkExpiredBookings() }
    }

    private fun createTestBooking(): Booking {
        val calendar = Calendar.getInstance()
        return Booking(
            id = 1,
            eventId = 1,
            userId = 1,
            ticketsCount = 2,
            bookingDate = calendar.time,
            expirationDate = calendar.apply { add(Calendar.HOUR, 2) }.time,
            status = BookingStatus.CONFIRMED
        )
    }
}