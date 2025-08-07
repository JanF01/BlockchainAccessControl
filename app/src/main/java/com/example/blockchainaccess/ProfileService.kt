package com.example.blockchainaccess


object ProfileService {
    fun createProfile(username: String, password: String, repeatPassword: String): Result<String> {
        if (username.isBlank() || password.isBlank() || repeatPassword.isBlank()) {
            return Result.failure(Exception("All fields must be filled."))
        }

        if (password != repeatPassword) {
            return Result.failure(Exception("Passwords do not match."))
        }

        val id = generateRandomId(9)

        // Simulate success with generated ID
        return Result.success("Profile created. ID: $id")
    }

    private fun generateRandomId(length: Int): String {
        val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }
}