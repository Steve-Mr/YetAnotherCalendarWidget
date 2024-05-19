package com.maary.yetanothercalendarwidget

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.database.getIntOrNull
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Calendar
import javax.inject.Inject

class CalendarContentResolver @Inject constructor(@ApplicationContext val context: Context) {

    private val contentResolver = context.contentResolver

    fun getCalendars(): List<CalendarR> {
        // Check if the READ_CALENDAR permission is granted
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CALENDAR
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Handle permission denial
            return emptyList()
        }

        // Query the Calendars table
        val uri = CalendarContract.Calendars.CONTENT_URI
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME
        )

        val cursor = contentResolver.query(uri, projection, null, null, null)

        // Create a list of Calendar objects
        val calendars = mutableListOf<CalendarR>()
        cursor?.use {
            while (it.moveToNext()) {
                val id = it.getLongOrNull(it.getColumnIndex(CalendarContract.Calendars._ID))
                val displayName =
                    it.getStringOrNull(it.getColumnIndex(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME))
                calendars.add(CalendarR(id, displayName))
            }
        }

        return calendars
    }

    // Create a StateFlow to hold the list of events
    private val _threeDayEventsStateFlow = MutableStateFlow<List<Event>>(emptyList())

    // Expose the StateFlow as a public property
    val threeDayEventsStateFlow: StateFlow<List<Event>> = _threeDayEventsStateFlow.asStateFlow()

    fun getThreeEventsForCalendar() {
        // Check if the READ_CALENDAR permission is granted
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CALENDAR
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Handle permission denial
            _threeDayEventsStateFlow.value = emptyList()
            return
        }

        val preferences = PreferenceRepository(context)
        var calendarIdList: List<Long>

        runBlocking {
            calendarIdList = if (preferences.getCalendars().first()!=null) preferences.getCalendars().first()!! else emptyList()
        }

        val uri = CalendarContract.Events.CONTENT_URI
        val projection = arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND
        )

// Calculate start and end timestamps for yesterday, today, and tomorrow
        val today = Calendar.getInstance()
        val startOfToday = today.clone() as Calendar
        startOfToday.set(Calendar.HOUR_OF_DAY, 0)
        startOfToday.set(Calendar.MINUTE, 0)
        startOfToday.set(Calendar.SECOND, 0)

        val endOfToday = today.clone() as Calendar
        endOfToday.set(Calendar.HOUR_OF_DAY, 23)
        endOfToday.set(Calendar.MINUTE, 59)
        endOfToday.set(Calendar.SECOND, 59)

        val yesterday = today.clone() as Calendar
        yesterday.add(Calendar.DAY_OF_MONTH, -1)
        val startOfYesterday = yesterday.clone() as Calendar
        startOfYesterday.set(Calendar.HOUR_OF_DAY, 0)
        startOfYesterday.set(Calendar.MINUTE, 0)
        startOfYesterday.set(Calendar.SECOND, 0)

        val endOfYesterday = yesterday.clone() as Calendar
        endOfYesterday.set(Calendar.HOUR_OF_DAY, 23)
        endOfYesterday.set(Calendar.MINUTE, 59)
        endOfYesterday.set(Calendar.SECOND, 59)

        val tomorrow = today.clone() as Calendar
        tomorrow.add(Calendar.DAY_OF_MONTH, 1)
        val startOfTomorrow = tomorrow.clone() as Calendar
        startOfTomorrow.set(Calendar.HOUR_OF_DAY, 0)
        startOfTomorrow.set(Calendar.MINUTE, 0)
        startOfTomorrow.set(Calendar.SECOND, 0)

        val endOfTomorrow = tomorrow.clone() as Calendar
        endOfTomorrow.set(Calendar.HOUR_OF_DAY, 23)
        endOfTomorrow.set(Calendar.MINUTE, 59)
        endOfTomorrow.set(Calendar.SECOND, 59)

        Log.v("CAS", "$calendarIdList")

        val selection = "${CalendarContract.Events.CALENDAR_ID} = ? AND ${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTEND} <= ?"

        val events = mutableListOf<Event>()

        calendarIdList.forEach { calendarId ->
            val selectionArgs = arrayOf(calendarId.toString(), startOfYesterday.timeInMillis.toString(), endOfTomorrow.timeInMillis.toString())

            val cursor = contentResolver.query(uri, projection, selection, selectionArgs, null)

            cursor?.use {
                while (it.moveToNext()) {
                    val id = it.getLongOrNull(it.getColumnIndex(CalendarContract.Events._ID))
                    val title = it.getStringOrNull(it.getColumnIndex(CalendarContract.Events.TITLE))
                    val dtstart = it.getLongOrNull(it.getColumnIndex(CalendarContract.Events.DTSTART))
                    val dtend = it.getLongOrNull(it.getColumnIndex(CalendarContract.Events.DTEND))
                    val allDay = it.getIntOrNull(it.getColumnIndex(CalendarContract.Events.ALL_DAY)) == 1
                    events.add(Event(id, title, dtstart, dtend, allDay))
                    Log.v("CAS" , "$title, $allDay")
                }
            }
        }

        events.sortBy { it.dtstart }

        // Update the StateFlow with the list of events
        _threeDayEventsStateFlow.value = events
    }

    // Create a StateFlow to hold the list of events
    private val _weeklyEventsStateFlow = MutableStateFlow<List<Event>>(emptyList())

    // Expose the StateFlow as a public property
    val weeklyEventsStateFlow: StateFlow<List<Event>> = _weeklyEventsStateFlow.asStateFlow()

    fun getWeeklyEventsForCalendar() {
        // Check if the READ_CALENDAR permission is granted
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CALENDAR
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Handle permission denial
            _weeklyEventsStateFlow.value = emptyList()
            return
        }

        val preferences = PreferenceRepository(context)
        var calendarIdList: List<Long>?

        runBlocking {
            calendarIdList = if (preferences.getCalendars().first()!=null) preferences.getCalendars().first()!! else emptyList()
        }

        // Get the start and end dates for the current week
        val calendar = Calendar.getInstance()
        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val startOfWeek = calendar.clone() as Calendar
        startOfWeek.add(Calendar.DAY_OF_WEEK, -firstDayOfWeek + 1)
        startOfWeek.set(Calendar.HOUR_OF_DAY, 0)
        startOfWeek.set(Calendar.MINUTE, 0)
        startOfWeek.set(Calendar.SECOND, 0)
        startOfWeek.set(Calendar.MILLISECOND, 0)
        val endOfWeek = calendar.clone() as Calendar
        endOfWeek.add(Calendar.DAY_OF_WEEK, 7 - firstDayOfWeek)
        endOfWeek.set(Calendar.HOUR_OF_DAY, 23)
        endOfWeek.set(Calendar.MINUTE, 59)
        endOfWeek.set(Calendar.SECOND, 59)
        endOfWeek.set(Calendar.MILLISECOND, 999)

        // Query the content resolver for events in the current week
        val uri = CalendarContract.Events.CONTENT_URI
        val projection = arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DESCRIPTION,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND
        )
        val selection = "((${CalendarContract.Events.CALENDAR_ID} = ? AND ${CalendarContract.Events.DTSTART} >= ?) AND (${CalendarContract.Events.DTSTART} <= ?))"

        val events = mutableListOf<Event>()

        calendarIdList?.forEach { calendarId ->
            val selectionArgs = arrayOf(
                calendarId.toString(),
                startOfWeek.timeInMillis.toString(),
                endOfWeek.timeInMillis.toString()
            )
            val sortOrder = "${CalendarContract.Events.DTSTART} ASC"

            val cursor = context.contentResolver.query(
                uri,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )

            // Create a list of events from the cursor
            cursor?.use {
                while (it.moveToNext()) {
                    val id = it.getLong(it.getColumnIndexOrThrow(CalendarContract.Events._ID))
                    val title = it.getString(it.getColumnIndexOrThrow(CalendarContract.Events.TITLE))
                    val dtstart = it.getLong(it.getColumnIndexOrThrow(CalendarContract.Events.DTSTART))
                    val dtend = it.getLong(it.getColumnIndexOrThrow(CalendarContract.Events.DTEND))
                    val allDay = it.getIntOrNull(it.getColumnIndex(CalendarContract.Events.ALL_DAY) ) == 1
                    events.add(Event(id, title, dtstart, dtend, allDay))
                }
            }
        }

        events.sortBy { it.dtstart }

        _weeklyEventsStateFlow.value = events

    }


    data class CalendarR(val id: Long?, val displayName: String?)
    data class Event(val id: Long?, val title: String?, val dtstart: Long?, val dtend: Long?, val allDay: Boolean?)

    init {
        getThreeEventsForCalendar()
        getWeeklyEventsForCalendar()
    }
}