package com.maary.yetanothercalendarwidget.blankwidget

import androidx.annotation.Keep
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

@Keep
class BlankWidgetProvider : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget
        get() = BlankWidget()
}