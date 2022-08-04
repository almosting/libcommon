package com.almotsing.net

import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pInfo
import java.lang.Exception

interface WifiP2pListener {
    fun onStateChanged(enabled: Boolean)
    fun onUpdateDevices(devices: List<WifiP2pDevice>)
    fun onConnect(info: WifiP2pInfo)
    fun onDisconnect()
    fun onError(e: Exception)
}