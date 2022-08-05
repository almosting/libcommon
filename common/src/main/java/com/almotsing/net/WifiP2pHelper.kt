package com.almotsing.net

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.NetworkInfo
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.WifiP2pManager.ActionListener
import android.util.Log
import androidx.annotation.RequiresPermission
import java.lang.ref.WeakReference
import java.util.concurrent.CopyOnWriteArraySet


class WifiP2pHelper private constructor() {
    private lateinit var mWeakContext: WeakReference<Context>
    private lateinit var mWifiP2pManager: WifiP2pManager
    private lateinit var mChannel: WifiP2pManager.Channel
    private val mListeners = CopyOnWriteArraySet<WifiP2pListener>()
    private val mAvailableDevices: MutableList<WifiP2pDevice> = ArrayList()
    private var mIsWifiP2pEnabled: Boolean = false
    private var mWifiP2pDevice: WifiP2pDevice? = null

    fun init(context: Context) {
        mWeakContext = WeakReference<Context>(context)
        mWifiP2pManager = context.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
    }

    @Synchronized
    fun register() {
        if (!::mWeakContext.isInitialized) {
            return
        }
        mWeakContext.get()?.let { context ->
            mChannel = mWifiP2pManager.initialize(context, context.mainLooper, mChannelListener)
            val intentFilter = IntentFilter().apply {
                addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
                addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
                addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
                addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
            }
            context.registerReceiver(mReceiver, intentFilter)
        }
    }

    @Synchronized
    fun unregister() {
        mIsWifiP2pEnabled = false
        internalDisconnect(null)
        mWeakContext.get()?.let {
            try {
                it.unregisterReceiver(mReceiver)
            } catch (e: Exception) {
                Log.w(TAG, e)
            }
        }
    }

    fun add(listener: WifiP2pListener) {
        mListeners.add(listener)
    }

    fun remove(listener: WifiP2pListener) {
        mListeners.remove(listener)
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    @Synchronized
    @Throws(IllegalStateException::class)
    fun startDiscovery() {
        if (DEBUG) Log.v(TAG, "startDiscovery:")
        if (::mChannel.isInitialized) {
            mWifiP2pManager.discoverPeers(mChannel, object : ActionListener {
                override fun onSuccess() {
                }

                override fun onFailure(reason: Int) {
                    callOnError(RuntimeException("failed to start discovery, reason=$reason"))
                }

            })
        } else {
            throw IllegalStateException("not registered")
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun connect(remoteMacAddress: String) {
        if (DEBUG) Log.v(TAG, "connect:remoteMacAddress=$remoteMacAddress");
        val config = WifiP2pConfig().apply {
            deviceAddress = remoteMacAddress
            wps.setup = WpsInfo.PBC
        }
        connect(config)
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun connect(device: WifiP2pDevice) {
        if (DEBUG) Log.v(TAG, "connect:device=$device")
        val config = WifiP2pConfig().apply {
            deviceAddress = device.deviceAddress
            wps.setup = WpsInfo.PBC
        }
        connect(config)
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    @Throws(IllegalStateException::class)
    fun connect(config: WifiP2pConfig) {
        if (DEBUG) Log.v(TAG, "connect:config=$config")
        if (::mChannel.isInitialized) {
            mWifiP2pManager.connect(mChannel, config, object : ActionListener {
                override fun onSuccess() {
                    // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
                }

                override fun onFailure(reason: Int) {
                    callOnError(RuntimeException("failed to connect, reason=$reason"))
                }
            })
        } else {
            throw IllegalStateException("not registered")
        }
    }


    @Synchronized
    fun disconnect() {
        if (DEBUG) Log.v(TAG, "disconnect:")
        internalDisconnect(object : ActionListener {
            override fun onSuccess() {}
            override fun onFailure(reason: Int) {
                callOnError(RuntimeException("failed to disconnect, reason=$reason"))
            }
        })
    }

    @Synchronized
    fun isWiFiP2pEnabled(): Boolean {
        return ::mChannel.isInitialized && mIsWifiP2pEnabled
    }

    private fun internalDisconnect(listener: ActionListener?) {
        if (::mWifiP2pManager.isInitialized) {
            if (mWifiP2pDevice == null
                || mWifiP2pDevice?.status == WifiP2pDevice.CONNECTED
            ) {
                if (::mChannel.isInitialized) {
                    mWifiP2pManager.removeGroup(mChannel, listener)
                }
            } else if (mWifiP2pDevice?.status == WifiP2pDevice.AVAILABLE
                || mWifiP2pDevice?.status == WifiP2pDevice.INVITED
            ) {
                mWifiP2pManager.cancelConnect(mChannel, listener)
            }
        }
    }

    @Synchronized
    private fun setIsWifiP2pEnabled(enabled: Boolean) {
        mIsWifiP2pEnabled = enabled
        callOnStateChanged(enabled)
    }

    @Synchronized
    private fun resetData() {
        if (DEBUG) Log.v(TAG, "resetData:")
        if (isConnectedOrConnecting()) {
            callOnDisconnect()
        }
    }

    @Synchronized
    private fun updateDevice(device: WifiP2pDevice?) {
        if (DEBUG) Log.v(TAG, "updateDevice:device=$device")
        mWifiP2pDevice = device
    }

    @Synchronized
    fun isConnected(): Boolean {
        return mWifiP2pDevice != null && mWifiP2pDevice!!.status == WifiP2pDevice.CONNECTED
    }

    @Synchronized
    fun isConnectedOrConnecting(): Boolean {
        return (mWifiP2pDevice != null
                && (mWifiP2pDevice!!.status == WifiP2pDevice.CONNECTED
                || mWifiP2pDevice!!.status == WifiP2pDevice.INVITED))
    }

    //********************************CallBacks***********************************

    private fun callOnStateChanged(enabled: Boolean) {
        if (DEBUG) Log.v(TAG, "callOnStateChanged:enabled=$enabled")
        mListeners.forEach {
            try {
                it.onStateChanged(enabled)
            } catch (e1: Exception) {
                Log.w(TAG, e1)
                mListeners.remove(it)
            }
        }
    }

    private fun callOnUpdateDevices(devices: List<WifiP2pDevice>) {
        if (DEBUG) Log.v(TAG, "callOnUpdateDevices:")
        for (listener in mListeners) {
            try {
                listener.onUpdateDevices(devices)
            } catch (e1: Exception) {
                Log.w(TAG, e1)
                mListeners.remove(listener)
            }
        }
    }

    private fun callOnConnect(info: WifiP2pInfo) {
        if (DEBUG) Log.v(TAG, "callOnConnect:")
        for (listener in mListeners) {
            try {
                listener.onConnect(info)
            } catch (e1: Exception) {
                Log.w(TAG, e1)
                mListeners.remove(listener)
            }
        }
    }

    private fun callOnDisconnect() {
        if (DEBUG) Log.v(TAG, "callOnDisconnect:")
        for (listener in mListeners) {
            try {
                listener.onDisconnect()
            } catch (e1: Exception) {
                Log.w(TAG, e1)
                mListeners.remove(listener)
            }
        }
    }

    private fun callOnError(e: Exception) {
        if (DEBUG) Log.w(TAG, "callOnError:", e)
        mListeners.forEach {
            try {
                it.onError(e)
            } catch (e1: Exception) {
                Log.w(TAG, e1)
                mListeners.remove(it)
            }
        }
    }

    //********************************Listeners***********************************

    private val mChannelListener = WifiP2pManager.ChannelListener {
        setIsWifiP2pEnabled(false)
        resetData()
        //todo 可以进行重试
    }

    private val mPeerListListener = WifiP2pManager.PeerListListener {
        if (DEBUG) Log.v(TAG, "onPeersAvailable:peers=$it")
        val devices = it.deviceList
        synchronized(mAvailableDevices) {
            mAvailableDevices.clear()
            mAvailableDevices.addAll(devices)
        }
        callOnUpdateDevices(mAvailableDevices)
    }

    private val mConnectionInfoListener = WifiP2pManager.ConnectionInfoListener {
        if (DEBUG) Log.v(TAG, "onConnectionInfoAvailable:info=$it")
        callOnConnect(it)
    }

    private val mReceiver = object : BroadcastReceiver() {
        @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                    if (DEBUG) Log.v(TAG, "onReceive:WIFI_P2P_STATE_CHANGED_ACTION")
                    try {
                        val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                        if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                            setIsWifiP2pEnabled(true)
                            if (DEBUG) Log.d(TAG, "P2P state changed, enabled")
                        } else {
                            setIsWifiP2pEnabled(false)
                            resetData()
                            if (DEBUG) Log.d(TAG, "P2P state changed, disabled")
                        }
                    } catch (e: Exception) {
                        callOnError(e)
                    }
                }
                WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                    if (DEBUG) Log.v(TAG, "onReceive:WIFI_P2P_PEERS_CHANGED_ACTION")
                    try {
                        mWifiP2pManager.requestPeers(mChannel, mPeerListListener)
                    } catch (e: Exception) {
                        callOnError(e)
                    }
                    if (DEBUG) Log.d(TAG, "P2P peers changed")
                }
                WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                    try {
                        val networkInfo: NetworkInfo? = intent.getParcelableExtra(
                            WifiP2pManager.EXTRA_NETWORK_INFO
                        )
                        if (DEBUG) Log.v(
                            TAG,
                            "onReceive:WIFI_P2P_CONNECTION_CHANGED_ACTION, networkInfo=$networkInfo"
                        )
                        if (networkInfo?.isConnected == true) {
                            // we are connected with the other device, request connection
                            // info to find group owner IP
                            mWifiP2pManager.requestConnectionInfo(
                                mChannel,
                                mConnectionInfoListener
                            )
                        } else {
                            // It's a disconnect
                            resetData()
                        }
                    } catch (e: Exception) {
                        callOnError(e)
                    }
                }
                WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                    if (DEBUG) Log.v(TAG, "onReceive:WIFI_P2P_THIS_DEVICE_CHANGED_ACTION")
                    try {
                        val device: WifiP2pDevice? = intent.getParcelableExtra(
                            WifiP2pManager.EXTRA_WIFI_P2P_DEVICE
                        )
                        updateDevice(device)
                    } catch (e: Exception) {
                        callOnError(e)
                    }
                }
            }
        }

    }


    companion object {
        private const val DEBUG = false
        private val TAG = WifiP2pHelper::class.simpleName
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { WifiP2pHelper() }
    }
}