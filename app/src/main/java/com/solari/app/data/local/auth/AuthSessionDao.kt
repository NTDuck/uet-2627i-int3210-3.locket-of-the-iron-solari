package com.solari.app.data.local.auth

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
abstract class AuthSessionDao {
    @Query("SELECT * FROM auth_sessions ORDER BY updated_at_epoch_millis DESC LIMIT 1")
    abstract fun observeCurrentSession(): Flow<AuthSessionEntity?>

    @Query("SELECT * FROM auth_sessions ORDER BY updated_at_epoch_millis DESC LIMIT 1")
    abstract suspend fun getCurrentSession(): AuthSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insert(session: AuthSessionEntity)

    @Query("DELETE FROM auth_sessions")
    abstract suspend fun clear()

    @Transaction
    open suspend fun replace(session: AuthSessionEntity) {
        clear()
        insert(session)
    }
}
