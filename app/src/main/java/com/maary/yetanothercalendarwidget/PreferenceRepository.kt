package com.maary.yetanothercalendarwidget

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferenceRepository(context: Context) {

    private val dataStore = context.dataStore

    companion object {
        val CALENDARS = stringPreferencesKey("calendars")
    }

    fun getCalendars(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[CALENDARS]
        }
    }

    suspend fun setCalendars(calendars: String) {
        dataStore.edit { preferences ->
            preferences[CALENDARS] = calendars
        }
    }


}