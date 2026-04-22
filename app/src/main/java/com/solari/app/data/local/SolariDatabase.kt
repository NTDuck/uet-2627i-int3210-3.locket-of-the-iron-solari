package com.solari.app.data.local

import androidx.room.Database
import androidx.room.migration.Migration
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.solari.app.data.local.auth.AuthSessionDao
import com.solari.app.data.local.auth.AuthSessionEntity

@Database(
    entities = [AuthSessionEntity::class],
    version = 2,
    exportSchema = true
)
abstract class SolariDatabase : RoomDatabase() {
    abstract fun authSessionDao(): AuthSessionDao

    companion object {
        val Migration1To2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE auth_sessions ADD COLUMN sign_in_method TEXT")
            }
        }
    }
}
