package com.grebnev.core.gallery

import android.content.Context
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