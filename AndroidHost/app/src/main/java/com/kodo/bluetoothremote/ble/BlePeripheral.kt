package com.kodo.bluetoothremote.ble

import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import java.nio.ByteBuffer
import java.util.UUID

object BlePeripheral {
    val SERVICE_UUID: UUID = UUID.fromString("7E570001-6A2B-4A12-9B7A-1D4C0D0A0001")
    val VIDEO_UUID: UUID = UUID.fromString("7E570002-6A2B-4A12-9B7A-1D4C0D0A0001")
    val CONTROL_UUID: UUID = UUID.fromString("7E570003-6A2B-4A12-9B7A-1D4C0D0A0001")
    val STATUS_UUID: UUID = UUID.fromString("7E570004-6A2B-4A12-9B7A-1D4C0D0A0001")
    private var gattServer: BluetoothGattServer? = null
    private var advertiser: BluetoothLeAdvertiser? = null
    private var connected: BluetoothDevice? = null
    private var videoCharacteristic: BluetoothGattCharacteristic? = null
    @Volatile var isAdvertising = false
        private set

    fun start(context: Context) {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = manager.adapter ?: return
        if (!adapter.isEnabled) return
        gattServer = manager.openGattServer(context, object : BluetoothGattServerCallback() {
            override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
                connected = if (newState == BluetoothProfile.STATE_CONNECTED) device else null
            }
            override fun onCharacteristicWriteRequest(
                device: BluetoothDevice, requestId: Int, characteristic: BluetoothGattCharacteristic,
                preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray
            ) {
                if (characteristic.uuid == CONTROL_UUID) {
                    ControlDispatcher.handle(context, value)
                }
                if (responseNeeded) gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)
            }
            override fun onDescriptorWriteRequest(
                device: BluetoothDevice, requestId: Int, descriptor: BluetoothGattDescriptor,
                preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray
            ) {
                descriptor.value = value
                if (responseNeeded) gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)
            }
        })
        val service = BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
        videoCharacteristic = BluetoothGattCharacteristic(VIDEO_UUID, BluetoothGattCharacteristic.PROPERTY_NOTIFY, BluetoothGattCharacteristic.PERMISSION_READ)
        videoCharacteristic!!.addDescriptor(BluetoothGattDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"), BluetoothGattDescriptor.PERMISSION_READ or BluetoothGattDescriptor.PERMISSION_WRITE))
        val control = BluetoothGattCharacteristic(CONTROL_UUID, BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE, BluetoothGattCharacteristic.PERMISSION_WRITE)
        val status = BluetoothGattCharacteristic(STATUS_UUID, BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_NOTIFY, BluetoothGattCharacteristic.PERMISSION_READ)
        service.addCharacteristic(videoCharacteristic)
        service.addCharacteristic(control)
        service.addCharacteristic(status)
        gattServer?.addService(service)
        advertiser = adapter.bluetoothLeAdvertiser
        val settings = AdvertiseSettings.Builder().setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY).setConnectable(true).setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH).build()
        val data = AdvertiseData.Builder().setIncludeDeviceName(false).addServiceUuid(ParcelUuid(SERVICE_UUID)).build()
        advertiser?.startAdvertising(settings, data, object : AdvertiseCallback() {
            override fun onStartSuccess(settingsInEffect: AdvertiseSettings) { isAdvertising = true }
            override fun onStartFailure(errorCode: Int) { isAdvertising = false; Log.e("KodoBLE", "Advertising failed: $errorCode") }
        })
    }

    fun sendVideoFrame(sequence: Int, frame: ByteArray) {
        val device = connected ?: return
        val characteristic = videoCharacteristic ?: return
        val mtuPayload = 180 // conservative default; actual negotiated MTU may differ
        val count = (frame.size + mtuPayload - 1) / mtuPayload
        for (part in 0 until count) {
            val start = part * mtuPayload
            val end = minOf(frame.size, start + mtuPayload)
            val header = ByteBuffer.allocate(8).putInt(sequence).putShort(part.toShort()).putShort(count.toShort()).array()
            val packet = header + frame.copyOfRange(start, end)
            characteristic.value = packet
            try { gattServer?.notifyCharacteristicChanged(device, characteristic, false) } catch (_: Exception) {}
        }
    }
}
