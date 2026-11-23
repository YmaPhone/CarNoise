package com.ymaphone.carnoise

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class CarReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        val dev: BluetoothDevice? = if (Build.VERSION.SDK_INT >= 33)
            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
        else @Suppress("DEPRECATION") intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
        val p = Prefs(ctx)
        // רק הרכב שנבחר מפעיל את הפרופיל
        if (dev == null || dev.address != p.carAddress) return
        when (intent.action) {
            BluetoothDevice.ACTION_ACL_CONNECTED -> if (p.autoEnable) CarMode.enter(ctx)
            BluetoothDevice.ACTION_ACL_DISCONNECTED -> CarMode.exit(ctx)
        }
    }
}
