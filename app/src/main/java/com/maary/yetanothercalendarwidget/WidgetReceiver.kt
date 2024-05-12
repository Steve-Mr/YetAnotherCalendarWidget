package com.maary.yetanothercalendarwidget

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.updateAll
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class WidgetReceiver: GlanceAppWidgetReceiver() {

    @Inject
    lateinit var calendarContentResolver: CalendarContentResolver

    override val glanceAppWidget: GlanceAppWidget
        get() = Widget()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        CoroutineScope(Dispatchers.IO).launch {
            Widget().updateAll(context) // This can be suspend if needed
            calendarContentResolver.getThreeEventsForCalendar()
            calendarContentResolver.getWeeklyEventsForCalendar()
        }
    }
}