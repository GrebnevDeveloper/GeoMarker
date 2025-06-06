package com.grebnev.core.gallery.di

import android.content.Context
import com.grebnev.core.gallery.domain.repository.GalleryRepository
import com.grebnev.core.gallery.repository.GalleryRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GalleryModule {
    @Provides
    @Singleton
    fun provideGalleryRepository(
        @ApplicationContext context: Context,
    ): GalleryRepository = GalleryRepositoryImpl(context)
}