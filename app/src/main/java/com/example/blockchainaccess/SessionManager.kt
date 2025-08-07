package com.example.blockchainaccess

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_session")

object SessionManager {

    private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    private val USER_ID = stringPreferencesKey("user_id")
    private val USERNAME = stringPreferencesKey("username")

    suspend fun saveSession(context: Context, userId: String, username: String) {
        context.dataStore.edit { prefs ->
            prefs[IS_LOGGED_IN] = true
            prefs[USER_ID] = userId
            prefs[USERNAME] = username
        }
    }

    suspend fun clearSession(context: Context) {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }

    suspend fun isLoggedIn(context: Context): Boolean {
        return context.dataStore.data.map { it[IS_LOGGED_IN] ?: false }.first()
    }

    suspend fun getUserId(context: Context): String? {
        return context.dataStore.data.map { it[USER_ID] }.first()
    }

    suspend fun getUsername(context: Context): String? {
        return context.dataStore.data.map { it[USERNAME] }.first()
    }
}
