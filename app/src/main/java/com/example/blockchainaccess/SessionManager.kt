package com.example.blockchainaccess

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")
object SessionManager {

    private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    private val USER_ID = stringPreferencesKey("user_id")
    private val USERNAME = stringPreferencesKey("username")

    private val WHITELIST = stringSetPreferencesKey("whitelist")

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

    suspend fun addToWhitelist(context: Context, userId: String) {

        val result = NetworkClient.addWhitelistEntry(userId)

        result.fold(
            onSuccess = {
                context.dataStore.edit { prefs ->
                    val currentWhitelist = prefs[WHITELIST] ?: emptySet()
                    prefs[WHITELIST] = currentWhitelist + userId
                }
            },
            onFailure = { error ->
                println("Error adding to whitelist: ${error.message}")
            }
        )
    }

    suspend fun removeFromWhitelist(context: Context, userId: String) {

        val result = NetworkClient.removeWhitelistEntry(userId)

        result.fold(
            onSuccess = {
                context.dataStore.edit { prefs ->
                    val currentWhitelist = prefs[WHITELIST] ?: emptySet()
                    prefs[WHITELIST] = currentWhitelist - userId
                }
            },
            onFailure = { error ->
                println("Error removing from whitelist: ${error.message}")
            }
        )
    }

    suspend fun saveWhitelist(context: Context, whitelist: Set<String>) {
        context.dataStore.edit { prefs ->
            prefs[WHITELIST] = whitelist
        }
    }


    fun getWhitelist(context: Context): Flow<Set<String>> {
        return context.dataStore.data.map { prefs ->
            prefs[WHITELIST] ?: emptySet()
        }
    }
}
