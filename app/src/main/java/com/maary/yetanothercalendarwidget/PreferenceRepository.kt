package com.maary.yetanothercalendarwidget

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferenceRepository @Inject constructor(@ApplicationContext context: Context) {

    private val dataStore = context.dataStore

    companion object {
        val CALENDARS = stringPreferencesKey("calendars")
    }

    fun getCalendars(): Flow<List<Long>?> {
        return dataStore.data.map { preferences ->
            preferences[CALENDARS]
                ?.takeIf { it.isNotEmpty() } // Filter out empty strings
                ?.split(",")
                ?.mapNotNull { it.toLongOrNull() } // Handle invalid Long conversions
        }
    }


    suspend fun setCalendars(calendars: List<Long>) {
        dataStore.edit { preferences ->
            preferences[CALENDARS] = calendars.joinToString(separator = ",")
        }
    }


}