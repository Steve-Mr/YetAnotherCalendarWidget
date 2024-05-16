package com.maary.yetanothercalendarwidget.calendarwidget

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback

class OpenEventAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val eventId = parameters[ActionParameters.Key<Long>("eventId")] ?: return
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = CalendarContract.Events.CONTENT_URI.buildUpon()
                .appendPath(eventId.toString())
                .build()
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}