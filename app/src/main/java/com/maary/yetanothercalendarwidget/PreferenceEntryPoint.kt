package com.maary.yetanothercalendarwidget

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface PreferenceEntryPoint {
    fun preferenceRepository(): PreferenceRepository
}