package com.kodo.bluetoothremote

import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.provider.Settings
import android.content.ComponentName
import android.content.Context
import android.text.TextUtils
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.kodo.bluetoothremote.capture.ProjectionService
import com.kodo.bluetoothremote.ble.BlePeripheral

class MainActivity : ComponentActivity() {
    private var sharing by mutableStateOf(false)
    private val projectionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val intent = Intent(this, ProjectionService::class.java).apply {
                action = ProjectionService.ACTION_START
                putExtra(ProjectionService.EXTRA_RESULT_CODE, result.resultCode)
                putExtra(ProjectionService.EXTRA_DATA, result.data)
            }
            ContextCompat.startForegroundService(this, intent)
            sharing = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BlePeripheral.start(this)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(
                primary = Color(0xFF7C9CFF),
                background = Color(0xFF0B1020),
                surface = Color(0xFF151D31)
            )) {
                Surface(Modifier.fillMaxSize(), color = Color(0xFF0B1020)) {
                    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                        Text("Kodo Bluetooth Remote", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                        Text("REDMI 8A DUAL  •  ANDROID 10", color = Color(0xFF9BA9C7))
                        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF151D31))) {
                            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(if (sharing) "Screen sharing is active" else "Ready to share", color = Color.White)
                                Text("BLE peripheral: ${if (BlePeripheral.isAdvertising) "Advertising" else "Starting"}", color = Color(0xFF9BA9C7))
                                Button(
                                    onClick = {
                                        val mgr = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                                        projectionLauncher.launch(mgr.createScreenCaptureIntent())
                                    },
                                    enabled = !sharing,
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text("Start screen sharing") }
                                OutlinedButton(
                                    onClick = { stopService(Intent(this@MainActivity, ProjectionService::class.java)); sharing = false },
                                    enabled = sharing,
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text("Stop sharing") }
                            }
                        }
                        OutlinedButton(
                            onClick = { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Enable remote keyboard / touch") }
                        Text("Keep this app's sharing session active while connecting from iPad. Android displays an ongoing notification during capture.", color = Color(0xFF9BA9C7))
                        Spacer(Modifier.weight(1f))
                        Text("KodoTechnology", color = Color(0xFF7C9CFF))
                    }
                }
            }
        }
    }
}
