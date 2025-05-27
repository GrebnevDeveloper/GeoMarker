package com.grebnev.core.database.di

import android.content.Context
import com.grebnev.core.database.database.dao.GeoMarkerDao
import com.grebnev.core.database.database.dao.GeoMarkerDatabase
import com.grebnev.core.database.repository.GeoMarkerRepository
import com.grebnev.core.database.repository.GeoMarkerRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): GeoMarkerDatabase = GeoMarkerDatabase.getInstance(context)

    @Provides
    @Singleton
    fun provideMarkerDao(database: GeoMarkerDatabase): GeoMarkerDao = database.geoMarkerDao()

    @Provides
    fun provideGeoMarkerRepository(impl: GeoMarkerRepositoryImpl): GeoMarkerRepository = impl
}