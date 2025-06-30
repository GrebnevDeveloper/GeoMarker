package com.grebnev.core.location.di

import android.content.Context
import com.grebnev.core.location.data.LocationRepositoryImpl
import com.grebnev.core.location.domain.repository.LocationRepository
import com.grebnev.core.location.domain.usecase.GetCurrentLocationUseCase
import com.grebnev.core.location.domain.usecase.ManageLocationUpdatesUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocationModule {
    @Provides
    @Singleton
    fun provideLocationRepository(
        @ApplicationContext context: Context,
    ): LocationRepository = LocationRepositoryImpl(context)

    @Provides
    fun provideManageLocationUpdatesUseCase(repository: LocationRepository): ManageLocationUpdatesUseCase =
        ManageLocationUpdatesUseCase(repository)

    @Provides
    fun provideGetCurrentLocationUseCase(repository: LocationRepository): GetCurrentLocationUseCase =
        GetCurrentLocationUseCase(repository)
}