package com.maary.yetanothercalendarwidget

import android.content.Context
import android.icu.text.SimpleDateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.layout.wrapContentWidth
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import java.util.Calendar
import java.util.Date
import java.util.Locale

class Widget : GlanceAppWidget() {
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

        val threeDayEvents = calendarContentResolver.threeDayEventsStateFlow.collectAsState().value

        Column {
            Row (horizontalAlignment = Alignment.End,
                modifier = GlanceModifier
                    .fillMaxWidth().padding(8.dp)){

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
            }
            Spacer(modifier = GlanceModifier.height(8.dp))

                if (isWeekView) {
                    WeekView()
                } else {
                    DayView(
                        modifier = GlanceModifier.background(GlanceTheme.colors.background),
                        events = threeDayEvents
                    )
                }


        }
    }

    @Composable
    private fun WeekView() {
        LazyColumn {
        }
    }

    @Composable
    private fun WeekItem() {

    }

    @Composable
    private fun DayView(
        modifier: GlanceModifier,
        events: List<CalendarContentResolver.Event>
    ) {
        val yesterdayEvents = events.filter {
            it.dtstart?.let { dtstart -> isEventToday(dtstart) } == 1 // 0
        }

        val todayEvents = events.filter {
            it.dtstart?.let { dtstart -> isEventToday(dtstart) } == 1
        }

        val tomorrowEvents = events.filter {
            it.dtstart?.let { dtstart -> isEventToday(dtstart) } == 1 // 2
        }


        LazyColumn(modifier = GlanceModifier.padding(8.dp)) {

            item {
                DayRow(events = yesterdayEvents,
                    tag = "Yesterday",
                    background = GlanceTheme.colors.secondaryContainer)
            }

            item {
                DayRow(events = todayEvents,
                    tag = "Today",
                    background = GlanceTheme.colors.primaryContainer)
            }

            item {
                DayRow(events = tomorrowEvents,
                    tag = "Tomorrow",
                    background = GlanceTheme.colors.tertiaryContainer)
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
                    .background(background)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                events.forEach {event ->
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
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(formatDateFromMilliseconds(event.dtstart!!))
            Text(
                modifier = GlanceModifier.padding(horizontal = 16.dp),
                text = event.title.toString()
            )
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
                .padding(horizontal = 8.dp, vertical = 8.dp)
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

    private fun formatDateFromMilliseconds(milliseconds: Long): String {
        val dateFormat = SimpleDateFormat("MM-dd", Locale.getDefault())
        val date = Date(milliseconds)
        return dateFormat.format(date)
    }


}