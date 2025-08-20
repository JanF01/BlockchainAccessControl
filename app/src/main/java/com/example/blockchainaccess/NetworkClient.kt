package com.example.blockchainaccess

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.* // Use CIO for a lightweight client
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


object NetworkClient {

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    @Serializable
    private data class CreateProfileRequest(
        val username: String,
        val password: String
    )

    @Serializable
    private data class CreateProfileResponse(
        val id: String,
        val whitelist: Set<String>
    )

    @Serializable
    private data class UpdateUserRequest(
        val id: String
    )

    @Serializable
    private data class WhitelistEntryRequest(
        val userId: String,
    )

    suspend fun addWhitelistEntry(userId: String, port: String): Result<Unit> {
        return try {
            val url = "http://192.168.0.198:$port/new_whitelist_entry"
            val response = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(WhitelistEntryRequest(userId))
            }

            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to add whitelist entry with status: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error while adding whitelist entry: ${e.message}"))
        }
    }

    suspend fun removeWhitelistEntry(userId: String, port: String): Result<Unit> {
        return try {
            val url = "http://192.168.0.198:$port/remove_whitelist_entry"
            val response = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(WhitelistEntryRequest(userId))
            }

            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to remove whitelist entry with status: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error while removing whitelist entry: ${e.message}"))
        }
    }

    suspend fun createAndRegisterProfile(username: String, password: String, port: String): Result<Pair<String, Set<String>>> {
        try {
            val createProfileUrl = "http://10.0.2.2:$port/login"
            val response: HttpResponse = client.post(createProfileUrl) {
                contentType(ContentType.Application.Json)
                setBody(CreateProfileRequest(username, password))
            }

            if (response.status != HttpStatusCode.OK) {
                return Result.failure(Exception("Profile creation failed with status: ${response.status}"))
            }

            val creationResponse = response.body<CreateProfileResponse>()

            if (creationResponse.id == "wrong") {
                return Result.failure(Exception("Server returned an error"))
            }

            val generatedId = creationResponse.id
            val whitelist = creationResponse.whitelist
            println("Profile created. ID: $generatedId")
            // --- Step 2: Register user ID with 127.0.0.1:8081 ---
            val registerUrl = "http://10.0.2.2:$port/app_save_id"
            val registerResponse: HttpResponse = client.post(registerUrl) {
                contentType(ContentType.Application.Json)
                setBody(UpdateUserRequest(generatedId))
            }

            if (registerResponse.status != HttpStatusCode.OK) {
                return Result.failure(Exception("User ID registration failed with status: ${registerResponse.status}"))
            }

            println("User ID $generatedId registered successfully.")
            return Result.success(Pair(generatedId, whitelist))

        } catch (e: Exception) {
            // Handle network or serialization errors
            return Result.failure(Exception("Network error: ${e.message}"))
        }
    }
}
