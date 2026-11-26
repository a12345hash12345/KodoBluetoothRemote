package com.kodo.bluetoothremote.capture

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.MediaCodec
import android.media.MediaFormat
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.IBinder
import android.util.Log
import android.view.Surface
import com.kodo.bluetoothremote.ble.BlePeripheral
import java.nio.ByteBuffer

class ProjectionService : Service() {
    companion object {
        const val ACTION_START = "com.kodo.bluetoothremote.START"
        const val EXTRA_RESULT_CODE = "result_code"
        const val EXTRA_DATA = "result_data"
        private const val CHANNEL = "kodo_screen_share"
        private const val NOTIFICATION_ID = 41
        private const val TAG = "KodoProjection"
    }

    private var projection: MediaProjection? = null
    private var encoder: MediaCodec? = null
    private var inputSurface: Surface? = null
    private var running = false
    private var frameSeq = 0

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_START) {
            val code = intent.getIntExtra(EXTRA_RESULT_CODE, Activity.RESULT_CANCELED)
            val data = intent.getParcelableExtra<Intent>(EXTRA_DATA)
            if (data != null) startCapture(code, data)
        }
        return START_NOT_STICKY
    }

    private fun startCapture(resultCode: Int, data: Intent) {
        createNotificationChannel()
        val notification = Notification.Builder(this, CHANNEL)
            .setContentTitle("Kodo Bluetooth Remote")
            .setContentText("Thanks for using my app - KodoTechnology")
            .setSmallIcon(android.R.drawable.presence_video_online)
            .setOngoing(true)
            .build()
        startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)

        try {
            val mgr = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            projection = mgr.getMediaProjection(resultCode, data)
            val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, 640, 480)
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfoCompat.COLOR_FORMAT_SURFACE)
            format.setInteger(MediaFormat.KEY_BIT_RATE, 450_000)
            format.setInteger(MediaFormat.KEY_FRAME_RATE, 15)
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2)
            encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
            encoder!!.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            inputSurface = encoder!!.createInputSurface()
            encoder!!.start()
            val metrics = resources.displayMetrics
            projection!!.createVirtualDisplay(
                "KodoRemote", 640, 480, metrics.densityDpi,
                android.hardware.display.DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                inputSurface, null, null
            )
            running = true
            Thread { drainEncoder() }.start()
        } catch (t: Throwable) {
            Log.e(TAG, "Unable to start projection", t)
            stopSelf()
        }
    }

    private fun drainEncoder() {
        val info = MediaCodec.BufferInfo()
        while (running) {
            val index = try { encoder?.dequeueOutputBuffer(info, 10_000) ?: -1 } catch (_: Exception) { -1 }
            if (index >= 0) {
                val buffer: ByteBuffer? = encoder?.getOutputBuffer(index)
                if (buffer != null && info.size > 0) {
                    val bytes = ByteArray(info.size)
                    buffer.position(info.offset)
                    buffer.limit(info.offset + info.size)
                    buffer.get(bytes)
                    BlePeripheral.sendVideoFrame(frameSeq++, bytes)
                }
                encoder?.releaseOutputBuffer(index, false)
            }
        }
    }

    private fun createNotificationChannel() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel(CHANNEL, "Screen sharing", NotificationManager.IMPORTANCE_LOW))
    }

    override fun onDestroy() {
        running = false
        try { encoder?.stop(); encoder?.release() } catch (_: Exception) {}
        inputSurface?.release()
        projection?.stop()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

private object MediaCodecInfoCompat {
    const val COLOR_FORMAT_SURFACE = 0x7F000789
}
