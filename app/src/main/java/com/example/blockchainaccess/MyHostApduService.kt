package com.example.blockchainaccess

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.content.Intent
import android.content.Context
import android.app.Service
import android.util.Log
import org.json.JSONObject

class MyHostApduService : HostApduService() {


    private var currentUserId: String = "unknown-user"
    private val roomName: String = "roomA"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.getStringExtra("NFC_USER_ID")?.let {
            currentUserId = it
            Log.d("HCE", "Updated userId to `$currentUserId`")
        }
        return Service.START_STICKY
    }

    override fun processCommandApdu(commandApdu: ByteArray, extras: Bundle?): ByteArray {
        Log.d("HCE", "APDU Received: ${commandApdu.joinToString(" ") { "%02X".format(it) }}")

        if (commandApdu.size >= 4 && commandApdu[1] == 0xA4.toByte()) {
            try {
                // 3. Create a JSON object and add the key-value pai
                val jsonData = JSONObject()
                jsonData.put("id", currentUserId)
                jsonData.put("room", roomName)

                // 4. Convert the JSON object to a UTF-8 byte array
                val responseData = jsonData.toString().toByteArray(Charsets.UTF_8)

                // 5. Return the JSON data followed by the success status word (90 00)
                return responseData + byteArrayOf(0x90.toByte(), 0x00.toByte())

            } catch (e: Exception) {
                Log.e("HCE", "Error creating JSON data", e)
                return byteArrayOf(0x6F.toByte(), 0x00.toByte()) // General error
            }
        }
        return byteArrayOf(0x6A.toByte(), 0x82.toByte()) // File not found
    }

    override fun onDeactivated(reason: Int) {
        Log.d("HCE", "Deactivated with reason $reason")
    }
}