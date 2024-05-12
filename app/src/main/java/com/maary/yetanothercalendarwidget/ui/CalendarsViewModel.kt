package com.maary.yetanothercalendarwidget.ui

import androidx.lifecycle.ViewModel
import com.maary.yetanothercalendarwidget.CalendarContentResolver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CalendarsViewModel(private val calendarContentResolver: CalendarContentResolver) : ViewModel() {

    private val _calendars = MutableStateFlow<List<CalendarContentResolver.CalendarR>>(emptyList())
    val calendars: StateFlow<List<CalendarContentResolver.CalendarR>> = _calendars.asStateFlow()

    init {
        loadCalendars()
    }

    fun refreshCalendars() {
        loadCalendars()
    }

    private fun loadCalendars() {
        // Load the list of calendars from the calendarContentResolver
        val calendars = calendarContentResolver.getCalendars()

        // Update the StateFlow with the list of calendars
        _calendars.value = calendars
    }
}