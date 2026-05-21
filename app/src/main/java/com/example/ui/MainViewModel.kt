package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.Event
import com.example.data.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

enum class AppScreen {
    MONTH,
    WEEK_AGENDA,
    DAY,
    SETTINGS,
    NEW_EVENT
}

class MainViewModel(private val repository: EventRepository) : ViewModel() {
    // Current Active Screen
    private val _currentScreen = MutableStateFlow(AppScreen.MONTH)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Screen Stack for correct back button handling
    private val screenStack = mutableListOf<AppScreen>()

    fun navigateTo(screen: AppScreen) {
        if (screen != _currentScreen.value) {
            screenStack.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        if (screenStack.isNotEmpty()) {
            _currentScreen.value = screenStack.removeAt(screenStack.size - 1)
            return true
        }
        return false
    }

    // Selected Day in Calendar (defaults to Jan 8, 2024 for full UI mockup compliance)
    private val _selectedDate = MutableStateFlow<Calendar>(Calendar.getInstance().apply {
        set(Calendar.YEAR, 2024)
        set(Calendar.MONTH, Calendar.JANUARY)
        set(Calendar.DAY_OF_MONTH, 8)
    })
    val selectedDate: StateFlow<Calendar> = _selectedDate.asStateFlow()

    fun setSelectedDate(calendar: Calendar) {
        _selectedDate.value = calendar
    }

    // Settings flows
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()
    fun toggleDarkMode(value: Boolean) { _isDarkMode.value = value }

    private val _weekStart = MutableStateFlow("Monday")
    val weekStart: StateFlow<String> = _weekStart.asStateFlow()
    fun setWeekStart(start: String) { _weekStart.value = start }

    private val _eventAlerts = MutableStateFlow(true)
    val eventAlerts: StateFlow<Boolean> = _eventAlerts.asStateFlow()
    fun toggleEventAlerts(value: Boolean) { _eventAlerts.value = value }

    private val _hapticFeedback = MutableStateFlow(true)
    val hapticFeedback: StateFlow<Boolean> = _hapticFeedback.asStateFlow()
    fun toggleHapticFeedback(value: Boolean) { _hapticFeedback.value = value }

    // List of all events from Room database
    val allEvents: StateFlow<List<Event>> = repository.allEvents
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Pre-create standard events on startup if database is currently empty
        viewModelScope.launch {
            val currentEvents = repository.allEvents.first()
            if (currentEvents.isEmpty()) {
                populateDefaultEvents()
            }
        }
    }

    private suspend fun populateDefaultEvents() {
        val cal = Calendar.getInstance()

        // Jan 3: Event
        cal.set(2024, Calendar.JANUARY, 3, 10, 0)
        val t3S = cal.timeInMillis
        val t3E = cal.timeInMillis + 90 * 60 * 1000
        repository.insert(Event(
            title = "Design Kickoff Session",
            startTime = t3S,
            endTime = t3E,
            categoryColor = "#0058be",
            location = "Design Studio"
        ))

        // Jan 5: Marketing & Code Refactoring
        cal.set(2024, Calendar.JANUARY, 5, 13, 0)
        val t51S = cal.timeInMillis
        val t51E = cal.timeInMillis + 60 * 60 * 1000
        repository.insert(Event(
            title = "Marketing Strategy Review",
            startTime = t51S,
            endTime = t51E,
            categoryColor = "#b01c53"
        ))

        cal.set(2024, Calendar.JANUARY, 5, 15, 30)
        val t52S = cal.timeInMillis
        val t52E = cal.timeInMillis + 60 * 60 * 1000
        repository.insert(Event(
            title = "Code Refactoring Day",
            startTime = t52S,
            endTime = t52E,
            categoryColor = "#4854bb"
        ))

        // Jan 8 (Today):
        // Product Sync Meeting: 09:00 - 10:30 AM
        cal.set(2024, Calendar.JANUARY, 8, 9, 0)
        val t81S = cal.timeInMillis
        val t81E = cal.timeInMillis + 90 * 60 * 1000
        repository.insert(Event(
            title = "Product Sync Meeting",
            startTime = t81S,
            endTime = t81E,
            categoryColor = "#0058be",
            location = "Conference Room B",
            hasAttachment = false
        ))

        // Strategy Sync: 09:00 - 10:00 AM (Main Boardroom)
        cal.set(2024, Calendar.JANUARY, 8, 9, 0)
        val t82S = cal.timeInMillis
        val t82E = cal.timeInMillis + 60 * 60 * 1000
        repository.insert(Event(
            title = "Strategy Sync",
            startTime = t82S,
            endTime = t82E,
            categoryColor = "#0058be",
            location = "Main Boardroom"
        ))

        // Product Design Review: 12:30 PM (Zoom Meeting)
        cal.set(2024, Calendar.JANUARY, 8, 12, 30)
        val t83S = cal.timeInMillis
        val t83E = cal.timeInMillis + 60 * 60 * 1000
        repository.insert(Event(
            title = "Product Design Review",
            startTime = t83S,
            endTime = t83E,
            categoryColor = "#d1396b",
            location = "Zoom Meeting"
        ))

        // Dentist Appointment: 02:00 - 03:00 PM (City Dental Clinic)
        cal.set(2024, Calendar.JANUARY, 8, 14, 0)
        val t84S = cal.timeInMillis
        val t84E = cal.timeInMillis + 60 * 60 * 1000
        repository.insert(Event(
            title = "Dentist Appointment",
            startTime = t84S,
            endTime = t84E,
            categoryColor = "#b01c53",
            location = "City Dental Clinic"
        ))

        // Jan 9 (Tomorrow relative to Jan 8 sync card):
        // Weekly Planning: 10:15 - 11:15 AM
        cal.set(2024, Calendar.JANUARY, 9, 10, 15)
        val t9S = cal.timeInMillis
        val t9E = cal.timeInMillis + 60 * 60 * 1000
        repository.insert(Event(
            title = "Weekly Planning",
            startTime = t9S,
            endTime = t9E,
            categoryColor = "#8692fd",
            location = "Team Space"
        ))

        // Jan 11 (Client Presentation)
        cal.set(2024, Calendar.JANUARY, 11, 10, 0)
        val t11S = cal.timeInMillis
        val t11E = cal.timeInMillis + 120 * 60 * 1000
        repository.insert(Event(
            title = "Client Presentation",
            startTime = t11S,
            endTime = t11E,
            categoryColor = "#005ac3"
        ))

        // Jan 17 (Team Sync)
        cal.set(2024, Calendar.JANUARY, 17, 16, 0)
        val t17S = cal.timeInMillis
        val t17E = cal.timeInMillis + 60 * 60 * 1000
        repository.insert(Event(
            title = "Team Sync",
            startTime = t17S,
            endTime = t17E,
            categoryColor = "#4854bb"
        ))

        // Jan 25: Large Event Card "Tech Innovators Summit"
        cal.set(2024, Calendar.JANUARY, 25, 14, 0)
        val t25S = cal.timeInMillis
        val t25E = cal.timeInMillis + 180 * 60 * 1000
        repository.insert(Event(
            title = "Tech Innovators Summit",
            startTime = t25S,
            endTime = t25E,
            categoryColor = "#0058be",
            tag = "Conference",
            description = "Annual gathering for designers and developers to discuss future trends."
        ))
    }

    // Save interactive Event added via 'New Event' flow
    fun addEvent(
        title: String,
        isAllDay: Boolean,
        startTime: Long,
        endTime: Long,
        categoryColor: String,
        location: String,
        notes: String,
        reminderMinutes: Int,
        tag: String
    ) {
        viewModelScope.launch {
            repository.insert(
                Event(
                    title = title.ifBlank { "New Event" },
                    isAllDay = isAllDay,
                    startTime = startTime,
                    endTime = endTime,
                    categoryColor = categoryColor,
                    location = location,
                    description = notes,
                    reminderMinutes = reminderMinutes,
                    tag = tag
                )
            )
        }
    }
}

class ViewModelFactory(private val repository: EventRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
