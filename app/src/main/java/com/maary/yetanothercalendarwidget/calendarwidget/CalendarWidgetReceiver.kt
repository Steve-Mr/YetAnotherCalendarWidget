package com.maary.yetanothercalendarwidget.calendarwidget

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.maary.yetanothercalendarwidget.CalendarContentResolver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CalendarWidgetReceiver: GlanceAppWidgetReceiver() {

    @Inject
    lateinit var calendarContentResolver: CalendarContentResolver

    override val glanceAppWidget: GlanceAppWidget
        get() = CalendarWidget()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        CoroutineScope(Dispatchers.IO).launch {
//            Widget().updateAll(context) // This can be suspend if needed
            calendarContentResolver.getThreeEventsForCalendar()
            calendarContentResolver.getWeeklyEventsForCalendar()
            val manager = GlanceAppWidgetManager(context)
            val calendarWidget = CalendarWidget()
            val glanceIds = manager.getGlanceIds(calendarWidget.javaClass)
            glanceIds.forEach { glanceId ->
                calendarWidget.update(context, glanceId)
            }
        }


    }
}