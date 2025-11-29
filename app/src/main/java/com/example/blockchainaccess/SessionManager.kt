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
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.*

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import android.util.Log

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")
object SessionManager {

    private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    private val USER_ID = stringPreferencesKey("user_id")
    private val USERNAME = stringPreferencesKey("username")

    private val PORT = stringPreferencesKey("port")
    private val WHITELIST = stringSetPreferencesKey("whitelist")

    val LOGS = stringPreferencesKey("logs")

    suspend fun saveSession(context: Context, userId: String, username: String, port: String) {
        context.dataStore.edit { prefs ->
            prefs[IS_LOGGED_IN] = true
            prefs[USER_ID] = userId
            prefs[PORT] = port
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

    suspend fun addToWhitelist(context: Context, userId: String, port: String) {

        val result = NetworkClient.addWhitelistEntry(userId, port)

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

    suspend fun removeFromWhitelist(context: Context, userId: String, port: String) {

        val result = NetworkClient.removeWhitelistEntry(userId, port)

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

    suspend fun saveLogBlocks(context: Context, blocks: List<NetworkClient.ChainBlock>) {
        try {
            context.dataStore.edit { prefs ->
                prefs.remove(LOGS)
                val updatedJson = NetworkClient.json.encodeToString(
                    ListSerializer(PolymorphicSerializer(NetworkClient.ChainBlock::class)),
                    blocks
                )
                prefs[LOGS] = updatedJson
            }
            Log.d("CHAIN", "Saved ${blocks.size} blocks")
        } catch (e: Exception) {
            Log.e("CHAIN", "Failed to save blocks", e)
        }
    }

    fun logsFlow(context: Context): Flow<List<NetworkClient.ChainBlock>> {
        return context.dataStore.data.map { prefs ->
            prefs[LOGS]?.let {
                try {
                    NetworkClient.json.decodeFromString(
                        ListSerializer(PolymorphicSerializer(NetworkClient.ChainBlock::class)),
                        it
                    )
                } catch (e: Exception) {
                    Log.e("CHAIN", "Failed to decode saved blocks", e)
                    emptyList()
                }
            } ?: emptyList()
        }
    }

    fun getPort(context: Context): Flow<String> {
        return context.dataStore.data.map { prefs ->
            prefs[PORT] ?: ""
        }
    }
    fun getWhitelist(context: Context): Flow<Set<String>> {
        return context.dataStore.data.map { prefs ->
            prefs[WHITELIST] ?: emptySet()
        }
    }
}
