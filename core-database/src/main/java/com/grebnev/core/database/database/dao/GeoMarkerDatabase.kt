package com.grebnev.core.database.database.dao

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.grebnev.core.database.database.model.GeoMarkerDbModel
import com.grebnev.core.database.database.model.MetadataDbModel

@Database(
    entities = [GeoMarkerDbModel::class, MetadataDbModel::class],
    version = 1,
    exportSchema = false,
)
abstract class GeoMarkerDatabase : RoomDatabase() {
    abstract fun geoMarkerDao(): GeoMarkerDao

    abstract fun metadataDao(): MetadataDao

    companion object {
        private const val DATABASE_NAME = "geoMarkerDatabase"
        private var instance: GeoMarkerDatabase? = null
        private val LOCK = Any()

        fun getInstance(context: Context): GeoMarkerDatabase {
            instance?.let { return it }

            synchronized(LOCK) {
                instance?.let { return it }

                val database =
                    Room
                        .databaseBuilder(
                            context = context,
                            klass = GeoMarkerDatabase::class.java,
                            name = DATABASE_NAME,
                        ).build()

                instance = database
                return database
            }
        }
    }
}