package com.maary.yetanothercalendarwidget.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maary.yetanothercalendarwidget.CalendarContentResolver
import com.maary.yetanothercalendarwidget.PreferenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class CalendarsViewModel @Inject constructor(
    private val calendarContentResolver: CalendarContentResolver,
    private val preferenceRepository: PreferenceRepository
) : ViewModel() {

    private val _calendars = MutableStateFlow<List<CalendarContentResolver.CalendarR>>(emptyList())
    val calendars: StateFlow<List<CalendarContentResolver.CalendarR>> = _calendars.asStateFlow()

    private val _selected = MutableStateFlow<List<Long>>(emptyList())
    val selected: StateFlow<List<Long>> = _selected.asStateFlow()

    init {
        loadCalendars()
        loadSelected()
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

    private fun loadSelected() {
        runBlocking {
            val selected = preferenceRepository.getCalendars().first()
            if (selected != null) {
                _selected.value = selected
            }
            Log.v("CAS", "load selected ${_selected.value}")
        }
    }

    fun selectCalendar(id: Long) {
        _selected.update {
            _selected.value.plus(id)
        }
    }

    fun finishSelection() {
        viewModelScope.launch {
            preferenceRepository.setCalendars(_selected.value)
            Log.v("CAS", "${_selected.value}")
        }
    }
}