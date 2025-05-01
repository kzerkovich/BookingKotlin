package api

import api.controllers.TicketController
import api.dto.CreateTicketRequest
import api.dto.PurchaseTicketsRequest
import api.dto.TicketResponse
import domain.TicketStatus
import domain.entities.Ticket
import domain.services.TicketService
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import org.junit.jupiter.api.Test
import org.valiktor.ConstraintViolationException
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TicketControllerTest {
    private val mockTicketService = mockk<TicketService>()
    private val ticketController = TicketController(mockTicketService)

    @Test
    fun `POST tickets creates new ticket`() = testApplication {
        val testTicket = Ticket(
            id = 1,
            eventId = 2,
            userId = 3,
            status = TicketStatus.AVAILABLE
        )

        every { mockTicketService.createTicket(any()) } returns testTicket

        setupApplication()

        val client = createClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }

        val request = CreateTicketRequest(
            eventId = 2,
            userId = 3
        )

        val response = client.post("/tickets") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val responseBody = response.body<TicketResponse>()
        assertEquals(1, responseBody.id)
        verify(exactly = 1) { mockTicketService.createTicket(any()) }
    }

    @Test
    fun `POST tickets rejects invalid data`() = testApplication {
        setupApplication()

        val client = createClient {
            install(ContentNegotiation) { json() }
        }

        listOf(
            CreateTicketRequest(eventId = 0, userId = 1),
            CreateTicketRequest(eventId = 1, userId = -5)
        ).forEach { badRequest ->
            assertFailsWith<ConstraintViolationException> {
                client.post("/tickets") {
                    contentType(ContentType.Application.Json)
                    setBody(badRequest)
                }
            }
        }
    }

    @Test
    fun `GET tickets by event returns list`() = testApplication {
        val testTickets = listOf(
            Ticket(1, 2, 3),
            Ticket(2, 2, 4)
        )

        every { mockTicketService.getTicketsByEvent(2) } returns testTickets

        setupApplication()

        val client = createClient {
            install(ContentNegotiation) { json() }
        }

        val response = client.get("/tickets/event/2")
        assertEquals(HttpStatusCode.OK, response.status)
        val tickets = response.body<List<TicketResponse>>()
        assertEquals(2, tickets.size)

        assertFailsWith<IllegalArgumentException> {
            client.get("/tickets/event/invalid")
        }
    }

    @Test
    fun `PUT ticket status updates correctly`() = testApplication {
        val updatedTicket = Ticket(1, 2, 3, status = TicketStatus.BOOKED)
        every { mockTicketService.updateTicketStatus(1, TicketStatus.BOOKED) } returns updatedTicket

        setupApplication()

        val client = createClient {
            install(ContentNegotiation) { json() }
        }

        val response = client.put("/tickets/1/status?status=booked") {
            contentType(ContentType.Application.Json)
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(TicketStatus.BOOKED, response.body<TicketResponse>().status)

        assertFailsWith<IllegalArgumentException> {
            client.put("/tickets/invalid/status?status=booked")
        }

        assertFailsWith<IllegalArgumentException> {
            client.put("/tickets/1/status")
        }
    }

    @Test
    fun `DELETE ticket removes it`() = testApplication {
        every { mockTicketService.deleteTicket(1) } returns 1

        setupApplication()

        val response = client.delete("/tickets/1")
        assertEquals(HttpStatusCode.NoContent, response.status)

        assertFailsWith<IllegalArgumentException> {
            client.delete("/tickets/invalid")
        }
    }

    @Test
    fun `POST purchase tickets processes payment`() = testApplication {
        val testTickets = listOf(
            Ticket(1, 2, 3, status = TicketStatus.PURCHASED),
            Ticket(2, 2, 3, status = TicketStatus.PURCHASED)
        )

        coEvery { mockTicketService.purchaseTickets(1, "payment_123") } returns testTickets

        setupApplication()

        val client = createClient {
            install(ContentNegotiation) { json() }
        }

        val request = PurchaseTicketsRequest("payment_123")
        val response = client.post("/tickets/purchase/1") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val tickets = response.body<List<TicketResponse>>()
        assertEquals(2, tickets.size)
        assertEquals(TicketStatus.PURCHASED, tickets[0].status)

        assertFailsWith<IllegalArgumentException> {
            client.post("/tickets/purchase/invalid") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }

    private fun TestApplicationBuilder.setupApplication() {
        application {
            install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    serializersModule = SerializersModule {
                        contextual(Date::class, domain.Serializer)
                    }
                })
            }
            routing {
                ticketController.apply { registerRoutes() }
            }
        }
    }
}