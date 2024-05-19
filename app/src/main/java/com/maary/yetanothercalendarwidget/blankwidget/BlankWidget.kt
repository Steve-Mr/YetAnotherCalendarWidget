package com.maary.yetanothercalendarwidget.blankwidget

import android.content.Context
import androidx.annotation.Keep
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent

@Keep
class BlankWidget: GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
            }
        }
    }
}