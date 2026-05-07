package com.solari.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.recentEmojiDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "solari_recent_emojis"
)

class RecentEmojiStore(context: Context) {
    private val dataStore = context.applicationContext.recentEmojiDataStore

    val recentEmojis: Flow<List<String>> = dataStore.data
        .catch { error ->
            if (error is IOException) {
                emit(emptyPreferences())
            } else {
                throw error
            }
        }
        .map { preferences ->
            preferences[RecentEmojisKey].toEmojiList()
        }

    suspend fun recordEmoji(emoji: String): List<String> {
        var updatedEmojis = emptyList<String>()
        dataStore.edit { preferences ->
            updatedEmojis = preferences[RecentEmojisKey]
                .toEmojiList()
                .withRecordedEmoji(emoji)
            preferences[RecentEmojisKey] = updatedEmojis.joinToString(SEPARATOR)
        }

        return updatedEmojis
    }

    suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.remove(RecentEmojisKey)
        }
    }

    fun mergeEmoji(currentEmojis: List<String>, emoji: String): List<String> {
        return currentEmojis.withRecordedEmoji(emoji)
    }

    private fun String?.toEmojiList(): List<String> {
        return this
            ?.split(SEPARATOR)
            ?.filter { it.isNotBlank() }
            .orEmpty()
    }

    private fun List<String>.withRecordedEmoji(emoji: String): List<String> {
        return buildList {
            add(emoji)
            this@withRecordedEmoji
                .asSequence()
                .filterNot { it == emoji }
                .take(MAX_RECENT_EMOJIS - 1)
                .forEach(::add)
        }
    }

    private companion object {
        val RecentEmojisKey = stringPreferencesKey("recent_emojis")
        const val SEPARATOR = "\u001F"
        const val MAX_RECENT_EMOJIS = 28
    }
}
