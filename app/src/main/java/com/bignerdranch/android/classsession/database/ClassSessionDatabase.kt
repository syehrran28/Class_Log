package com.bignerdranch.android.classsession.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bignerdranch.android.classsession.ClassSession

@Database(entities = [ClassSession::class], version = 4, exportSchema = false)
@TypeConverters(ClassSessionTypeConverters::class)
abstract class ClassSessionDatabase : RoomDatabase() {
    abstract fun classSessionDao(): ClassSessionDao
}

val migration_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE ClassSession ADD COLUMN student TEXT NOT NULL DEFAULT ''"
        )
    }
}
