package org.daimhim.im.core

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.os.RemoteException
import android.util.ArrayMap
import org.daimhim.imc_core.*

class FrogServiceNative : Service() {


    private lateinit var iEngine: IEngine

    override fun onCreate() {
        super.onCreate()
        iEngine = OkhttpIEngine
            .Builder()
            .let {
                FSNConfig.fsnConfig?.crate(it) ?: it
            }
            .build()
        FSNConfig
            .fsnConfig
            ?.bindEngine(iEngine as OkhttpIEngine)
    }

    override fun onBind(intent: Intent?): IBinder {
        return frogServiceIBinder
    }



    private val frogServiceIBinder = object : FrogService.Stub() {
        override fun engineOn(key: String?) {
            iEngine
                .engineOn(key ?: throw RemoteException("key 不可为空"))
        }

        override fun engineOff() {
            iEngine
                .engineOff()
        }

        override fun engineState(): Int {
            return iEngine.engineState()
        }

        override fun makeConnection() {
            iEngine.onNetworkChange(-1)
        }

        override fun onChangeMode(mode: Int) {
            iEngine.onChangeMode(mode)
        }

        override fun onNetworkChange(mode: Int) {
            iEngine.onNetworkChange(mode)
        }

        override fun setSharedParameters(parameters: MutableMap<String, String>?) {
            FSNConfig.setSharedParameters(parameters ?: mutableMapOf())
        }

        override fun sendByte(md5: String?, index: Int, length: Int, data: ByteArray): Boolean {
            var isSuccess = false
            BigDataSplitUtil
                .dataAssemblyByte(md5, index, length, data) {
                    isSuccess = iEngine.send(it)
                }
            return isSuccess
        }

        override fun sendString(md5: String?, index: Int, length: Int, data: ByteArray): Boolean {
            var isSuccess = false
            BigDataSplitUtil
                .dataAssemblyStr(md5, index, length, data) {
                    isSuccess = iEngine.send(it)
                }
            return isSuccess
        }

        private val imcListeners = ArrayMap<RemoteV2IMCListener, V2IMCListener>()
        override fun addIMCListener(listener: RemoteV2IMCListener?) {
            if (imcListeners.containsKey(listener)) {
                return
            }
            val imcListener = object : V2IMCListener {
                override fun onMessage(byteArray: ByteArray) {
                    BigDataSplitUtil.dataSplitting(byteArray) { p0, p1, p2, p3 ->
                        listener?.onMessageByte(p0, p1, p2, p3)
                    }
                }

                override fun onMessage(text: String) {
                    BigDataSplitUtil.dataSplitting(text) { p0, p1, p2, p3 ->
                        listener?.onMessageString(p0, p1, p2, p3)
                    }
                }
            }
            iEngine.addIMCListener(imcListener)
            imcListeners.put(listener, imcListener)
        }
        override fun removeIMCListener(listener: RemoteV2IMCListener?) {
            if (!imcListeners.containsKey(listener)) {
                return
            }
            val iterator = imcListeners.iterator()
            var next: MutableMap.MutableEntry<RemoteV2IMCListener, V2IMCListener>
            while (iterator.hasNext()) {
                next = iterator.next()
                if (next.value == listener) {
                    iEngine.removeIMCListener(next.value)
                    iterator.remove()
                }
            }
        }

        private val imcSocketListeners = ArrayMap<RemoteV2IMCListener, V2IMCSocketListener>()

        override fun addIMCSocketListener(level: Int, listener: RemoteV2IMCListener?) {
            if (imcSocketListeners.containsKey(listener)) {
                return
            }
            val imcSocketListener = object : V2IMCSocketListener {
                override fun onMessage(iEngine: IEngine, bytes: ByteArray): Boolean {
                    var isSuccess = false
                    BigDataSplitUtil.dataSplitting(bytes) { p0, p1, p2, p3 ->
                        isSuccess = listener?.onMessageByte(p0, p1, p2, p3) ?: false
                    }
                    return isSuccess
                }

                override fun onMessage(iEngine: IEngine, text: String): Boolean {
                    var isSuccess = false
                    BigDataSplitUtil.dataSplitting(text) { p0, p1, p2, p3 ->
                        isSuccess = listener?.onMessageString(p0, p1, p2, p3) ?: false
                    }
                    return isSuccess
                }
            }
            iEngine.addIMCSocketListener(level, imcSocketListener)
            imcSocketListeners.put(listener, imcSocketListener)
        }

        override fun removeIMCSocketListener(listener: RemoteV2IMCListener?) {
            if (!imcSocketListeners.containsKey(listener)) {
                return
            }
            val iterator = imcSocketListeners.iterator()
            var next: MutableMap.MutableEntry<RemoteV2IMCListener, V2IMCSocketListener>
            while (iterator.hasNext()) {
                next = iterator.next()
                if (next.value == listener) {
                    iEngine.removeIMCSocketListener(next.value)
                    iterator.remove()
                }
            }
        }

        override fun setIMCStatusListener(listener: RemoteIMCStatusListener?) {
            if (listener == null) {
                iEngine.setIMCStatusListener(null)
                return
            }
            iEngine.setIMCStatusListener(object : IMCStatusListener {
                override fun connectionClosed() {
                    listener.connectionClosed()
                }

                override fun connectionLost(throwable: Throwable) {
                    listener.connectionLost(Bundle())
                }

                override fun connectionSucceeded() {
                    listener.connectionSucceeded()
                }

            })
        }

    }
}