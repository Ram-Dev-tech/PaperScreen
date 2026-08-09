package com.paperscreen.android.control

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat

class SystemToggleManager(private val context: Context) {

    fun isWifiEnabled(): Boolean {
        return try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            wifiManager?.isWifiEnabled == true
        } catch (e: Exception) {
            false
        }
    }

    fun toggleWifi(enable: Boolean) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // On Android Q+, we must launch the settings panel to let the user toggle wifi
                val panelIntent = Intent(Settings.Panel.ACTION_WIFI)
                panelIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(panelIntent)
            } else {
                @Suppress("DEPRECATION")
                val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
                wifiManager?.isWifiEnabled = enable
            }
        } catch (e: Exception) {
            // Fallback for custom ROMs (like HyperOS) that might crash on the Panel API
            try {
                val settingsIntent = Intent(Settings.ACTION_WIFI_SETTINGS)
                settingsIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(settingsIntent)
            } catch (ex: Exception) {
                android.util.Log.e("SystemToggleManager", "Unable to launch settings", ex)
            }
        }
    }

    fun isBluetoothEnabled(): Boolean {
        return try {
            val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
            val adapter = bluetoothManager?.adapter ?: return false
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    @SuppressLint("MissingPermission")
                    val enabled = adapter.isEnabled
                    enabled
                } else {
                    false
                }
            } else {
                @SuppressLint("MissingPermission")
                val enabled = adapter.isEnabled
                enabled
            }
        } catch (e: Exception) {
            false
        }
    }

    fun toggleBluetooth(enable: Boolean) {
        try {
            val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
            val adapter = bluetoothManager?.adapter

            // On Android 13+ (API 33), adapter.enable()/disable() is deprecated and restricted.
            // Launch the settings intent for user action.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                launchBluetoothSettings()
                return
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    @Suppress("DEPRECATION")
                    if (enable) adapter?.enable() else adapter?.disable()
                } else {
                    launchBluetoothSettings()
                }
            } else {
                @Suppress("DEPRECATION")
                @SuppressLint("MissingPermission")
                if (enable) adapter?.enable() else adapter?.disable()
            }
        } catch (e: Exception) {
            launchBluetoothSettings()
        }
    }

    private fun launchBluetoothSettings() {
        try {
            val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } catch (e: Exception) {
            android.util.Log.e("SystemToggleManager", "Unable to launch intent", e)
        }
    }
}
