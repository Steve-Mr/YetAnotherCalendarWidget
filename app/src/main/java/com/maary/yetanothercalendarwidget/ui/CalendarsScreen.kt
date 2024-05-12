package com.maary.yetanothercalendarwidget.ui

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.maary.yetanothercalendarwidget.CalendarContentResolver
import com.maary.yetanothercalendarwidget.PreferenceRepository
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarsListScreen() {

    val context = LocalContext.current

    val calendarContentResolver = CalendarContentResolver(context)
    val viewModel: CalendarsViewModel = CalendarsViewModel(calendarContentResolver)
    val preferenceRepository: PreferenceRepository = PreferenceRepository(context)

    // check if the read calendar permission is granted
    // if not, request it
    // if granted, show the list of calendars
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {isGranted ->
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendars") }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = innerPadding,
        ) {
            items(calendars.value.size) { index ->
                CalendarItem(
                    calendars.value[index],
                    onItemClick = {
                        coroutineScope.launch {
                            runBlocking {
                                preferenceRepository.setCalendars(calendars.value[index].id.toString())
                            }
                            Log.v("CAS", calendars.value[index].id.toString())
                        }
                    }
                )
            }
        }
    }


}

@Composable
fun CalendarItem(
    calendar: CalendarContentResolver.CalendarR,
    onItemClick: () -> Unit
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() },
        headlineContent = {
            if (calendar.displayName != null)
                Text(calendar.displayName)
        },
    )
}


@Preview(showSystemUi = true)
@Composable
fun CalendarsListScreenPreview() {
    CalendarsListScreen()
}

//    @Preview
//    @Composable
//    fun CalendarItemPreview() {
//        CalendarItem("calendar")
//    }


