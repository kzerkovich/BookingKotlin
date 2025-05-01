package api

import api.auth.UserPrincipal
import api.controllers.EventController
import api.dto.CreateEventRequest
import api.dto.EventResponse
import api.dto.UpdateEventRequest
import domain.Roles
import domain.entities.Event
import domain.services.EventService
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
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

class EventControllerTest {
    private val mockEventService = mockk<EventService>()
    private val eventController = EventController(mockEventService)

    @Test
    fun `POST events should create event`() = testApplication {
        val testEvent = createTestEvent()
        every { mockEventService.createEvent(any()) } returns testEvent

        val client = createClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    serializersModule = SerializersModule {
                        contextual(Date::class, domain.Serializer)
                    }
                })
            }
        }

        setupApplication()

        val request = CreateEventRequest(
            name = "Concert",
            description = "Big concert",
            date = Date().time + 100000,
            location = "Park",
            category = "Music",
            totalTickets = 100,
            price = 50.0
        )

        val response = client.post("/events") {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(request)
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val responseBody = response.body<EventResponse>()
        assertEquals(testEvent.id, responseBody.id)
        verify(exactly = 1) { mockEventService.createEvent(any()) }
    }

    @Test
    fun `POST events rejects invalid data`() = testApplication {
        val client = createClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    serializersModule = SerializersModule {
                        contextual(Date::class, domain.Serializer)
                    }
                })
            }
        }

        setupApplication()

        listOf(
            CreateEventRequest("", "desc", Date().time + 1000, "loc", "cat", 0, -1.0),
            CreateEventRequest("name", "desc", Date().time - 1000, "loc", "cat", 10, 10.0)
        ).forEach { badRequest ->
            assertFailsWith<ConstraintViolationException> {
                client.post("/events") {
                    contentType(ContentType.Application.Json)
                    accept(ContentType.Application.Json)
                    setBody(badRequest)
                }
            }
        }
    }

    @Test
    fun `GET all events returns list`() = testApplication {
        val testEvents = listOf(createTestEvent(), createTestEvent())
        every { mockEventService.getAllEvents() } returns testEvents

        val client = createClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    serializersModule = SerializersModule {
                        contextual(Date::class, domain.Serializer)
                    }
                })
            }
        }

        setupApplication()

        val response = client.get("/events") {
            header(HttpHeaders.Accept, ContentType.Application.Json)
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val events = response.body<List<EventResponse>>()
        assertEquals(2, events.size)
    }

    @Test
    fun `GET event by id returns correct event`() = testApplication {
        val testEvent = createTestEvent()
        every { mockEventService.getEvent(1) } returns testEvent

        val client = createClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    serializersModule = SerializersModule {
                        contextual(Date::class, domain.Serializer)
                    }
                })
            }
        }

        setupApplication()

        val response = client.get("/events/1") {
            header(HttpHeaders.Accept, ContentType.Application.Json)
        }

        assertEquals(testEvent.id, response.body<EventResponse>().id)

        assertFailsWith<IllegalArgumentException> {
            client.get("/events/invalid")
        }
    }

    @Test
    fun `PUT event updates existing event`() = testApplication {
        val testEvent = createTestEvent()
        every { mockEventService.getEvent(1) } returns testEvent
        every { mockEventService.updateEvent(any()) } returnsArgument 0

        val client = createClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    serializersModule = SerializersModule {
                        contextual(Date::class, domain.Serializer)
                    }
                })
            }
        }

        setupApplication()

        val updateRequest = UpdateEventRequest(
            name = "Updated Name",
            date = Date().time + 100000
        )

        val response = client.put("/events/1") {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(updateRequest)
        }

        assertEquals("Updated Name", response.body<EventResponse>().name)
        verify { mockEventService.updateEvent(any()) }
    }

    @Test
    fun `DELETE event removes it`() = testApplication {
        every { mockEventService.deleteEvent(1) } returns 1

        setupApplication()

        val response = client.delete("/events/1")
        assertEquals(HttpStatusCode.NoContent, response.status)
    }

    @Test
    fun `GET events by location returns filtered`() = testApplication {
        val testEvents = listOf(createTestEvent().copy(location = "Stadium"))
        every { mockEventService.getEventsByLocation("Stadium") } returns testEvents

        val client = createClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    serializersModule = SerializersModule {
                        contextual(Date::class, domain.Serializer)
                    }
                })
            }
        }

        setupApplication()

        val response = client.get("/events/location/Stadium") {
            header(HttpHeaders.Accept, ContentType.Application.Json)
        }

        val events = response.body<List<EventResponse>>()
        assertEquals(1, events.size)
        assertEquals("Stadium", events[0].location)
    }

    @Test
    fun `GET filtered events works with params`() = testApplication {
        val testEvents = listOf(createTestEvent())
        every { mockEventService.getFilteredEvents("Park", "Music", any(), any()) } returns testEvents

        setupApplication {
            install(Authentication) {
                basic("admin-auth") {
                    validate { UserPrincipal(domain.Roles.USER) }
                }
            }
        }

        val client = createClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    serializersModule = SerializersModule {
                        contextual(Date::class, domain.Serializer)
                    }
                })
            }
        }

        val response = client.get("/events/filter?location=Park&category=Music") {
            header(HttpHeaders.Accept, ContentType.Application.Json)
        }

        assertEquals(1, response.body<List<EventResponse>>().size)
    }

    @Test
    fun `Authenticated ticket update requires admin`() = testApplication {
        val testEvent = createTestEvent()
        coEvery { mockEventService.updateEventTickets(1, 200, any()) } returns testEvent

        setupApplication {
            install(Authentication) {
                basic("admin-auth") {
                    validate { credentials ->
                        if (credentials.name == "admin") {
                            UserPrincipal(domain.Roles.ADMIN)
                        } else {
                            null
                        }
                    }
                }
            }
        }

        val failedResponse = client.put("/events/1/tickets?total=200")
        assertEquals(HttpStatusCode.Unauthorized, failedResponse.status)

        val response = client.put("/events/1/tickets?total=200") {
            basicAuth("admin", "password")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        coVerify(exactly = 1) { mockEventService.updateEventTickets(1, 200, domain.Roles.ADMIN) }
    }

    @Test
    fun `Cancel event requires admin rights`() = testApplication {
        val mockEventService = mockk<EventService> {
            coEvery { cancelEvent(any(), any()) } returns 1
        }

        val eventController = EventController(mockEventService)

        application {
            install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
                json(Json {
                    serializersModule = SerializersModule {
                        contextual(Date::class, domain.Serializer)
                    }
                })
            }
            install(Authentication) {
                basic("admin-auth") {
                    realm = "Secure domain"
                    validate { credentials ->
                        if (credentials.name == "admin" && credentials.password == "password") {
                            UserPrincipal(Roles.ADMIN)
                        } else {
                            null
                        }
                    }
                }
            }
            routing {
                eventController.apply { registerRoutes() }
            }
        }

        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }

        client.post("/events/1/cancel").apply {
            assertEquals(HttpStatusCode.Unauthorized, status)
        }

        client.post("/events/1/cancel") {
            basicAuth("admin", "password")
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
        }

        coVerify(exactly = 1) {
            mockEventService.cancelEvent(
                eventId = eq(1),
                requesterRole = eq(Roles.ADMIN)
            )
        }
    }

    private fun TestApplicationBuilder.setupApplication(
        authConfig: Application.() -> Unit = {
            install(Authentication) {
                basic("admin-auth") {
                    validate { UserPrincipal(domain.Roles.USER) }
                }
            }
        }
    ) {
        application {
            install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    serializersModule = SerializersModule {
                        contextual(Date::class, domain.Serializer)
                    }
                })
            }
            authConfig()
            routing { eventController.apply { registerRoutes() } }
        }
    }

    private fun createTestEvent() = Event(
        id = 1,
        name = "Test Event",
        description = "Description",
        date = Date(),
        location = "Location",
        category = "Category",
        totalTickets = 100,
        availableTickets = 100,
        price = 50.0
    )
}