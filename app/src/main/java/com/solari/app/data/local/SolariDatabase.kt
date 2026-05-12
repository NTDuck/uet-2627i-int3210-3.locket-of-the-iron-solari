package com.solari.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.solari.app.data.local.auth.AuthSessionDao
import com.solari.app.data.local.auth.AuthSessionEntity

@Database(
    entities = [AuthSessionEntity::class],
    version = 1,
    exportSchema = true
)
abstract class SolariDatabase : RoomDatabase() {
    abstract fun authSessionDao(): AuthSessionDao
}
