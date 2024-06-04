package com.maary.yetanothercalendarwidget.calendarwidget

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.util.Log
import androidx.annotation.Keep
 import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.layout.wrapContentWidth
import androidx.glance.text.FontStyle
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.maary.yetanothercalendarwidget.CalendarContentResolver
import com.maary.yetanothercalendarwidget.MainActivity
import com.maary.yetanothercalendarwidget.R
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Keep
class CalendarWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                Content()
            }
        }
    }

    @Composable
    private fun Content() {
        var isWeekView by remember { mutableStateOf(false) }
        val context = LocalContext.current
        val calendarContentResolver = CalendarContentResolver(context)

        val weeklyEvents = calendarContentResolver.weeklyEventsStateFlow.collectAsState().value
        val threeDayEvents = calendarContentResolver.threeDayEventsStateFlow.collectAsState().value

        Box(modifier = GlanceModifier.fillMaxSize(), //.background(GlanceTheme.colors.widgetBackground),
            contentAlignment = Alignment.TopEnd) {
            if (isWeekView) {
                WeekView(events = weeklyEvents)
            } else {
                DayView(events = threeDayEvents)
            }

            Row(
                horizontalAlignment = Alignment.End,
                modifier = GlanceModifier
                    .fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            ) {

                Image(modifier = GlanceModifier
                    .cornerRadius(16.dp)
                    .clickable {
                        if (isWeekView) {
                            calendarContentResolver.getWeeklyEventsForCalendar()
                        } else {
                            calendarContentResolver.getThreeEventsForCalendar()
                        }
                    }
                    .background(GlanceTheme.colors.inversePrimary)
                    .padding(4.dp),
                    provider = ImageProvider(R.drawable.ic_refresh),
                    contentDescription = "refresh")

                Spacer(modifier = GlanceModifier.width(8.dp))

                Image(modifier = GlanceModifier
                    .cornerRadius(16.dp)
                    .clickable {
                        isWeekView = !isWeekView
                    }
                    .background(GlanceTheme.colors.inversePrimary)
                    .padding(4.dp),
                    provider = ImageProvider(
                        if (isWeekView) R.drawable.ic_day else R.drawable.ic_week
                    ),
                    contentDescription = "change")

                Spacer(modifier = GlanceModifier.width(8.dp))

                Image(
                    modifier = GlanceModifier
                        .cornerRadius(16.dp)
                        .clickable(
                            actionStartActivity(MainActivity::class.java)
                        )
                        .background(GlanceTheme.colors.inversePrimary)
                        .padding(4.dp),
                    provider = ImageProvider(R.drawable.ic_settings),
                    contentDescription = "change"
                )

            }


        }
    }

    @Composable
    private fun WeekView(
        events: List<CalendarContentResolver.Event>
    ) {

        val eventsByDay = events.groupBy { event ->
            val dayOfYear = getDayOfYear(event.dtstart!!)
            dayOfYear
        }

        val (minDtstart, maxDtstart) = findEarliestAndLatestDates(events)

        val widgetItemStates = mutableListOf<WidgetItemState>()

        for (day in getDayOfYear(minDtstart!!) .. getDayOfYear(maxDtstart!!)) {
            val dayOfWeek = getDayOfWeek(day)
            Log.v("YACW-DAY", "$day, $dayOfWeek")
            widgetItemStates.add(
                WidgetItemState(
                    eventsByDay[day] ?: emptyList(),
                    LocalContext.current.getString(getDayOfWeekResource(dayOfWeek)), // Use a helper function
                    getBackgroundColor(dayOfWeek)  // Another helper function
                )
            )
        }

        Log.v("YACW", widgetItemStates.size.toString())

        LazyColumn(modifier = GlanceModifier.padding(8.dp)) {
            items(widgetItemStates) { widgetItemState ->
                DayRow(
                    events = widgetItemState.events,
                    tag = widgetItemState.tag,
                    background = widgetItemState.background
                )
            }
        }
    }

    @Composable
    private fun DayView(
        events: List<CalendarContentResolver.Event>
    ) {
        val yesterdayEvents = events.filter {
            it.dtstart?.let { dtstart -> isEventToday(dtstart) } == 0
        }

        val todayEvents = events.filter {
            it.dtstart?.let { dtstart -> isEventToday(dtstart) } == 1
        }

        val tomorrowEvents = events.filter {
            it.dtstart?.let { dtstart -> isEventToday(dtstart) } == 2
        }

        val widgetItemStates = listOf(
            WidgetItemState(
                yesterdayEvents,
                LocalContext.current.getString(R.string.yesterday),
                GlanceTheme.colors.secondary
            ),
            WidgetItemState(
                todayEvents,
                LocalContext.current.getString(R.string.today),
                GlanceTheme.colors.primary
            ),
            WidgetItemState(
                tomorrowEvents,
                LocalContext.current.getString(R.string.tomorrow),
                GlanceTheme.colors.tertiary
            )
        )

        LazyColumn(modifier = GlanceModifier.padding(8.dp)) {
            items(widgetItemStates) { widgetItemState ->
                DayRow(
                    events = widgetItemState.events,
                    tag = widgetItemState.tag,
                    background = widgetItemState.background
                )
            }

        }


    }

    @Composable
    private fun DayRow(
        events: List<CalendarContentResolver.Event>,
        tag: String,
        background: ColorProvider
    ) {
        Column (modifier = GlanceModifier.padding(4.dp)){
            DayTag(tag = tag)

            Spacer(GlanceModifier.height(8.dp).padding(8.dp))

            Column(
                modifier = GlanceModifier
                    .cornerRadius(16.dp)
                    .background(background),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                events.forEach { event ->
                    DayItem(event)
                }
            }
            Spacer(GlanceModifier.height(8.dp).padding(8.dp))
        }

    }

    @Composable
    private fun DayItem(event: CalendarContentResolver.Event) {
        Row(
            modifier = GlanceModifier
                .clickable(
                    actionRunCallback<OpenEventAction>(
                        parameters = actionParametersOf(
                            ActionParameters.Key<Long>("eventId") to (event.id ?: -1)
                        )
                    )
                )
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatDateFromMilliseconds(event.dtstart!!),
                style = TextStyle(GlanceTheme.colors.onPrimary)
            )
            Column(modifier = GlanceModifier.padding(horizontal = 16.dp)) {

                val dtstart = formatMillisecondsToHhMm(event.dtstart)
                val dtend = formatMillisecondsToHhMm(event.dtend!!)

                Text(
                    modifier = GlanceModifier.fillMaxWidth(),
                    text = if (event.allDay == false && dtend != dtstart) {
                        "$dtstart - $dtend"
                    } else LocalContext.current.getString(R.string.allday),
                    style = TextStyle(
                        color = GlanceTheme.colors.onPrimary,
                        fontStyle = FontStyle.Italic,
                        fontSize = 12.sp
                    )
                )

                Text(
                    text = event.title.toString(),
                    style = TextStyle(
                        color = GlanceTheme.colors.onPrimary,
                        fontSize = 16.sp
                    )
                )
            }
        }

    }

    @Composable
    private fun DayTag(tag: String) {
        Text(
            text = tag,
            style = TextStyle(color = GlanceTheme.colors.surface),
            modifier = GlanceModifier.background(GlanceTheme.colors.inverseSurface)
                .cornerRadius(8.dp)
                .wrapContentWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }

    private fun isEventToday(eventStartMillis: Long): Int {
        val currentTimeMillis = System.currentTimeMillis()

        val calendar = Calendar.getInstance().apply {
            timeInMillis = currentTimeMillis
        }

        val currentDay = calendar.get(Calendar.DAY_OF_YEAR)
        calendar.timeInMillis = eventStartMillis
        val eventDay = calendar.get(Calendar.DAY_OF_YEAR)

        return if (currentDay == eventDay) 1
        else if (currentDay < eventDay) 2
        else 0
    }


    private fun getDayOfYear(milliseconds: Long): Int {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = milliseconds
        return calendar.get(Calendar.DAY_OF_YEAR)
    }

    private fun getDayOfWeek(day: Int): Int {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_YEAR, day)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        return when (dayOfWeek) {
            Calendar.SUNDAY -> 7
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            else -> 0
        }
    }

    private fun formatDateFromMilliseconds(milliseconds: Long): String {
        val dateFormat = SimpleDateFormat("MM-dd", Locale.getDefault())
        val date = Date(milliseconds)
        return dateFormat.format(date)
    }

    private fun formatMillisecondsToHhMm(milliseconds: Long): String {
        val date = Date(milliseconds)
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        return formatter.format(date)
    }

    private fun getDayOfWeekResource(dayOfWeek: Int): Int {
        return when (dayOfWeek) {
            1 -> R.string.monday
            2 -> R.string.tuesday
            3 -> R.string.wednesday
            4 -> R.string.thursday
            5 -> R.string.friday
            6 -> R.string.saturday
            7 -> R.string.sunday
            else -> 0 // Handle unexpected input
        }
    }

    @Composable
    private fun getBackgroundColor(dayOfWeek: Int): ColorProvider {
        return when (dayOfWeek) {
            1, 4 -> GlanceTheme.colors.primary // Monday, Thursday
            2, 5 -> GlanceTheme.colors.secondary // Tuesday, Friday
            3, 6 -> GlanceTheme.colors.tertiary // Wednesday, Saturday
            7 -> GlanceTheme.colors.primary // Sunday (example, adjust as needed)
            else -> GlanceTheme.colors.background // Default for unexpected input
        }
    }

    private fun findEarliestAndLatestDates(events: List<CalendarContentResolver.Event>): Pair<Long?, Long?> {
        // Find the event with the earliest start date
        val earliestEvent = events.minByOrNull { it.dtstart ?: Long.MAX_VALUE }
        // Find the event with the latest end date
        val latestEvent = events.maxByOrNull { it.dtstart ?: Long.MIN_VALUE }

        // Extract the dates from the found events
        val earliestDate = earliestEvent?.dtstart
        val latestDate = latestEvent?.dtstart

        return Pair(earliestDate, latestDate)
    }

    data class WidgetItemState(
        val events: List<CalendarContentResolver.Event>,
        val tag: String,
        val background: ColorProvider
    )

}