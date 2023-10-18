package org.daimhim.im.core

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.JsonReader
import com.google.gson.JsonParser
import okio.ByteString
import okio.ByteString.Companion.toByteString
import org.daimhim.container.ContextHelper
import org.daimhim.imc_core.*
import timber.multiplatform.log.Timber
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.locks.ReentrantLock

class PlatformIEngine {
    private val connectService = ConnectService(this)
    private val messagePowerCore = MessagePowerCore()
    private val interceptors = mutableListOf<Interceptor>()
    private val mainExecutive = object : MessagePowerCore.MainExecutive {
        override fun realSend(request: Request): Int {
            var isSuccess = false
            when (request) {
                is ByteArrayRequest -> {
                    BigDataSplitUtil
                        .dataSplitting(request.byteArray) { p0, p1, p2, p3 ->
                            isSuccess = connectService
                                .connect()
                                .sendByte(p0, p1, p2, p3)
                        }
                }

                is StringRequest -> {
                    BigDataSplitUtil
                        .dataSplitting(request.text) { p0, p1, p2, p3 ->
                            isSuccess = connectService
                                .connect()
                                .sendString(p0, p1, p2, p3)
                        }
                }

                else -> {
                    throw IllegalStateException("不支持的请求类型${request}")
                }
            }
            if (isSuccess) {
                return 1
            }
            return 0
        }

        override fun realSuccessResponseCall(pending: MessagePowerCore.Pending, response: Response){
            pending
                .callback
                .get()
                ?.onResponse(response)
        }

        override fun realFailureCall(pending: MessagePowerCore.Pending, response:Response, throwable: Throwable){
            pending
                .callback
                .get()
                ?.onFailure(response,throwable)
        }

    }
    private val mainReceipt = object : V2IMCSocketListener<PlatformIEngine>{
        override fun onMessage(iEngine: PlatformIEngine, bytes: ByteString): Boolean {
            val parseString = JsonParser.parseString(bytes.utf8())
            if (!parseString.isJsonObject){
                return super.onMessage(iEngine, bytes)
            }
            val asJsonObject = parseString.asJsonObject
            val get = asJsonObject.get("pushId")
            if (!get.isJsonPrimitive){
                return super.onMessage(iEngine, bytes)
            }
            val asLong = get.asLong
            messagePowerCore.callResponseBody(asLong,ByteArrayResponseBody(bytes))
            return super.onMessage(iEngine, bytes)
        }

        override fun onMessage(iEngine: PlatformIEngine, text: String): Boolean {
            val parseString = JsonParser.parseString(text)
            if (!parseString.isJsonObject){
                return super.onMessage(iEngine, text)
            }
            val asJsonObject = parseString.asJsonObject
            val get = asJsonObject.get("pushId")
            if (!get.isJsonPrimitive){
                return super.onMessage(iEngine, text)
            }
            val asLong = get.asLong
            messagePowerCore.callResponseBody(asLong,StringResponseBody(text))
            return super.onMessage(iEngine, text)
        }
    }
    init {
        messagePowerCore.mainExecutive = mainExecutive
        addIMCSocketListener(20,mainReceipt)
    }

    companion object {
        val FROG_SERVICE_ACTION = "org.daimhim.im.core.frog"
    }

    fun login(token: String, imAccount: String) {
        connectService
            .connect()
            .setAccountInfo(token, imAccount)
    }

    fun signOut() {
        connectService
            .connect()
            .loginOut()
    }

    fun engineState(): Int {
        return connectService
            .connect()
            .engineState()
    }

    fun onChangeMode(mode: Int) {
        connectService
            .connect()
            .onChangeMode(mode)
    }

    fun onNetworkChange(mode: Int) {
        connectService
            .connect()
            .onNetworkChange(mode)
    }

    fun send(text: String): Call = RealCall(StringRequest(MsgIDFactory.nextId(), text),
        messagePowerCore,interceptors)

    fun send(byteArray: ByteArray): Call =
        RealCall(ByteArrayRequest(MsgIDFactory.nextId(), byteArray),
            messagePowerCore,interceptors)

    fun addInterceptor(interceptor: Interceptor){
        interceptors.add(interceptor)
    }
    fun removeInterceptor(interceptor: Interceptor){
        interceptors.remove(interceptor)
    }
    /***
     * 监听相关
     */
    private val imcListenerManager = IMCListenerManager<PlatformIEngine>()
    private val imcStatusListeners = mutableListOf<IMCStatusListener>()

    fun addIMCStatusListener(listener: IMCStatusListener) {
        imcStatusListeners.add(listener)
    }
    fun removeIMCStatusListener(listener: IMCStatusListener) {
        imcStatusListeners.remove(listener)
    }

    fun addIMCListener(imcListener: V2IMCListener) {
        imcListenerManager.addIMCListener(imcListener)
    }

    fun addIMCSocketListener(
        level: Int,
        imcSocketListener: V2IMCSocketListener<PlatformIEngine>
    ) {
        imcListenerManager.addIMCSocketListener(level, imcSocketListener)
    }

    fun removeIMCListener(imcListener: V2IMCListener) {
        imcListenerManager.removeIMCListener(imcListener)
    }

    fun removeIMCSocketListener(imcSocketListener: V2IMCSocketListener<PlatformIEngine>) {
        imcListenerManager.removeIMCSocketListener(imcSocketListener)
    }

    class ConnectService(private val platformIEngine: PlatformIEngine) {
        private var frogService: FrogService? = null
        private val reentrantLock = ReentrantLock()
        private val loopLock = reentrantLock.newCondition()


        fun connect(): FrogService {
            reentrantLock.lock()
            if (frogService != null) {
                return frogService!!
            }
            val intent = Intent(FROG_SERVICE_ACTION)
            intent.setPackage(ContextHelper.getApplication().packageName)
            ContextHelper
                .getApplication()
                .startService(intent)
            ContextHelper
                .getApplication()
                .bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            try {
                loopLock.await(5, TimeUnit.SECONDS)
            } catch (e: TimeoutException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            if (frogService != null && !isFirst) {
                isFirst = true
                Timber.i("remoteV2IMCListener:${remoteV2IMCListener} remoteIMCStatusListener:${remoteIMCStatusListener}")
                frogService?.addIMCListener(remoteV2IMCListener)
                frogService?.setIMCStatusListener(remoteIMCStatusListener)
            }
            reentrantLock.unlock()
            return frogService!!
        }

        private var isFirst = false
        private val serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                reentrantLock.lock()
                try {
                    frogService = FrogService.Stub.asInterface(service)
                    Timber.i("onServiceConnected ${frogService?.engineState()}")
                    loopLock.signal()
                } finally {
                    reentrantLock.unlock()
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                reentrantLock.lock()
                try {
                    Timber.i("onServiceDisconnected")
                    frogService = null
                } finally {
                    reentrantLock.unlock()
                }
            }

        }
        private val remoteIMCStatusListener = object : RemoteIMCStatusListener.Stub() {


            override fun connectionClosed() {
                platformIEngine.imcStatusListeners.forEach {
                    it.connectionClosed()
                }
            }

            override fun connectionLost(throwable: Bundle?) {
                platformIEngine.imcStatusListeners.forEach {
                    it.connectionLost(NullPointerException())
                }
            }

            override fun connectionSucceeded() {
                platformIEngine.imcStatusListeners.forEach {
                    it.connectionSucceeded()
                }
            }

        }
        private val remoteV2IMCListener = object : RemoteV2IMCListener.Stub() {

            override fun onMessageByte(md5: String?, index: Int, length: Int, data: ByteArray) {
                BigDataSplitUtil.dataAssemblyByte(md5, index, length, data) {
                    platformIEngine.imcListenerManager.onMessage(platformIEngine, it.toByteString())
                }
            }

            override fun onMessageString(md5: String?, index: Int, length: Int, data: ByteArray) {
                BigDataSplitUtil.dataAssemblyStr(md5, index, length, data) {
                    platformIEngine.imcListenerManager.onMessage(platformIEngine, it)
                }
            }
        }


    }


}