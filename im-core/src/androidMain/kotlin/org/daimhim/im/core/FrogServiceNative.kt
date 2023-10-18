package org.daimhim.im.core

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.RemoteException
import okio.ByteString.Companion.toByteString
import org.daimhim.container.ContextHelper
import org.daimhim.imc_core.IEngine
import org.daimhim.imc_core.IEngineState
import org.daimhim.imc_core.WebSocketEngine
import timber.multiplatform.log.Timber

class FrogServiceNative : Service() {
    companion object {
        internal var fsnConfig: FSNConfig? = null

        @JvmStatic
        fun setFSNConfig(config: FSNConfig) {
            fsnConfig = config
        }
    }

    private lateinit var iEngine: IEngine

    override fun onCreate() {
        super.onCreate()
        if (!this::iEngine.isInitialized){
            iEngine = WebSocketEngine
                .Builder()
                .let {
                    fsnConfig?.crate(it)?:it
                }
                .build()
            fsnConfig?.bindEngine(iEngine as WebSocketEngine)
        }
        try {
            val token = fsnConfig?.getToken() ?: return
            val imAccount = fsnConfig?.getImAccount() ?: return
            frogServiceIBinder.setAccountInfo(token, imAccount)
        } catch (e: Exception) {
            e.printStackTrace()
            Timber.e(e)
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        return frogServiceIBinder
    }

    private val frogServiceIBinder = object : FrogService.Stub() {
        private val imcListenerManager = mutableMapOf<RemoteV2IMCListener, NativeV2IMCListener>()
        override fun addIMCListener(listener: RemoteV2IMCListener?) {
            Timber.i("addIMCListener ${listener} ${ContextHelper.getProcessName()} ${ContextHelper.isMainProcess()}")
            listener ?: throw NullPointerException("addIMCListener(valueCallBack)")
            val mergeForward = imcListenerManager[listener] ?: NativeV2IMCListener(listener)
            iEngine
                .addIMCListener(mergeForward)
            imcListenerManager[listener] = mergeForward
        }

        override fun removeIMCListener(listener: RemoteV2IMCListener?) {
            iEngine
                .removeIMCListener(imcListenerManager[listener] ?: return)
            imcListenerManager.remove(listener)
        }

        override fun setIMCStatusListener(listener: RemoteIMCStatusListener?) {
            if (listener == null) {
                iEngine
                    .setIMCStatusListener(null)
                return
            }
            val nativeIMCStatusListener = NativeIMCStatusListener(listener)
            when (
                iEngine
                    .engineState()) {
                IEngineState.ENGINE_OPEN -> {
                    nativeIMCStatusListener.connectionSucceeded()
                }

                IEngineState.ENGINE_CLOSED -> {
                    nativeIMCStatusListener.connectionClosed()
                }

                else -> {
                    nativeIMCStatusListener.connectionLost(RemoteException("状态恢复"))
                    //无操作
                }
            }
            iEngine
                .setIMCStatusListener(nativeIMCStatusListener)
        }

        override fun setAccountInfo(token: String, imAccount: String) {
            iEngine.engineOn(
                fsnConfig?.getBaseWs(token, imAccount)
                    ?.also {
                        Timber.i("setAccountInfo(${it})")
                    }
                    ?: throw IllegalStateException("BaseWs 不能为空")
            )
        }

        override fun makeConnection() {
            // nothing to do
        }

        override fun onChangeMode(mode: Int) {
            iEngine.onChangeMode(mode)
        }

        override fun onNetworkChange(mode: Int) {
            iEngine.onNetworkChange(mode)
        }

        override fun sendByte(md5: String?, index: Int, length: Int, data: ByteArray): Boolean {
            var isSuccess = false
            BigDataSplitUtil.dataAssemblyByte(md5, index, length, data) {
                isSuccess = iEngine.send(it.toByteString())
            }
            return isSuccess
        }

        override fun sendString(md5: String?, index: Int, length: Int, data: ByteArray): Boolean {
            var isSuccess = false
            BigDataSplitUtil.dataAssemblyStr(md5, index, length, data) {
                isSuccess = iEngine.send(it)
            }
            return isSuccess
        }

        override fun engineState(): Int {
            return iEngine.engineState()
        }

        override fun loginOut() {
            iEngine.engineOff()
        }

    }
}