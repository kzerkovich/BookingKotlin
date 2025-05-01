package api.controllers

import domain.entities.Event
import domain.services.EventService
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.valiktor.validate
import org.valiktor.functions.*
import java.util.*

class EventController(private val eventService: EventService) {

}