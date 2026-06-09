package com.xiaosan.cleanmaster.trash

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.xiaosan.cleanmaster.core.model.Converters
import com.xiaosan.cleanmaster.core.model.TrashItem

@Database(entities = [TrashItem::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class TrashDatabase : RoomDatabase() {
    abstract fun trashDao(): TrashDao

    companion object {
        @Volatile
        private var INSTANCE: TrashDatabase? = null

        fun getInstance(context: Context): TrashDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TrashDatabase::class.java,
                    "trash_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
