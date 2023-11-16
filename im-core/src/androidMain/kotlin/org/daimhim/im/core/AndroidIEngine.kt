package org.daimhim.im.core

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.RemoteException
import android.util.ArrayMap
import org.daimhim.container.ContextHelper
import org.daimhim.imc_core.*
import timber.multiplatform.log.Timber
import java.util.concurrent.TimeoutException

class AndroidIEngine : IEngine {

    companion object {
        val FROG_SERVICE_ACTION = "org.daimhim.im.core.frog"
    }

    override fun engineOff() {
        connect()?.engineOff()
    }

    override fun engineOn(key: String) {
        connect()?.engineOn(key)
    }

    override fun engineState(): Int {
        return connect()?.engineState()?:IEngineState.ENGINE_CLOSED
    }

    override fun makeConnection() {
        connect()?.makeConnection()
    }


    override fun onChangeMode(mode: Int) {
        connect()
            ?.onChangeMode(mode)
    }

    override fun onNetworkChange(networkState: Int) {
        connect()
            ?.onNetworkChange(networkState)
    }

    override fun send(text: String): Boolean {
        var isSuccess = false
        BigDataSplitUtil
            .dataSplitting(text) { p0, p1, p2, p3 ->
                isSuccess = connect()
                    ?.sendString(p0, p1, p2, p3)?:false
            }
        return isSuccess
    }


    override fun send(byteArray: ByteArray): Boolean {
        var isSuccess = false
        BigDataSplitUtil
            .dataSplitting(byteArray) { p0, p1, p2, p3 ->
                isSuccess = connect()
                    ?.sendByte(p0, p1, p2, p3)?:false
            }
        return isSuccess
    }

    override fun setIMCStatusListener(listener: IMCStatusListener?) {
        if (listener == null) {
            connect()?.setIMCStatusListener(null)
            return
        }
        connect()?.setIMCStatusListener(object : RemoteIMCStatusListener.Stub() {

            override fun connectionClosed() {
                listener.connectionClosed()
            }

            override fun connectionLost(throwable: Bundle?) {
                listener.connectionLost(RemoteException("咋回事啊！"))
            }

            override fun connectionSucceeded() {
                listener.connectionSucceeded()
            }

        })
    }

    private val imcListeners = ArrayMap<RemoteV2IMCListener, V2IMCListener>()
    override fun addIMCListener(imcListener: V2IMCListener) {
        // 是否包含value
        if (imcListeners.containsValue(imcListener)) {
            return
        }
        val value = object : RemoteV2IMCListener.Stub() {
            override fun onMessageByte(md5: String?, index: Int, length: Int, data: ByteArray?): Boolean {
                BigDataSplitUtil.dataAssemblyByte(md5, index, length, data ?: byteArrayOf()) {
                    imcListener.onMessage(it)
                }
                return true
            }

            override fun onMessageString(md5: String?, index: Int, length: Int, data: ByteArray?): Boolean {
                BigDataSplitUtil.dataAssemblyStr(md5, index, length, data ?: byteArrayOf()) {
                    imcListener.onMessage(it)
                }
                return true
            }
        }
        connect()?.addIMCListener(value)
        imcListeners.put(value, imcListener)
    }
    override fun removeIMCListener(imcListener: V2IMCListener) {
        if (!imcListeners.containsValue(imcListener)) {
            return
        }
        val iterator = imcListeners.iterator()
        var next: MutableMap.MutableEntry<RemoteV2IMCListener, V2IMCListener>
        while (iterator.hasNext()) {
            next = iterator.next()
            if (next.value == imcListener) {
                connect()?.removeIMCListener(next.key)
                iterator.remove()
            }
        }
    }

    private val imcSocketListeners = ArrayMap<RemoteV2IMCListener, V2IMCSocketListener>()
    override fun addIMCSocketListener(level: Int, imcSocketListener: V2IMCSocketListener) {
        // 是否包含value
        if (imcSocketListeners.containsValue(imcSocketListener)) {
            return
        }
        val value = object : RemoteV2IMCListener.Stub() {
            override fun onMessageByte(md5: String?, index: Int, length: Int, data: ByteArray?): Boolean {
                var isSuccess = false
                BigDataSplitUtil.dataAssemblyByte(md5, index, length, data ?: byteArrayOf()) {
                    isSuccess = imcSocketListener.onMessage(this@AndroidIEngine, it)
                }
                return isSuccess
            }

            override fun onMessageString(md5: String?, index: Int, length: Int, data: ByteArray?): Boolean {
                var isSuccess = false
                BigDataSplitUtil.dataAssemblyStr(md5, index, length, data ?: byteArrayOf()) {
                    isSuccess = imcSocketListener.onMessage(this@AndroidIEngine, it)
                }
                return isSuccess
            }
        }
        connect()?.addIMCSocketListener(level, value)
        imcSocketListeners.put(value, imcSocketListener)
    }
    override fun removeIMCSocketListener(imcSocketListener: V2IMCSocketListener) {
        if (!imcSocketListeners.containsValue(imcSocketListener)) {
            return
        }
        val iterator = imcListeners.iterator()
        var next: MutableMap.MutableEntry<RemoteV2IMCListener, V2IMCListener>
        while (iterator.hasNext()) {
            next = iterator.next()
            if (next.value == imcSocketListener) {
                connect()?.removeIMCSocketListener(next.key)
                iterator.remove()
            }
        }
    }
    fun setSharedParameters(parameters:MutableMap<String,String>){
        connect()?.setSharedParameters(parameters)
    }
    fun isConnectLocal():Boolean{
        return frogService != null
    }

    private val aWait = Object()
    private var frogService: FrogService? = null

    /**
     * 连接子进程服务
     */
    internal fun connect(): FrogService? {
        if (frogService != null) {
            return frogService
        }
        synchronized(aWait){
            try {
                // 永存启动
                Timber.i("connect ${Thread.currentThread().name}")
                var intent = Intent(FROG_SERVICE_ACTION)
                intent.setPackage(ContextHelper.getApplication().packageName)
                ContextHelper
                    .getApplication()
                    .startService(intent)
                // 临绑启动
                intent = Intent(FROG_SERVICE_ACTION)
                intent.setPackage(ContextHelper.getApplication().packageName)
                ContextHelper
                    .getApplication()
                    .bindService(intent, ConnectService(this), Context.BIND_AUTO_CREATE)
                // 等待
                aWait.wait()
            } catch (e: TimeoutException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } finally {
                aWait.notify()
            }
        }

        return frogService
    }

    class ConnectService(private val androidIEngine: AndroidIEngine) : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            try {
                androidIEngine.frogService = FrogService.Stub.asInterface(service)
                Timber.i("onServiceConnected ${androidIEngine.frogService?.engineState()} ${Thread.currentThread().name}")
            } finally {
                synchronized(androidIEngine.aWait){
                    androidIEngine.aWait.notify()
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            try {
                Timber.i("onServiceDisconnected ${Thread.currentThread().name}")
                androidIEngine.frogService = null
            } finally {
                synchronized(androidIEngine.aWait){
                    androidIEngine.aWait.notify()
                }
            }
        }
    }

}