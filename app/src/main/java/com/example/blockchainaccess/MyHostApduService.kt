package com.example.blockchainaccess

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.content.Intent
import android.content.Context
import android.app.Service
import android.util.Log

class MyHostApduService : HostApduService() {


    private var currentUserId: String = "unknown-user"

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
            val responseData = currentUserId.toByteArray(Charsets.UTF_8)
            return responseData + byteArrayOf(0x90.toByte(), 0x00.toByte())
        }
        return byteArrayOf(0x6A.toByte(), 0x82.toByte()) // File not found
    }

    override fun onDeactivated(reason: Int) {
        Log.d("HCE", "Deactivated with reason $reason")
    }
}