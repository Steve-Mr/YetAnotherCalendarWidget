package com.maary.yetanothercalendarwidget.ui

import android.Manifest
import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.maary.yetanothercalendarwidget.CalendarContentResolver
import com.maary.yetanothercalendarwidget.R
import com.maary.yetanothercalendarwidget.calendarwidget.CalendarWidget
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarsListScreen(appWidgetId: Int) {

    val context = LocalContext.current

    val viewModel: CalendarsViewModel = viewModel()

    // check if the read calendar permission is granted
    // if not, request it
    // if granted, show the list of calendars
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // permission granted
            viewModel.refreshCalendars()

        }
    }

    LaunchedEffect(Unit) {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CALENDAR
            ) -> {
                // permission granted
                viewModel.refreshCalendars()
            }

            else -> {
                launcher.launch(Manifest.permission.READ_CALENDAR)
            }
        }
    }

    val calendars = viewModel.calendars.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val selected = viewModel.selected.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.calendars)) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                viewModel.finishSelection()
                coroutineScope.launch {
                    val manager = GlanceAppWidgetManager(context)
                    val widget = CalendarWidget()
                    val glanceIds = manager.getGlanceIds(widget.javaClass)
                    glanceIds.forEach { glanceId ->
                        widget.update(context, glanceId)
                    }
                }
                val resultIntent = Intent().apply {
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                }
                (context as? Activity)?.setResult(Activity.RESULT_OK, resultIntent)
                (context as? Activity)?.finish()
            }) {
                Icon(painter = painterResource(id = R.drawable.ic_done), contentDescription = null)
            }
        }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = innerPadding,
        ) {
            items(calendars.value.size) { index ->
                CalendarItem(
                    selected.value.contains(calendars.value[index].id),
                    calendars.value[index],
                    onItemClick = {
                        coroutineScope.launch {
                            calendars.value[index].id?.let { viewModel.selectCalendar(it) }
                        }
                    }
                )
            }
        }
    }


}

@Composable
fun CalendarItem(
    selected: Boolean,
    calendar: CalendarContentResolver.CalendarR,
    onItemClick: () -> Unit
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() },
        leadingContent = { Checkbox(checked = selected, onCheckedChange = null) },
        headlineContent = {
            if (calendar.displayName != null)
                Text(calendar.displayName)
        },
    )
}