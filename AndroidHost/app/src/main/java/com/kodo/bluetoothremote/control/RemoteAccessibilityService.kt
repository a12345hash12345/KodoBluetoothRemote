package com.kodo.bluetoothremote.control

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent

class RemoteAccessibilityService : AccessibilityService() {
    companion object { var instance: RemoteAccessibilityService? = null }
    override fun onServiceConnected() { instance = this }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}
    override fun onDestroy() { if (instance === this) instance = null; super.onDestroy() }

    fun tap(nx: Float, ny: Float) {
        val dm = resources.displayMetrics
        val path = Path().apply { moveTo(nx.coerceIn(0f,1f) * dm.widthPixels, ny.coerceIn(0f,1f) * dm.heightPixels) }
        val gesture = GestureDescription.Builder().addStroke(GestureDescription.StrokeDescription(path, 0, 60)).build()
        dispatchGesture(gesture, null, null)
    }

    fun swipe(x1: Float, y1: Float, x2: Float, y2: Float, duration: Long) {
        val dm = resources.displayMetrics
        val path = Path().apply {
            moveTo(x1.coerceIn(0f,1f) * dm.widthPixels, y1.coerceIn(0f,1f) * dm.heightPixels)
            lineTo(x2.coerceIn(0f,1f) * dm.widthPixels, y2.coerceIn(0f,1f) * dm.heightPixels)
        }
        val gesture = GestureDescription.Builder().addStroke(GestureDescription.StrokeDescription(path, 0, duration.coerceIn(50,2000))).build()
        dispatchGesture(gesture, null, null)
    }

    // AccessibilityService is not a general-purpose hardware keyboard injector.
    // Key events are intentionally not fabricated here; supported text/key actions need
    // a focused, user-approved input method or app-specific integration.
    fun sendKey(androidKeyCode: Int, down: Boolean) { /* reserved for supported key mapping */ }
}
