package com.kodo.bluetoothremote.ble

import android.content.Context
import android.content.Intent
import android.util.Log
import com.kodo.bluetoothremote.capture.ProjectionService
import com.kodo.bluetoothremote.control.RemoteAccessibilityService
import org.json.JSONObject

object ControlDispatcher {
    fun handle(context: Context, bytes: ByteArray) {
        try {
            val json = JSONObject(String(bytes, Charsets.UTF_8))
            when (json.optString("type")) {
                "stop" -> context.stopService(Intent(context, ProjectionService::class.java))
                "key" -> RemoteAccessibilityService.instance?.sendKey(json.optInt("code"), json.optBoolean("down"))
                "tap" -> RemoteAccessibilityService.instance?.tap(json.optDouble("x").toFloat(), json.optDouble("y").toFloat())
                "swipe" -> RemoteAccessibilityService.instance?.swipe(
                    json.optDouble("x1").toFloat(), json.optDouble("y1").toFloat(),
                    json.optDouble("x2").toFloat(), json.optDouble("y2").toFloat(),
                    json.optLong("duration", 250)
                )
            }
        } catch (e: Exception) { Log.e("KodoControl", "Invalid control packet", e) }
    }
}
