package org.daimhim.im.core.android

import android.app.Application
import com.kongqw.network.monitor.NetworkMonitorManager
import org.daimhim.container.ContextHelper
import org.daimhim.im.core.FSNConfig
import org.daimhim.im.core.FrogServiceNative
import org.daimhim.im.core.QGBHeartbeat
import org.daimhim.im.core.android.FullLifecycleHandler
import org.daimhim.imc_core.WebSocketEngine
import timber.multiplatform.log.DebugTree
import timber.multiplatform.log.Timber

class StartApp : Application() {
    private val base_url = "wss://client.qgbtech.cn/ws:90?token=%s&name=%s&platform=android"
//    private val base_url = "ws://44293ff6.r9.cpolar.top"
    override fun onCreate() {
        super.onCreate()
        Timber.plant(DebugTree())
        ContextHelper.init(this)
        NetworkMonitorManager.getInstance().init(this)
        registerActivityLifecycleCallbacks(FullLifecycleHandler())
        FrogServiceNative.setFSNConfig(object : FSNConfig{
            override fun getBaseWs(token: String?, imAccount: String?): String {
                return String.format(base_url,token,imAccount)
            }

            override fun getToken(): String {
                return "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzUxMiJ9.eyJzdWIiOiIxMTk5MjQ0MzIxMjU0MTUwMTQ0Iiwic2NvcGUiOiJkZWZhdWx0IiwiaXNzIjoiMTMwODg5NTYxMTIiLCJsb2dpbiI6MTY5MDUyNTI3M30.SVbpqPluVnAoFf_tauafCabh-RfTcXslFD_C95aOKBDfoMM_yOBM16L6Y17q0EpL-eBZc6oi0RQWpAQUYKVbgdboEq3ZFItAMSEphJENcLLKgyy8PVw5cIlNapa9Eq3-wArZHI2qc3ICsR6_FJqH5rEnir6jqXPEqJMdhPvoDkg"
            }

            override fun getImAccount(): String {
                return "202206211949282"
            }

            override fun crate(builder: WebSocketEngine.Builder): WebSocketEngine.Builder {
                return builder
                    .customHeartbeat(QGBHeartbeat())
            }

            override fun bindEngine(webSocketEngine: WebSocketEngine) {
//                webSocketEngine.addIMCListener()
            }
        })
    }

}