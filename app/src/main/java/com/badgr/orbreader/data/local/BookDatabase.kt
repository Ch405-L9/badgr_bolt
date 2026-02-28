package com.badgr.orbreader.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.room.AutoMigration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [BookEntity::class],
    version = 3,
    exportSchema = true,
    autoMigrations = [
        // AutoMigration(from = 2, to = 3)
    ]
)
abstract class BookDatabase : RoomDatabase() {

    abstract fun bookDao(): BookDao

    companion object {
        @Volatile private var INSTANCE: BookDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE books ADD COLUMN currentWordIndex INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        fun getInstance(context: Context): BookDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    BookDatabase::class.java,
                    "orbreader.db"
                )
                .addMigrations(MIGRATION_1_2)
                // If migration fails, delete the DB and start over (Dev Only!)
                .fallbackToDestructiveMigration() 
                .build()
                .also { INSTANCE = it }
            }
    }
}
