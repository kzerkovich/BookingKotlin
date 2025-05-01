package di

import api.controllers.*
import data.repositoryImpl.*
import domain.repository.*
import domain.services.*
import org.koin.dsl.module

val appModule = module {

    single<BookingRepository> { BookingRepositoryImpl() }
    single<EventRepository> { EventRepositoryImpl() }
    single<UsersRepository> { UsersRepositoryImpl() }
    single<TicketRepository> { TicketRepositoryImpl() }


    single { BookingController(get()) }
    single { EventController(get()) }
    single { UserController(get()) }
    single { TicketController(get()) }
}