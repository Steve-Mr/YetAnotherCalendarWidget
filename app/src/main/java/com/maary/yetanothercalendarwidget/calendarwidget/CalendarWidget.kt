package com.maary.yetanothercalendarwidget.calendarwidget

import android.content.Context
import android.icu.text.SimpleDateFormat
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

        Box(contentAlignment = Alignment.TopEnd) {
            if (isWeekView) {
                WeekView(events = weeklyEvents)
            } else {
                DayView(events = threeDayEvents)
            }

            Row(
                horizontalAlignment = Alignment.End,
                modifier = GlanceModifier
                    .fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
            ) {

                Image(modifier = GlanceModifier
                    .cornerRadius(16.dp)
                    .clickable {
                        calendarContentResolver.getThreeEventsForCalendar()
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
        val eventsByDay = events.groupBy {
            when (it.dtstart?.let { it1 -> getDayOfWeek(it1) }) {
                1 -> "MON"
                2 -> "TUE"
                3 -> "WED"
                4 -> "THU"
                5 -> "FRI"
                6 -> "SAT"
                7 -> "SUN"
                else -> "NONE"
            }
        }

        val widgetItemStates = listOf(
            WidgetItemState(
                eventsByDay["SUN"] ?: emptyList(),
                LocalContext.current.getString(R.string.sunday),
                GlanceTheme.colors.primaryContainer
            ),
            WidgetItemState(
                eventsByDay["MON"] ?: emptyList(),
                LocalContext.current.getString(R.string.monday),
                GlanceTheme.colors.primaryContainer
            ),
            WidgetItemState(
                eventsByDay["TUE"] ?: emptyList(),
                LocalContext.current.getString(R.string.tuesday),
                GlanceTheme.colors.secondaryContainer
            ),
            WidgetItemState(
                eventsByDay["WED"] ?: emptyList(),
                LocalContext.current.getString(R.string.wednesday),
                GlanceTheme.colors.tertiaryContainer
            ),
            WidgetItemState(
                eventsByDay["THU"] ?: emptyList(),
                LocalContext.current.getString(R.string.thursday),
                GlanceTheme.colors.primaryContainer
            ),
            WidgetItemState(
                eventsByDay["FRI"] ?: emptyList(),
                LocalContext.current.getString(R.string.friday),
                GlanceTheme.colors.secondaryContainer
            ),
            WidgetItemState(
                eventsByDay["SAT"] ?: emptyList(),
                LocalContext.current.getString(R.string.saturday),
                GlanceTheme.colors.tertiaryContainer
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
                GlanceTheme.colors.secondaryContainer
            ),
            WidgetItemState(
                todayEvents,
                LocalContext.current.getString(R.string.today),
                GlanceTheme.colors.primaryContainer
            ),
            WidgetItemState(
                tomorrowEvents,
                LocalContext.current.getString(R.string.tomorrow),
                GlanceTheme.colors.tertiaryContainer
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
        Column {
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
                style = TextStyle(GlanceTheme.colors.onSurface)
            )
            Column(modifier = GlanceModifier.padding(horizontal = 16.dp)) {

                val dtstart = formatMillisecondsToHhMm(event.dtstart)
                val dtend = formatMillisecondsToHhMm(event.dtend!!)

                Text(
                    text = if (event.allDay == false && dtend != dtstart) {
                        "$dtstart - $dtend"
                    } else LocalContext.current.getString(R.string.allday),
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontStyle = FontStyle.Italic,
                        fontSize = 12.sp
                    )
                )

                Text(
                    text = event.title.toString(),
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
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

    private fun getDayOfWeek(milliseconds: Long): Int {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = milliseconds
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

    data class WidgetItemState(
        val events: List<CalendarContentResolver.Event>,
        val tag: String,
        val background: ColorProvider
    )

}