package com.example.data

import kotlinx.coroutines.flow.Flow

class EventRepository(private val eventDao: EventDao) {
    val allEvents: Flow<List<Event>> = eventDao.getAllEvents()

    suspend fun insert(event: Event): Long {
        return eventDao.insertEvent(event)
    }

    suspend fun delete(event: Event) {
        eventDao.deleteEvent(event)
    }

    suspend fun deleteById(id: Long) {
        eventDao.deleteById(id)
    }

    suspend fun clearAll() {
        eventDao.clearAllEvents()
    }
}
